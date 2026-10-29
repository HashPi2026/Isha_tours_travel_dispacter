package com.traveldispatcher.app.data.email

import android.util.Log
import com.traveldispatcher.app.data.model.Dispatch
import com.traveldispatcher.app.data.model.EmailSettings
import com.traveldispatcher.app.data.repository.InquiryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class SmtpMailSender(
    private val inquiryRepository: InquiryRepository
) {
    companion object {
        private const val TAG = "SmtpMailSender"
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "465"
        private const val DELAY_BETWEEN_EMAILS_MS = 600L
    }

    /**
     * Sends emails to a list of dispatches one by one with a 600ms gap.
     * Updates each dispatch row in Supabase with sent/failed status and readable error message.
     * A failure in one email does NOT stop the remaining emails.
     */
    suspend fun dispatchBatch(
        dispatches: List<Dispatch>,
        builtEmail: BuiltEmail,
        settings: EmailSettings,
        onProgress: ((completed: Int, total: Int, lastResult: DispatchResult) -> Unit)? = null
    ): List<DispatchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<DispatchResult>()

        if (settings.gmailAddress.isBlank() || settings.appPassword.isBlank()) {
            val failureMsg = "Email not configured: Please set Gmail Address and App Password in Settings."
            dispatches.forEach { dispatch ->
                val result = DispatchResult(dispatch.id, false, failureMsg)
                results.add(result)
                inquiryRepository.updateDispatchStatus(
                    dispatchId = dispatch.id,
                    status = "failed",
                    errorMessage = failureMsg,
                    emailHtml = builtEmail.htmlBody
                )
            }
            return@withContext results
        }

        // Configure JavaMail SSL Session for smtp.gmail.com:465
        val props = Properties().apply {
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.auth", "true")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.ssl.required", "true")
            put("mail.smtp.socketFactory.port", SMTP_PORT)
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.socketFactory.fallback", "false")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "15000")
        }

        val auth = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(settings.gmailAddress.trim(), settings.appPassword.trim())
            }
        }

        val session = Session.getInstance(props, auth)

        dispatches.forEachIndexed { index, dispatch ->
            val result = sendSingleMail(session, dispatch, builtEmail, settings)
            results.add(result)

            // Update database row immediately after each send
            val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(Date())

            inquiryRepository.updateDispatchStatus(
                dispatchId = dispatch.id,
                status = if (result.success) "sent" else "failed",
                errorMessage = result.errorMessage,
                sentAt = if (result.success) nowIso else null,
                emailHtml = builtEmail.htmlBody
            )

            onProgress?.invoke(index + 1, dispatches.size, result)

            // 600 ms gap between sends to comply with rate limits & prevent burst flagging
            if (index < dispatches.size - 1) {
                delay(DELAY_BETWEEN_EMAILS_MS)
            }
        }

        results
    }

    private fun sendSingleMail(
        session: Session,
        dispatch: Dispatch,
        builtEmail: BuiltEmail,
        settings: EmailSettings
    ): DispatchResult {
        return try {
            val message = MimeMessage(session).apply {
                val fromPersonal = settings.senderDisplayName.ifBlank { "Travel Dispatcher" }
                setFrom(InternetAddress(settings.gmailAddress.trim(), fromPersonal))
                setRecipient(Message.RecipientType.TO, InternetAddress(dispatch.agentEmail.trim()))
                subject = builtEmail.subject

                if (settings.replyToEmail.isNotBlank()) {
                    replyTo = arrayOf(InternetAddress(settings.replyToEmail.trim()))
                }

                // Multipart: plain text alternative + rich HTML
                val multipart = MimeMultipart("alternative")

                val textPart = MimeBodyPart().apply {
                    setText(builtEmail.plainText, "utf-8")
                }
                val htmlPart = MimeBodyPart().apply {
                    setContent(builtEmail.htmlBody, "text/html; charset=utf-8")
                }

                multipart.addBodyPart(textPart)
                multipart.addBodyPart(htmlPart)

                setContent(multipart)
                sentDate = Date()
            }

            Transport.send(message)
            Log.d(TAG, "Sent email to ${dispatch.agentEmail} successfully")
            DispatchResult(dispatch.id, true, null)
        } catch (e: Exception) {
            val readableError = e.localizedMessage ?: e.message ?: "Failed to connect to SMTP server"
            Log.e(TAG, "Failed sending to ${dispatch.agentEmail}: $readableError", e)
            DispatchResult(dispatch.id, false, readableError)
        }
    }
}

data class DispatchResult(
    val dispatchId: String,
    val success: Boolean,
    val errorMessage: String? = null
)
