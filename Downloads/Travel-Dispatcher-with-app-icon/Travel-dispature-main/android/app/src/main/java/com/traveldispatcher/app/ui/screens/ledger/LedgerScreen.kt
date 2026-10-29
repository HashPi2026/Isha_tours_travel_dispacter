package com.traveldispatcher.app.ui.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.data.model.Agent
import com.traveldispatcher.app.data.model.LedgerRecord
import com.traveldispatcher.app.data.model.LedgerSend
import com.traveldispatcher.app.ui.components.PermanentDeleteDialog
import com.traveldispatcher.app.ui.theme.Emerald600
import com.traveldispatcher.app.ui.theme.Rose600
import com.traveldispatcher.app.ui.theme.Slate100
import com.traveldispatcher.app.ui.theme.Slate200
import com.traveldispatcher.app.ui.theme.Slate400
import com.traveldispatcher.app.ui.theme.Slate50
import com.traveldispatcher.app.ui.theme.Slate500
import com.traveldispatcher.app.ui.theme.Slate600
import com.traveldispatcher.app.ui.theme.Slate800
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.White

@Composable
fun LedgerScreen(
    records: List<LedgerRecord>,
    agents: List<Agent>,
    onAddRecord: (clientName: String, amount: Double, date: String, notes: String?) -> Unit,
    onDeleteRecord: (recordId: String) -> Unit,
    onAddSend: (recordId: String, agentId: String?, agentName: String, amount: Double, date: String, note: String?) -> Unit,
    onDeleteSend: (sendId: String) -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var activeRecordForDisburse by remember { mutableStateOf<LedgerRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<LedgerRecord?>(null) }
    var sendToDelete by remember { mutableStateOf<LedgerSend?>(null) }

    val totalReceived = records.sumOf { it.receivedAmount }
    val totalSent = records.sumOf { it.totalSent }
    val balanceHeld = (totalReceived - totalSent).coerceAtLeast(0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Khatabook Middleman Ledger",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Track client money held, record agent disbursements, and prevent over-sending",
                    fontSize = 13.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = White,
                            contentColor = Slate900
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Slate200)
                        ),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Export PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showAddMoneyDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = " Money Received", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Summary Stat Cards (matching screenshot 06)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // TOTAL RECEIVED
                    StatCard(
                        label = "TOTAL RECEIVED",
                        amountText = "Rs.  %,.0f".format(totalReceived),
                        subtitle = "From all client bookings",
                        icon = Icons.Default.ArrowDownward,
                        iconTint = Emerald600,
                        amountColor = Slate900
                    )

                    // TOTAL SENT
                    StatCard(
                        label = "TOTAL SENT",
                        amountText = "Rs.  %,.0f".format(totalSent),
                        subtitle = "Disbursed to travel agents",
                        icon = Icons.Default.ArrowUpward,
                        iconTint = Rose600,
                        amountColor = Slate900
                    )

                    // BALANCE WITH ME
                    StatCard(
                        label = "BALANCE WITH ME",
                        amountText = "Rs.  %,.0f".format(balanceHeld),
                        subtitle = "Net funds held",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF2563EB),
                        amountColor = Emerald600
                    )
                }
            }

            // Ledger Records List
            items(records, key = { it.id }) { record ->
                LedgerRecordCard(
                    record = record,
                    onDisburseClick = { activeRecordForDisburse = record },
                    onDeleteRecord = { recordToDelete = record },
                    onDeleteSend = { send -> sendToDelete = send }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Disburse Bottom Sheet
    activeRecordForDisburse?.let { rec ->
        DisburseBottomSheet(
            record = rec,
            agents = agents,
            onDismiss = { activeRecordForDisburse = null },
            onAddSentAmount = { agentId, agentName, amount, date, note ->
                onAddSend(rec.id, agentId, agentName, amount, date, note)
                activeRecordForDisburse = null
            }
        )
    }

    // Money Received Dialog
    if (showAddMoneyDialog) {
        MoneyReceivedDialog(
            onDismiss = { showAddMoneyDialog = false },
            onSave = { clientName, amount, date, notes ->
                onAddRecord(clientName, amount, date, notes)
                showAddMoneyDialog = false
            }
        )
    }

    // Delete Record Confirmation
    recordToDelete?.let { rec ->
        PermanentDeleteDialog(
            title = "Delete Ledger Record for ${rec.clientName}?",
            message = "Warning: This will permanently delete this client's ledger entry AND all ${rec.sends.size} associated agent disbursement records from Supabase.",
            onConfirm = {
                onDeleteRecord(rec.id)
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null }
        )
    }

    // Delete Single Send Confirmation
    sendToDelete?.let { send ->
        PermanentDeleteDialog(
            title = "Delete Disbursement of Rs. %,.0f?".format(send.amountSent),
            message = "This will permanently remove the disbursement to ${send.agentName} and restore the held balance.",
            onConfirm = {
                onDeleteSend(send.id)
                sendToDelete = null
            },
            onDismiss = { sendToDelete = null }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    amountText: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    amountColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amountText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500
            )
        }
    }
}

@Composable
private fun LedgerRecordCard(
    record: LedgerRecord,
    onDisburseClick: () -> Unit,
    onDeleteRecord: () -> Unit,
    onDeleteSend: (LedgerSend) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header: Client Name + Edit/Trash icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDeleteRecord, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Subline: Date + Note
            val notesPart = if (!record.notes.isNullOrBlank()) "  ·  ${record.notes}" else ""
            Text(
                text = "Date: ${record.receivedDate}$notesPart",
                fontSize = 12.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Box: RECEIVED | SENT TOTAL | BALANCE HELD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate50, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "RECEIVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Rs.  %,.0f".format(record.receivedAmount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SENT TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Rs.  %,.0f".format(record.totalSent), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Rose600)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "BALANCE HELD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Rs.  %,.0f".format(record.balanceHeld), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-section: DISBURSED TO AGENTS (X) + Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISBURSED TO AGENTS (${record.sends.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate600
                )

                Button(
                    onClick = onDisburseClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate900,
                        contentColor = White
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+ Send", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sends list or empty container
            if (record.sends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No funds disbursed to agents yet. Click \"+ Send\" to pay an agent.",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    record.sends.forEach { send ->
                        DisbursedRow(send = send, onDelete = { onDeleteSend(send) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DisbursedRow(
    send: LedgerSend,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50, RoundedCornerShape(8.dp))
            .border(1.dp, Slate200, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = send.agentName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
                Text(
                    text = send.disbursementDate,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Text(
                text = "Rs.  %,.0f".format(send.amountSent),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Rose600,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
            }
        }
    }
}
