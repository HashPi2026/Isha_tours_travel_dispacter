package com.traveldispatcher.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.data.model.EmailSettings
import com.traveldispatcher.app.ui.theme.Emerald600
import com.traveldispatcher.app.ui.theme.Rose600
import com.traveldispatcher.app.ui.theme.Slate200
import com.traveldispatcher.app.ui.theme.Slate400
import com.traveldispatcher.app.ui.theme.Slate50
import com.traveldispatcher.app.ui.theme.Slate500
import com.traveldispatcher.app.ui.theme.Slate600
import com.traveldispatcher.app.ui.theme.Slate800
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentOwnerEmail: String,
    initialEmailSettings: EmailSettings,
    hasPasswordStored: Boolean,
    onSaveEmailSettings: (EmailSettings) -> Unit,
    onSendTestEmail: (onResult: (success: Boolean, message: String) -> Unit) -> Unit,
    onExportBackupJson: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gmailAddress by remember { mutableStateOf(initialEmailSettings.gmailAddress) }
    var appPassword by remember { mutableStateOf("") }
    var senderDisplayName by remember { mutableStateOf(initialEmailSettings.senderDisplayName) }
    var replyToEmail by remember { mutableStateOf(initialEmailSettings.replyToEmail) }

    var saveFeedback by remember { mutableStateOf<String?>(null) }
    var testEmailFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isSendingTest by remember { mutableStateOf(false) }

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
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage owner account, Gmail SMTP configuration, and data exports",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            // 1. SIGNED-IN ACCOUNT CARD
            item {
                SettingsCard(title = "SIGNED-IN OWNER ACCOUNT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Authenticated as:",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentOwnerEmail.ifBlank { "bookings.ishatours@gmail.com" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Rose600
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFECDD3))
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign out", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 2. EMAIL ACCOUNT (Gmail SMTP + Keystore)
            item {
                SettingsCard(title = "EMAIL ACCOUNT (GMAIL SMTP)") {
                    Text(
                        text = "Emails are sent directly from your device to partner agents over secure port 465 SSL. Your App Password is stored encrypted in the hardware-backed Android Keystore and is never displayed after saving.",
                        fontSize = 12.sp,
                        color = Slate500,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Gmail Address *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = gmailAddress,
                        onValueChange = { gmailAddress = it },
                        placeholder = { Text("e.g. yourname@gmail.com", color = Slate400, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = settingsInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Gmail App Password *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                        if (hasPasswordStored) {
                            Text(
                                text = "Keystore Encrypted (Saved)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Emerald600
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = appPassword,
                        onValueChange = { appPassword = it },
                        placeholder = {
                            Text(
                                if (hasPasswordStored) "•••••••••••••••• (Leave blank to keep current)" else "16-character App Password (e.g. abcd efgh ijkl mnop)",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = settingsInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Sender Display Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = senderDisplayName,
                        onValueChange = { senderDisplayName = it },
                        placeholder = { Text("e.g. Travel Dispatcher / Jaykumar Mistry", color = Slate400, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = settingsInputColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Reply-To Address (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = replyToEmail,
                        onValueChange = { replyToEmail = it },
                        placeholder = { Text("e.g. quotes@yourcompany.com", color = Slate400, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = settingsInputColors()
                    )

                    if (saveFeedback != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = saveFeedback ?: "", color = Emerald600, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveEmailSettings(
                                    EmailSettings(
                                        gmailAddress = gmailAddress.trim(),
                                        appPassword = appPassword.trim(),
                                        senderDisplayName = senderDisplayName.trim().ifBlank { "Travel Dispatcher" },
                                        replyToEmail = replyToEmail.trim()
                                    )
                                )
                                saveFeedback = "Email configuration saved securely in Android Keystore."
                                appPassword = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate900,
                                contentColor = White
                            ),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text("Save Email Settings", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                isSendingTest = true
                                testEmailFeedback = null
                                onSendTestEmail { success, msg ->
                                    isSendingTest = false
                                    testEmailFeedback = Pair(success, msg)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSendingTest && (gmailAddress.isNotBlank() && (appPassword.isNotBlank() || hasPasswordStored)),
                            modifier = Modifier.height(42.dp)
                        ) {
                            if (isSendingTest) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send test email to myself", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    testEmailFeedback?.let { (success, msg) ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = msg,
                            color = if (success) Emerald600 else Rose600,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. EXPORT BACKUP
            item {
                SettingsCard(title = "DATA BACKUP") {
                    Text(
                        text = "Export a full JSON dump of all inquiries, stops, dispatches, ledger records, and partner agents from your Supabase database.",
                        fontSize = 12.sp,
                        color = Slate500,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onExportBackupJson,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
                            contentColor = White
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backup (JSON)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // 4. CREDITS (Jaykumar Mistry only, as instructed)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Isha Tours Travel Dispatcher · Developed for Jaykumar Mistry",
                        fontSize = 12.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun settingsInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Slate900,
    unfocusedBorderColor = Slate200,
    focusedContainerColor = White,
    unfocusedContainerColor = White
)
