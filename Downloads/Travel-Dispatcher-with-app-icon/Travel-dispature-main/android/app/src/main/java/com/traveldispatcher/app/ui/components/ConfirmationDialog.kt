package com.traveldispatcher.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traveldispatcher.app.ui.theme.Rose600
import com.traveldispatcher.app.ui.theme.Slate600
import com.traveldispatcher.app.ui.theme.Slate900
import com.traveldispatcher.app.ui.theme.White

@Composable
fun PermanentDeleteDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Permanently Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Slate900
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Slate600
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rose600,
                    contentColor = White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = confirmButtonText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Cancel", color = Slate600)
            }
        }
    )
}
