package com.traveldispatcher.app.ui.screens.inquiries

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.data.model.Dispatch
import com.traveldispatcher.app.data.model.Inquiry
import com.traveldispatcher.app.ui.components.PermanentDeleteDialog
import com.traveldispatcher.app.ui.theme.Amber100
import com.traveldispatcher.app.ui.theme.Amber600
import com.traveldispatcher.app.ui.theme.Emerald100
import com.traveldispatcher.app.ui.theme.Emerald600
import com.traveldispatcher.app.ui.theme.Rose100
import com.traveldispatcher.app.ui.theme.Rose600
import com.traveldispatcher.app.ui.theme.Slate100
import com.traveldispatcher.app.ui.theme.Slate200
import com.traveldispatcher.app.ui.theme.Slate400
import com.traveldispatcher.app.ui.theme.Slate50
import com.traveldispatcher.app.ui.theme.Slate500
import com.traveldispatcher.app.ui.theme.Slate800
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.Slate600
import com.traveldispatcher.app.ui.theme.White

@Composable
fun InquiriesScreen(
    inquiries: List<Inquiry>,
    onNewInquiryClick: () -> Unit,
    onStatusChange: (inquiryId: String, newStatus: String) -> Unit,
    onDeleteInquiry: (inquiryId: String) -> Unit,
    onExportPdf: () -> Unit,
    onRetryDispatch: (Dispatch) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("all") } // "all", "inquiry", "confirmed", "cancelled"
    var selectedTypeFilter by remember { mutableStateOf("all") } // "all", "package", "ticket"

    var selectedInquiryForSheet by remember { mutableStateOf<Inquiry?>(null) }
    var inquiryToDelete by remember { mutableStateOf<Inquiry?>(null) }

    val totalCount = inquiries.size
    val pendingCount = inquiries.count { it.status == "inquiry" }
    val confirmedCount = inquiries.count { it.status == "confirmed" }
    val cancelledCount = inquiries.count { it.status == "cancelled" }

    // Filter inquiries
    val filteredInquiries = inquiries.filter { inq ->
        val matchesSearch = searchQuery.isBlank() ||
                inq.clientName.contains(searchQuery, ignoreCase = true) ||
                inq.destination.contains(searchQuery, ignoreCase = true) ||
                inq.inquiryNumber.toString().contains(searchQuery) ||
                inq.clientPhone.contains(searchQuery)

        val matchesStatus = when (selectedStatusFilter) {
            "inquiry" -> inq.status == "inquiry"
            "confirmed" -> inq.status == "confirmed"
            "cancelled" -> inq.status == "cancelled"
            else -> true
        }

        val matchesType = when (selectedTypeFilter) {
            "package" -> inq.category == "package"
            "ticket" -> inq.category == "ticket"
            else -> true
        }

        matchesSearch && matchesStatus && matchesType
    }

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
                // Title & Subtitle
                Text(
                    text = "Travel Inquiries",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Search inquiries, filter by status, and track dispatch logs",
                    fontSize = 13.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row (Export PDF & New Inquiry)
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
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export PDF ($totalCount)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onNewInquiryClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
                            contentColor = White,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF6B7280)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ New Inquiry",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 Metric Summary Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "ALL INQUIRIES",
                            count = totalCount.toString(),
                            icon = Icons.Default.Layers,
                            isDark = true,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "PENDING (I)",
                            count = pendingCount.toString(),
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "CONFIRMED (C)",
                            count = confirmedCount.toString(),
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "CANCELLED (X)",
                            count = cancelledCount.toString(),
                            icon = Icons.Default.Cancel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search by client name, destination, inquiry #, ph...",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate900,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        label = "All Statuses",
                        isSelected = selectedStatusFilter == "all",
                        onClick = { selectedStatusFilter = "all" }
                    )
                    FilterPill(
                        label = "Pending / Inquiry ($pendingCount)",
                        isSelected = selectedStatusFilter == "inquiry",
                        onClick = { selectedStatusFilter = "inquiry" }
                    )
                    FilterPill(
                        label = "Confirmed ($confirmedCount)",
                        isSelected = selectedStatusFilter == "confirmed",
                        onClick = { selectedStatusFilter = "confirmed" }
                    )
                    FilterPill(
                        label = "Cancelled ($cancelledCount)",
                        isSelected = selectedStatusFilter == "cancelled",
                        onClick = { selectedStatusFilter = "cancelled" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type Filter Sub-row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Types",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTypeFilter == "all") FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTypeFilter == "all") Slate900 else Slate500,
                        modifier = Modifier.clickable { selectedTypeFilter = "all" }
                    )
                    Text(text = "  ·  ", fontSize = 12.sp, color = Slate400)
                    Text(
                        text = "Packages",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTypeFilter == "package") FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTypeFilter == "package") Slate900 else Slate500,
                        modifier = Modifier.clickable { selectedTypeFilter = "package" }
                    )
                    Text(text = "  ·  ", fontSize = 12.sp, color = Slate400)
                    Text(
                        text = "Tickets",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTypeFilter == "ticket") FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTypeFilter == "ticket") Slate900 else Slate500,
                        modifier = Modifier.clickable { selectedTypeFilter = "ticket" }
                    )
                }
            }

            // Inquiry Cards List
            items(filteredInquiries, key = { it.id }) { inq ->
                InquiryCard(
                    inquiry = inq,
                    onStatusChange = { newStatus -> onStatusChange(inq.id, newStatus) },
                    onDelete = { inquiryToDelete = inq },
                    onViewDispatches = { selectedInquiryForSheet = inq }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dispatch status bottom sheet
    selectedInquiryForSheet?.let { inq ->
        DispatchStatusBottomSheet(
            inquiry = inq,
            onDismiss = { selectedInquiryForSheet = null },
            onRetryDispatch = { dispatch -> onRetryDispatch(dispatch) },
            onViewEmailHtml = { /* Handled in detail */ }
        )
    }

    // Delete Confirmation Dialog
    inquiryToDelete?.let { inq ->
        PermanentDeleteDialog(
            title = "Delete Inquiry #${inq.inquiryNumber}?",
            message = "This will permanently delete this inquiry, its stops, and all dispatch records from Supabase. Any linked ledger records will be preserved with the inquiry link cleared.",
            onConfirm = {
                onDeleteInquiry(inq.id)
                inquiryToDelete = null
            },
            onDismiss = { inquiryToDelete = null }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    count: String,
    icon: ImageVector,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isDark) Slate900 else White, RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Slate900 else Slate200, RoundedCornerShape(12.dp))
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
                    color = if (isDark) Color(0xFFCBD5E1) else Slate500
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFCBD5E1) else Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) White else Slate900
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Slate900 else Color(0xFFE2E8F0),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) White else Slate700
        )
    }
}

@Composable
private fun InquiryCard(
    inquiry: Inquiry,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onViewDispatches: () -> Unit
) {
    val isPackage = inquiry.category.equals("package", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Icon + #1043 Destination + Status Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Box
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate100, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPackage) Icons.Default.Luggage else Icons.Default.Flight,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${inquiry.inquiryNumber}  ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                    Text(
                        text = inquiry.destination,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                // Status Indicator
                when (inquiry.status) {
                    "inquiry" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(6.dp)
                                .background(Amber600, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inquiry (I)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Amber600)
                        }
                    }
                    "confirmed" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(6.dp)
                                .background(Emerald600, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirmed (C)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald600)
                        }
                    }
                    "cancelled" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(6.dp)
                                .background(Rose600, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelled (x)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Rose600)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client & Phone
            Text(
                text = "Client: ${inquiry.clientName} · Phone: ${inquiry.clientPhone}",
                fontSize = 13.sp,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Route & Dates & Pax
            val datesText = if (inquiry.dateType == "flexible") {
                inquiry.flexibleDateRange ?: if (!inquiry.startDate.isNullOrBlank() && !inquiry.endDate.isNullOrBlank()) {
                    "Anytime: ${inquiry.startDate} to ${inquiry.endDate}"
                } else "Flexible dates"
            } else {
                if (!inquiry.startDate.isNullOrBlank() && !inquiry.endDate.isNullOrBlank()) {
                    "${inquiry.startDate} to ${inquiry.endDate}"
                } else (inquiry.startDate ?: "Dates to be decided")
            }

            val paxText = buildString {
                append("${inquiry.adults} Adult${if (inquiry.adults > 1) "s" else ""}")
                if (inquiry.children > 0) append(", ${inquiry.children} Child${if (inquiry.children > 1) "ren" else ""}")
            }

            val routeText = if (inquiry.pickupPoint.isNotBlank() && inquiry.dropPoint.isNotBlank()) {
                "${inquiry.pickupPoint} → ${inquiry.dropPoint}"
            } else if (inquiry.pickupPoint.isNotBlank()) {
                "From: ${inquiry.pickupPoint}"
            } else if (inquiry.dropPoint.isNotBlank()) {
                "To: ${inquiry.dropPoint}"
            } else null

            val detailsList = listOfNotNull(
                routeText,
                datesText,
                paxText,
                if (!isPackage && !inquiry.flightClass.isNullOrBlank()) inquiry.flightClass else null
            )

            Text(
                text = detailsList.joinToString("  ·  "),
                fontSize = 12.sp,
                color = Slate500,
                lineHeight = 16.sp
            )

            // Stops container (if package and stops exist)
            if (isPackage && inquiry.stops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val stopsStr = inquiry.stops.joinToString(" → ") { "${it.placeName} (${it.nights}N)" }
                    Text(
                        text = "Stops: $stopsStr",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dispatched count + View Dispatch Log Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dispatched to ${inquiry.dispatches.size} agent(s)  ",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Row(
                    modifier = Modifier.clickable { onViewDispatches() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Dispatch Log & Preview",
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Status Buttons Row: Status: [I (Pending)] [C (Confirmed)] [x (Cancelled)] [Trash]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Status:",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.padding(end = 2.dp)
                )

                // I (Pending)
                StatusButton(
                    label = "I (Pending)",
                    isSelected = inquiry.status == "inquiry",
                    activeBg = Amber100,
                    activeBorder = Amber600,
                    activeText = Amber600,
                    onClick = { onStatusChange("inquiry") }
                )

                // C (Confirmed)
                StatusButton(
                    label = "C (Confirmed)",
                    isSelected = inquiry.status == "confirmed",
                    activeBg = Emerald100,
                    activeBorder = Emerald600,
                    activeText = Emerald600,
                    onClick = { onStatusChange("confirmed") }
                )

                // x (Cancelled)
                StatusButton(
                    label = "x (Cancelled)",
                    isSelected = inquiry.status == "cancelled",
                    activeBg = Rose100,
                    activeBorder = Rose600,
                    activeText = Rose600,
                    onClick = { onStatusChange("cancelled") }
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Inquiry",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeBorder: Color,
    activeText: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) activeBg else Slate50,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) activeBorder else Slate200,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) activeText else Slate800
        )
    }
}

private val Slate700 = Color(0xFF334155)
