package com.traveldispatcher.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.traveldispatcher.app.data.email.BuiltEmail
import com.traveldispatcher.app.data.email.EmailBuilder
import com.traveldispatcher.app.data.export.PeriodPreset
import com.traveldispatcher.app.data.model.Agent
import com.traveldispatcher.app.data.model.Dispatch
import com.traveldispatcher.app.data.model.Inquiry
import com.traveldispatcher.app.data.model.LedgerRecord
import com.traveldispatcher.app.ui.components.AppBottomNavBar
import com.traveldispatcher.app.ui.components.AppTab
import com.traveldispatcher.app.ui.components.AppTopBar
import com.traveldispatcher.app.ui.components.OfflineBanner
import com.traveldispatcher.app.ui.screens.agents.AgentsScreen
import com.traveldispatcher.app.ui.screens.inquiries.InquiriesScreen
import com.traveldispatcher.app.ui.screens.ledger.LedgerScreen
import com.traveldispatcher.app.ui.screens.login.LoginScreen
import com.traveldispatcher.app.ui.screens.new_inquiry.NewInquiryScreen
import com.traveldispatcher.app.ui.screens.settings.SettingsScreen
import com.traveldispatcher.app.ui.theme.Slate50
import com.traveldispatcher.app.ui.theme.TravelDispatcherTheme
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TravelDispatcherApp

        setContent {
            TravelDispatcherTheme {
                MainContent(app = app)
            }
        }
    }
}

@Composable
fun MainContent(app: TravelDispatcherApp) {
    val isOnline by app.networkMonitor.isOnline.collectAsState()
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(AppTab.INQUIRIES) }

    val coroutineScope = rememberCoroutineScope()

    // Inquiries State
    val inquiries = remember { mutableStateListOf<Inquiry>() }
    // Ledger Records State
    val ledgerRecords = remember { mutableStateListOf<LedgerRecord>() }
    // Agents State
    val agents = remember { mutableStateListOf<Agent>() }

    // Initial load from repositories (or seed samples if fresh)
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            val agentRes = app.agentRepository.getAgents()
            if (agentRes.isSuccess && agentRes.getOrNull()?.isNotEmpty() == true) {
                agents.clear()
                agents.addAll(agentRes.getOrThrow())
            }
        }

        val inqRes = app.inquiryRepository.getInquiries()
        if (inqRes.isSuccess && inqRes.getOrNull()?.isNotEmpty() == true) {
            inquiries.clear()
            inquiries.addAll(inqRes.getOrThrow())
        }

        val ledgerRes = app.ledgerRepository.getLedgerRecords()
        if (ledgerRes.isSuccess && ledgerRes.getOrNull()?.isNotEmpty() == true) {
            ledgerRecords.clear()
            ledgerRecords.addAll(ledgerRes.getOrThrow())
        }
    }


    if (!isAuthenticated) {
        LoginScreen(
            onLoginSuccess = { isAuthenticated = true }
        )
        return
    }

    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    onSettingsClick = { currentTab = AppTab.SETTINGS },
                    onLogoutClick = {
                        isAuthenticated = false
                        currentTab = AppTab.INQUIRIES
                    }
                )
                OfflineBanner(isOnline = isOnline)
            }
        },
        bottomBar = {
            AppBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.INQUIRIES -> {
                    InquiriesScreen(
                        inquiries = inquiries,
                        onNewInquiryClick = { currentTab = AppTab.NEW },
                        onStatusChange = { inquiryId, newStatus ->
                            coroutineScope.launch {
                                app.inquiryRepository.updateInquiryStatus(inquiryId, newStatus)
                                val idx = inquiries.indexOfFirst { it.id == inquiryId }
                                if (idx != -1) {
                                    inquiries[idx] = inquiries[idx].copy(status = newStatus)
                                }
                            }
                        },
                        onDeleteInquiry = { inquiryId ->
                            coroutineScope.launch {
                                val res = app.inquiryRepository.deleteInquiry(inquiryId)
                                if (res.isSuccess) {
                                    inquiries.removeAll { it.id == inquiryId }
                                    Toast.makeText(app, "Inquiry deleted permanently.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(app, "Delete failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onExportPdf = {
                            val pdfFile = app.exportManager.generateInquiriesPdf(inquiries, includePhone = false)
                            app.exportManager.shareFile(pdfFile, "application/pdf", "Share Inquiries PDF")
                        },
                        onRetryDispatch = { dispatch ->
                            coroutineScope.launch {
                                val settings = app.keystoreManager.getEmailSettings()
                                val inq = inquiries.find { it.id == dispatch.inquiryId }
                                if (inq != null) {
                                    val built = EmailBuilder.buildEmail(
                                        inquiryNumber = inq.inquiryNumber,
                                        category = inq.category,
                                        clientName = inq.clientName,
                                        destination = inq.destination,
                                        pickupPoint = inq.pickupPoint,
                                        dropPoint = inq.dropPoint,
                                        dateType = inq.dateType,
                                        startDate = inq.startDate ?: "",
                                        endDate = inq.endDate ?: "",
                                        adults = inq.adults,
                                        children = inq.children,
                                        stops = inq.stops,
                                        flightTripType = inq.flightTripType,
                                        flightClass = inq.flightClass,
                                        preferredAirline = inq.preferredAirline,
                                        specialNotes = inq.specialNotes
                                    )
                                    app.smtpMailSender.dispatchBatch(listOf(dispatch), built, settings)
                                    // Refresh inquiries
                                    val updated = app.inquiryRepository.getInquiries()
                                    if (updated.isSuccess) {
                                        inquiries.clear()
                                        inquiries.addAll(updated.getOrThrow())
                                    }
                                }
                            }
                        }
                    )
                }

                AppTab.NEW -> {
                    NewInquiryScreen(
                        agents = agents,
                        onSaveAndSend = { inq, stops, selectedAgentIds, newAgentEmails ->
                            coroutineScope.launch {
                                // Resolve typed emails to real agent records before creating the inquiry.
                                // New agents are saved first so every dispatch has a valid agent ID.
                                val resolvedAgentIds = selectedAgentIds.toMutableList()
                                val resolvedAgentsById = agents.associateBy { it.id }.toMutableMap()

                                for (email in newAgentEmails
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .distinctBy { it.lowercase() }
                                ) {
                                    val lookupResult = app.agentRepository.findAgentByEmail(email)
                                    if (lookupResult.isFailure) {
                                        Toast.makeText(
                                            app,
                                            "Could not check agent email $email: ${lookupResult.exceptionOrNull()?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }

                                    var agent = lookupResult.getOrNull()

                                    if (agent == null) {
                                        val defaultName = email.substringBefore("@").ifBlank { email }
                                        val createResult = app.agentRepository.createAgent(
                                            name = defaultName,
                                            email = email,
                                            phone = ""
                                        )

                                        if (createResult.isFailure) {
                                            // Re-check in case the same email was added concurrently.
                                            val retryLookup = app.agentRepository.findAgentByEmail(email)
                                            agent = retryLookup.getOrNull()

                                            if (agent == null) {
                                                Toast.makeText(
                                                    app,
                                                    "Could not add agent $email: ${createResult.exceptionOrNull()?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@launch
                                            }
                                        } else {
                                            agent = createResult.getOrThrow()
                                        }
                                    }

                                    val resolvedAgent = agent
                                    if (resolvedAgent != null) {
                                        resolvedAgentsById[resolvedAgent.id] = resolvedAgent
                                        if (agents.none { it.id == resolvedAgent.id }) {
                                            agents.add(resolvedAgent)
                                        }
                                        if (resolvedAgentIds.none { it == resolvedAgent.id }) {
                                            resolvedAgentIds.add(resolvedAgent.id)
                                        }
                                    }
                                }

                                val result = app.inquiryRepository.createInquiry(
                                    inquiry = inq,
                                    stops = stops,
                                    agentIds = resolvedAgentIds,
                                    agentsMap = resolvedAgentsById
                                )

                                if (result.isSuccess) {
                                    val savedInquiry = result.getOrThrow()
                                    inquiries.add(0, savedInquiry)

                                    // Build email strictly without client phone!
                                    val builtEmail = EmailBuilder.buildEmail(
                                        inquiryNumber = savedInquiry.inquiryNumber,
                                        category = savedInquiry.category,
                                        clientName = savedInquiry.clientName,
                                        destination = savedInquiry.destination,
                                        pickupPoint = savedInquiry.pickupPoint,
                                        dropPoint = savedInquiry.dropPoint,
                                        dateType = savedInquiry.dateType,
                                        startDate = savedInquiry.startDate ?: "",
                                        endDate = savedInquiry.endDate ?: "",
                                        adults = savedInquiry.adults,
                                        children = savedInquiry.children,
                                        stops = savedInquiry.stops,
                                        flightTripType = savedInquiry.flightTripType,
                                        flightClass = savedInquiry.flightClass,
                                        preferredAirline = savedInquiry.preferredAirline,
                                        specialNotes = savedInquiry.specialNotes
                                    )

                                    val emailSettings = app.keystoreManager.getEmailSettings()

                                    val dispatchResults = app.smtpMailSender.dispatchBatch(
                                        dispatches = savedInquiry.dispatches,
                                        builtEmail = builtEmail,
                                        settings = emailSettings
                                    )

                                    // Refresh inquiries after email sending and status updates finish.
                                    val updated = app.inquiryRepository.getInquiries()
                                    if (updated.isSuccess) {
                                        inquiries.clear()
                                        inquiries.addAll(updated.getOrThrow())
                                    }

                                    // Refresh agents so newly added emails are available for future inquiries.
                                    val refreshedAgents = app.agentRepository.getAgents()
                                    if (refreshedAgents.isSuccess) {
                                        agents.clear()
                                        agents.addAll(refreshedAgents.getOrThrow())
                                    }

                                    val sent = dispatchResults.count { it.success }
                                    val failed = dispatchResults.count { !it.success }

                                    val message = when {
                                        dispatchResults.isEmpty() ->
                                            "Inquiry #${savedInquiry.inquiryNumber} saved, but no agents were selected."
                                        failed == 0 ->
                                            "Inquiry #${savedInquiry.inquiryNumber}: $sent email(s) sent."
                                        sent == 0 ->
                                            "Inquiry saved, but all $failed email(s) failed."
                                        else ->
                                            "Inquiry saved: $sent sent, $failed failed."
                                    }

                                    Toast.makeText(app, message, Toast.LENGTH_LONG).show()
                                    currentTab = AppTab.INQUIRIES
                                } else {
                                    Toast.makeText(
                                        app,
                                        "Failed to save inquiry: ${result.exceptionOrNull()?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                }

                AppTab.LEDGER -> {
                    LedgerScreen(
                        records = ledgerRecords,
                        agents = agents,
                        onAddRecord = { clientName, amount, date, notes ->
                            coroutineScope.launch {
                                val res = app.ledgerRepository.createLedgerRecord(clientName, amount, date, notes)
                                if (res.isSuccess) {
                                    ledgerRecords.add(0, res.getOrThrow())
                                }
                            }
                        },
                        onDeleteRecord = { recordId ->
                            coroutineScope.launch {
                                val res = app.ledgerRepository.deleteLedgerRecord(recordId)
                                if (res.isSuccess) {
                                    ledgerRecords.removeAll { it.id == recordId }
                                    Toast.makeText(app, "Ledger record deleted permanently.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(app, "Delete failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onAddSend = { recordId, agentId, agentName, amount, date, note ->
                            coroutineScope.launch {
                                val res = app.ledgerRepository.addSentAmount(recordId, agentId, agentName, amount, date, note)
                                if (res.isSuccess) {
                                    val send = res.getOrThrow()
                                    val idx = ledgerRecords.indexOfFirst { it.id == recordId }
                                    if (idx != -1) {
                                        val rec = ledgerRecords[idx]
                                        ledgerRecords[idx] = rec.copy(sends = listOf(send) + rec.sends)
                                    }
                                } else {
                                    Toast.makeText(app, "Disbursement failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onDeleteSend = { sendId ->
                            coroutineScope.launch {
                                val res = app.ledgerRepository.deleteSentAmount(sendId)
                                if (res.isSuccess) {
                                    ledgerRecords.forEachIndexed { idx, rec ->
                                        if (rec.sends.any { it.id == sendId }) {
                                            ledgerRecords[idx] = rec.copy(sends = rec.sends.filter { it.id != sendId })
                                        }
                                    }
                                }
                            }
                        },
                        onExportPdf = {
                            val pdf = app.exportManager.generateLedgerPdf(ledgerRecords)
                            app.exportManager.shareFile(pdf, "application/pdf", "Share Ledger PDF")
                        }
                    )
                }

                AppTab.AGENTS -> {
                    AgentsScreen(
                        agents = agents,
                        onAddAgent = { name, email, phone, isActive ->
                            coroutineScope.launch {
                                val res = app.agentRepository.createAgent(name, email, phone)
                                if (res.isSuccess) {
                                    agents.add(res.getOrThrow())
                                }
                            }
                        },
                        onEditAgent = { id, name, email, phone, isActive ->
                            coroutineScope.launch {
                                val res = app.agentRepository.updateAgent(id, name, email, phone, isActive)
                                if (res.isSuccess) {
                                    val idx = agents.indexOfFirst { it.id == id }
                                    if (idx != -1) agents[idx] = res.getOrThrow()
                                }
                            }
                        },
                        onDeleteAgent = { agentId ->
                            coroutineScope.launch {
                                val res = app.agentRepository.deleteAgent(agentId)
                                if (res.isSuccess) {
                                    agents.removeAll { it.id == agentId }
                                    Toast.makeText(app, "Agent deleted permanently.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(app, "Delete failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }

                AppTab.SETTINGS -> {
                    val initialEmailSettings = remember {
                        app.keystoreManager.getEmailSettings().let { saved ->
                            saved.copy(
                                gmailAddress = saved.gmailAddress.ifBlank {
                                    "bookings.ishatours@gmail.com"
                                },
                                senderDisplayName = saved.senderDisplayName.ifBlank {
                                    "Isha Tours & Travel"
                                }

                            )
                        }
                    }
                    val hasPasswordStored = remember {
                        app.keystoreManager.hasPasswordStored()
                    }

                    SettingsScreen(
                        currentOwnerEmail = "bookings.ishatours@gmail.com",
                        initialEmailSettings = initialEmailSettings,
                        hasPasswordStored = hasPasswordStored,
                        onSaveEmailSettings = { settings ->
                            coroutineScope.launch {
                                val existing = app.keystoreManager.getEmailSettings()
                                val settingsToSave = settings.copy(
                                    gmailAddress = settings.gmailAddress.trim().ifBlank {
                                        existing.gmailAddress.ifBlank {
                                            "bookings.ishatours@gmail.com"
                                        }
                                    },
                                    appPassword = settings.appPassword.trim().ifBlank {
                                        existing.appPassword
                                    },
                                    senderDisplayName = settings.senderDisplayName.trim().ifBlank {
                                        existing.senderDisplayName.ifBlank {
                                            "Isha Tours & Travel"
                                        }
                                    },
                                    replyToEmail = settings.replyToEmail.trim()
                                )
                                app.keystoreManager.saveEmailSettings(settingsToSave)
                                Toast.makeText(
                                    app,
                                    "Email settings saved. Existing password was preserved if left blank.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onSendTestEmail = { callback ->
                            coroutineScope.launch {
                                val settings = app.keystoreManager.getEmailSettings()
                                val testDispatch = Dispatch(
                                    id = "test",
                                    agentName = "Owner Test",
                                    agentEmail = settings.gmailAddress
                                )
                                val testEmail = BuiltEmail(
                                    subject = "Travel Dispatcher - SMTP Test Email",
                                    plainText = "Your Gmail SMTP setup with App Password is functioning correctly on port 465 SSL.",
                                    htmlBody = "<h2>Travel Dispatcher - SMTP Test</h2><p>Your Gmail SMTP setup with App Password is functioning correctly on port 465 SSL.</p>"
                                )
                                val res = app.smtpMailSender.dispatchBatch(listOf(testDispatch), testEmail, settings)
                                val first = res.firstOrNull()
                                if (first?.success == true) {
                                    callback(true, "Test email sent successfully to ${settings.gmailAddress}!")
                                } else {
                                    callback(false, first?.errorMessage ?: "Failed to send test email")
                                }
                            }
                        },
                        onExportBackupJson = {
                            val backupObj = JSONObject().apply {
                                put("exported_at", System.currentTimeMillis())
                                put("inquiries_count", inquiries.size)
                                put("ledger_records_count", ledgerRecords.size)
                                put("agents_count", agents.size)
                            }
                            val file = File(app.cacheDir, "travel_dispatcher_backup_${System.currentTimeMillis()}.json")
                            file.writeText(backupObj.toString(2))
                            app.exportManager.shareFile(file, "application/json", "Export Travel Dispatcher Backup")
                        },
                        onSignOut = {
                            isAuthenticated = false
                            currentTab = AppTab.INQUIRIES
                        }
                    )
                }
            }
        }
    }
}

