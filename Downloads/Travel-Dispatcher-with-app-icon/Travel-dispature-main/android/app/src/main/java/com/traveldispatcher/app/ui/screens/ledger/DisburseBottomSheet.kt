package com.traveldispatcher.app.ui.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.data.model.Agent
import com.traveldispatcher.app.data.model.LedgerRecord
import com.traveldispatcher.app.ui.theme.Emerald600
import com.traveldispatcher.app.ui.theme.Rose600
import com.traveldispatcher.app.ui.theme.Slate200
import com.traveldispatcher.app.ui.theme.Slate400
import com.traveldispatcher.app.ui.theme.Slate50
import com.traveldispatcher.app.ui.theme.Slate500
import com.traveldispatcher.app.ui.theme.Slate800
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.Slate600
import com.traveldispatcher.app.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisburseBottomSheet(
    record: LedgerRecord,
    agents: List<Agent>,
    onDismiss: () -> Unit,
    onAddSentAmount: (
        agentId: String?,
        agentName: String,
        amountSent: Double,
        disbursementDate: String,
        note: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val maxAvailable = record.balanceHeld
    var selectedAgent by remember { mutableStateOf<Agent?>(null) }
    var agentNameInput by remember { mutableStateOf("") }
    var amountSentInput by remember { mutableStateOf(if (maxAvailable > 0) String.format(Locale.US, "%.0f", maxAvailable) else "0") }
    var disbursementDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var noteInput by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Disburse to Agent · ${record.clientName}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Client (Locked)
            Text(
                text = "Client (Locked)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = record.clientName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate200,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = Slate50,
                    unfocusedContainerColor = Slate50
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Select Partner Agent or Type Name
            Text(
                text = "Select Partner Agent or Type Name *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAgent?.name ?: "-- Choose from saved directory --",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate900,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    agents.filter { it.isActive }.forEach { agent ->
                        DropdownMenuItem(
                            text = { Text("${agent.name} (${agent.email})") },
                            onClick = {
                                selectedAgent = agent
                                agentNameInput = agent.name
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = agentNameInput,
                onValueChange = {
                    agentNameInput = it
                    if (selectedAgent?.name != it) selectedAgent = null
                },
                placeholder = { Text("Agent or Supplier Name (e.g. Apex Holidays)", color = Slate400, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Sent with Prefilled max available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount Sent (Rs.) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
                Text(
                    text = "Prefilled: Max available Rs. %,.0f".format(maxAvailable),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Emerald600,
                    modifier = Modifier.clickable {
                        amountSentInput = String.format(Locale.US, "%.0f", maxAvailable)
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = amountSentInput,
                onValueChange = {
                    amountSentInput = it
                    val parsed = it.toDoubleOrNull() ?: 0.0
                    errorMessage = if (parsed > maxAvailable) {
                        "Sent total exceeds received amount (Max allowed: Rs. %,.0f)".format(maxAvailable)
                    } else null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (errorMessage != null) Rose600 else Slate900,
                    unfocusedBorderColor = if (errorMessage != null) Rose600 else Slate200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Rose600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Disbursement Date
            Text(
                text = "Disbursement Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = disbursementDate,
                onValueChange = { disbursementDate = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Note
            Text(
                text = "Note",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("e.g. Hotel advance payment", color = Slate400, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel", color = Slate600, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val amount = amountSentInput.toDoubleOrNull() ?: 0.0
                        if (agentNameInput.isBlank()) {
                            errorMessage = "Please enter or select an agent name."
                            return@Button
                        }
                        if (amount <= 0) {
                            errorMessage = "Amount must be greater than zero."
                            return@Button
                        }
                        if (amount > maxAvailable) {
                            errorMessage = "Sent total exceeds received amount."
                            return@Button
                        }

                        onAddSentAmount(
                            selectedAgent?.id,
                            agentNameInput.trim(),
                            amount,
                            disbursementDate.trim(),
                            noteInput.trim().ifBlank { null }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate900,
                        contentColor = White
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(text = "Add Sent Amount", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
