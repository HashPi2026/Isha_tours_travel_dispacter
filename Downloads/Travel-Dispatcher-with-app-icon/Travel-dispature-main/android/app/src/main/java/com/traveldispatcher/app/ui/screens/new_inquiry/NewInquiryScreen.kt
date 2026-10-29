package com.traveldispatcher.app.ui.screens.new_inquiry

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.data.model.Agent
import com.traveldispatcher.app.data.model.Inquiry
import com.traveldispatcher.app.data.model.InquiryStop
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.util.Patterns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInquiryScreen(
    agents: List<Agent>,
    onSaveAndSend: (
        inquiry: Inquiry,
        stops: List<InquiryStop>,
        selectedAgentIds: List<String>,
        newAgentEmails: List<String>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf("package") } // "package" or "ticket"
    var flightTripType by remember { mutableStateOf("one_way") } // "one_way" or "round_trip"

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var pickupPoint by remember { mutableStateOf("") }
    var dropPoint by remember { mutableStateOf("") }

    var dateType by remember { mutableStateOf("fixed") } // "fixed" or "flexible"
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var flexibleDateRange by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    var adults by remember { mutableStateOf("2") }
    var children by remember { mutableStateOf("0") }
    var flightClass by remember { mutableStateOf("Economy") }
    var preferredAirline by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }
    val selectedExternalEmails = remember {
        mutableStateListOf<String>()
    }

    // Stops list for Tour Package
    val stops = remember {
        mutableStateListOf(
            InquiryStop(placeName = "", nights = 1)
        )
    }

    // Selected agent IDs for dispatch - unchecked by default
    val selectedAgentIds = remember {
        mutableStateListOf<String>()
    }
    var agentSearchQuery by remember { mutableStateOf("") }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val filteredAgents = agents.filter {
        it.isActive && (
                agentSearchQuery.isBlank() ||
                        it.name.contains(agentSearchQuery, ignoreCase = true) ||
                        it.email.contains(agentSearchQuery, ignoreCase = true) ||
                        it.phone.contains(agentSearchQuery)
                )
    }

    val isPackage = category == "package"

    Box(
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
                    text = "New Travel Inquiry",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Fast intake & one-click quotation dispatching to partner agents",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            // 1. INQUIRY CATEGORY CARD
            item {
                FormCard(title = "INQUIRY CATEGORY") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryToggleButton(
                            label = "Tour Package",
                            icon = Icons.Default.Luggage,
                            isSelected = isPackage,
                            onClick = { category = "package" },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryToggleButton(
                            label = "Flight Ticket",
                            icon = Icons.Default.Flight,
                            isSelected = !isPackage,
                            onClick = { category = "ticket" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isPackage) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = flightTripType == "one_way",
                                onClick = { flightTripType = "one_way" },
                                colors = RadioButtonDefaults.colors(selectedColor = Slate900)
                            )
                            Text(
                                text = "One-Way (Single)",
                                fontSize = 13.sp,
                                color = Slate800,
                                modifier = Modifier.clickable { flightTripType = "one_way" }
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            RadioButton(
                                selected = flightTripType == "round_trip",
                                onClick = { flightTripType = "round_trip" },
                                colors = RadioButtonDefaults.colors(selectedColor = Slate900)
                            )
                            Text(
                                text = "Round-Trip (Going & Return)",
                                fontSize = 13.sp,
                                color = Slate800,
                                modifier = Modifier.clickable { flightTripType = "round_trip" }
                            )
                        }
                    }
                }
            }

            // 2. CLIENT & DESTINATION DETAILS
            item {
                FormCard(title = "CLIENT & DESTINATION DETAILS") {
                    FormLabel(text = "Client Name *")
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        placeholder = { Text("e.g. Ramesh Patel", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormLabel(text = "Client Phone *")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Never shared",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald600
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        placeholder = { Text("+91 98765 43210", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FormLabel(text = "Destination *")

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        placeholder = {
                            Text(
                                text = if (dateType == "flexible")
                                    "Describe preferred destinations or options (e.g. Goa or Kerala; open to suggestions)"
                                else
                                    "e.g. Goa, Dubai, Kashmir, Singapore",
                                color = Slate400
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
//                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FormLabel(text = "Pickup Point")
                    OutlinedTextField(
                        value = pickupPoint,
                        onValueChange = { pickupPoint = it },
                        placeholder = { Text("e.g. Mumbai Airport / Ahmedabad", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FormLabel(text = "Drop Point")
                    OutlinedTextField(
                        value = dropPoint,
                        onValueChange = { dropPoint = it },
                        placeholder = { Text("e.g. Hotel Resort / Airport", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )
                }
            }

            // 3. STOPS & NIGHT STAYS (Only for Tour Package)
            if (isPackage) {
                item {
                    FormCard(
                        title = "STOPS & NIGHT STAYS",
                        headerAction = {
                            OutlinedButton(
                                onClick = {
                                    stops.add(InquiryStop(placeName = "", nights = 1))
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Slate900
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Slate200)
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Stop", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    ) {
                        Text(
                            text = "Add places and nights for this tour",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        stops.forEachIndexed { index, stop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    modifier = Modifier.width(22.dp)
                                )

                                OutlinedTextField(
                                    value = stop.placeName,
                                    onValueChange = { newName ->
                                        stops[index] = stop.copy(placeName = newName)
                                    },
                                    placeholder = { Text("Place / City (e.g. North Goa)", color = Slate400, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = formInputColors()
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = stop.nights.toString(),
                                    onValueChange = { n ->
                                        val parsed = n.toIntOrNull() ?: 1
                                        stops[index] = stop.copy(nights = parsed.coerceAtLeast(0))
                                    },
                                    modifier = Modifier.width(60.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = formInputColors()
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nts", fontSize = 12.sp, color = Slate600)

                                if (stops.size > 1) {
                                    IconButton(
                                        onClick = { stops.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Slate400, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. DATES
            item {
                FormCard(title = "DATES") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = dateType == "fixed",
                            onClick = { dateType = "fixed" },
                            colors = RadioButtonDefaults.colors(selectedColor = Slate900)
                        )
                        Text(
                            text = "Fixed Dates",
                            fontSize = 13.sp,
                            color = Slate800,
                            modifier = Modifier.clickable { dateType = "fixed" }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = dateType == "flexible",
                            onClick = { dateType = "flexible" },
                            colors = RadioButtonDefaults.colors(selectedColor = Slate900)
                        )
                        Text(
                            text = "Anytime Between (Flexible)",
                            fontSize = 13.sp,
                            color = Slate800,
                            modifier = Modifier.clickable { dateType = "flexible" }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (dateType == "fixed") {
                        FormLabel(text = "Start Date")
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            placeholder = { Text("YYYY-MM-DD (e.g. 2027-01-18)", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formInputColors(),
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Choose start date",
                                        tint = Slate600
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FormLabel(text = "End Date")
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            placeholder = { Text("YYYY-MM-DD (e.g. 2027-01-31)", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formInputColors(),
                            trailingIcon = {
                                IconButton(onClick = { showEndDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Choose end date",
                                        tint = Slate600
                                    )
                                }
                            }
                        )
                    } else {
                        FormLabel(text = "Travel Date Range / Details")
                        OutlinedTextField(
                            value = flexibleDateRange,
                            onValueChange = { flexibleDateRange = it },
                            placeholder = { Text("e.g. Anytime between 18 Jan 2027 and 31 Jan 2027, or 1st week of March", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formInputColors()
                        )
                    }
                }
            }

            // 5. PASSENGERS & PREFERENCES
            item {
                FormCard(title = "PASSENGERS & PREFERENCES") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Adults (12+ yrs) *")
                            OutlinedTextField(
                                value = adults,
                                onValueChange = { adults = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = formInputColors()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Children")
                            OutlinedTextField(
                                value = children,
                                onValueChange = { children = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = formInputColors()
                            )
                        }
                    }

                    if (!isPackage) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FormLabel(text = "Flight Class (Optional)")

                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = !classDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = flightClass,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(10.dp),
                                colors = formInputColors()
                            )
                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                listOf("Economy", "Premium Economy", "Business", "First Class").forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            flightClass = item
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FormLabel(text = "Preferred Airline (Optional)")
                        OutlinedTextField(
                            value = preferredAirline,
                            onValueChange = { preferredAirline = it },
                            placeholder = { Text("e.g. IndiGo, Air India, Emirates", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formInputColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FormLabel(text = "Special Notes / Requests (Optional)")
                    OutlinedTextField(
                        value = specialNotes,
                        onValueChange = { specialNotes = it },
                        placeholder = { Text("e.g. 4-star beach resort preferred, vegetarian meals, airport transfer required", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = formInputColors()
                    )
                }
            }

            // 6. DISPATCH TO AGENTS
            item {
                FormCard(
                    title = "DISPATCH TO AGENTS (${selectedAgentIds.size + selectedExternalEmails.size} SELECTED)",
                    headerAction = {
                        val activeAgents = agents.filter { it.isActive }
                        val isAllSelected = activeAgents.isNotEmpty() && selectedAgentIds.size == activeAgents.size
                        Text(
                            text = if (isAllSelected) "Deselect All" else "Select All",
                            color = Color(0xFF2563EB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (isAllSelected) {
                                    selectedAgentIds.clear()
                                } else {
                                    selectedAgentIds.clear()
                                    selectedAgentIds.addAll(activeAgents.map { it.id })
                                }
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Separate email sent to each agent via Gmail SMTP",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = agentSearchQuery,
                        onValueChange = { agentSearchQuery = it },
                        placeholder = { Text("Search active agents...", color = Slate400, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = formInputColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    filteredAgents.forEach { agent ->
                        val isChecked = selectedAgentIds.contains(agent.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedAgentIds.remove(agent.id)
                                    else selectedAgentIds.add(agent.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox box
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(if (isChecked) Slate900 else White, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isChecked) Slate900 else Slate400, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = agent.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate900)
                                Text(text = agent.email, fontSize = 12.sp, color = Slate500)
                            }

                            Text(text = agent.phone, fontSize = 12.sp, color = Slate400)
                        }
                    }
                    val typedEmail = agentSearchQuery.trim()

                    val isValidEmail = Patterns.EMAIL_ADDRESS
                        .matcher(typedEmail)
                        .matches()

                    val emailAlreadyExists = agents.any {
                        it.email.trim().equals(typedEmail, ignoreCase = true)
                    }

                    if (
                        isValidEmail &&
                        !emailAlreadyExists &&
                        !selectedExternalEmails.any {
                            it.equals(typedEmail, ignoreCase = true)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExternalEmails.add(typedEmail)
                                    agentSearchQuery = ""
                                    validationError = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Add new agent",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )

                                Text(
                                    text = typedEmail,
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    selectedExternalEmails.toList().forEach { email ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = email,
                                    fontSize = 13.sp,
                                    color = Slate800
                                )
                                Text(
                                    text = "New agent · Will be saved for future inquiries",
                                    fontSize = 11.sp,
                                    color = Emerald600
                                )
                            }
                            IconButton(
                                onClick = { selectedExternalEmails.remove(email) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $email",
                                    tint = Slate500
                                )
                            }
                        }
                    }
                }
            }

            if (validationError != null) {
                item {
                    Text(
                        text = validationError ?: "",
                        color = Rose600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Sticky Save & Send Button at bottom
            item {
                Button(
                    onClick = {
                        if (clientName.isBlank() || clientPhone.isBlank() || destination.isBlank()) {
                            validationError = "Please fill in Client Name, Client Phone, and Destination."
                            return@Button
                        }
                        if (selectedAgentIds.isEmpty() && selectedExternalEmails.isEmpty()) {
                            validationError = "Please select at least one agent or add an email address."
                            return@Button
                        }

                        validationError = null
                        isSubmitting = true

                        val inq = Inquiry(
                            category = category,
                            clientName = clientName.trim(),
                            clientPhone = clientPhone.trim(),
                            destination = destination.trim(),
                            pickupPoint = pickupPoint.trim(),
                            dropPoint = dropPoint.trim(),
                            dateType = dateType,
                            startDate = if (dateType == "fixed" && startDate.isNotBlank()) startDate.trim() else null,
                            endDate = if (dateType == "fixed" && endDate.isNotBlank()) endDate.trim() else null,
                            flexibleDateRange = if (dateType == "flexible") flexibleDateRange.trim().ifBlank { null } else null,
                            adults = adults.toIntOrNull() ?: 2,
                            children = children.toIntOrNull() ?: 0,
                            flightTripType = if (!isPackage) flightTripType else null,
                            flightClass = if (!isPackage) flightClass else null,
                            preferredAirline = if (!isPackage && preferredAirline.isNotBlank()) preferredAirline.trim() else null,
                            specialNotes = specialNotes.trim().ifBlank { null },
                            status = "inquiry"
                        )

                        val validStops = if (isPackage) {
                            stops.filter { it.placeName.isNotBlank() }
                        } else emptyList()

                        onSaveAndSend(inq, validStops, selectedAgentIds.toList(), selectedExternalEmails.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate900,
                        contentColor = White
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Save & Send to Agents",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            startDate = formatter.format(Date(millis))
                        }
                        showStartDatePicker = false
                    }) {
                        Text("OK", color = Slate900)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancel", color = Slate900)
                    }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            endDate = formatter.format(Date(millis))
                        }
                        showEndDatePicker = false
                    }) {
                        Text("OK", color = Slate900)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel", color = Slate900)
                    }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(16.dp))
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                headerAction?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CategoryToggleButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(if (isSelected) Slate900 else White, RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) Slate900 else Slate200, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) White else Slate700,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) White else Slate700
            )
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Slate800,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun formInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Slate900,
    unfocusedBorderColor = Slate200,
    focusedContainerColor = White,
    unfocusedContainerColor = White
)

private val Slate700 = Color(0xFF334155)
