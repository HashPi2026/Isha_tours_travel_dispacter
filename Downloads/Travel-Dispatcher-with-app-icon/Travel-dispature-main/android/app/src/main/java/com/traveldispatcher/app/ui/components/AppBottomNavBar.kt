package com.traveldispatcher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.ui.theme.Slate200
import com.traveldispatcher.app.ui.theme.Slate400
import com.traveldispatcher.app.ui.theme.Slate500
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.White

enum class AppTab(val route: String, val label: String) {
    INQUIRIES("inquiries", "Inquiries"),
    NEW("new", "New"),
    LEDGER("ledger", "Ledger"),
    AGENTS("agents", "Agents"),
    SETTINGS("settings", "Settings")
}

@Composable
fun AppBottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(White)
            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(0.dp))
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "Inquiries",
                icon = Icons.Outlined.Description,
                isSelected = currentTab == AppTab.INQUIRIES,
                onClick = { onTabSelected(AppTab.INQUIRIES) }
            )

            // Center Highlighted "New" Tab
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(AppTab.NEW) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (currentTab == AppTab.NEW) Slate900 else Slate900.copy(alpha = 0.9f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Inquiry",
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "New",
                        fontSize = 11.sp,
                        fontWeight = if (currentTab == AppTab.NEW) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTab == AppTab.NEW) Slate900 else Slate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            NavBarItem(
                label = "Ledger",
                icon = Icons.Outlined.MenuBook,
                isSelected = currentTab == AppTab.LEDGER,
                onClick = { onTabSelected(AppTab.LEDGER) }
            )

            NavBarItem(
                label = "Agents",
                icon = Icons.Outlined.People,
                isSelected = currentTab == AppTab.AGENTS,
                onClick = { onTabSelected(AppTab.AGENTS) }
            )

            NavBarItem(
                label = "Settings",
                icon = Icons.Outlined.Settings,
                isSelected = currentTab == AppTab.SETTINGS,
                onClick = { onTabSelected(AppTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Slate900 else Slate400,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Slate900 else Slate500,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
