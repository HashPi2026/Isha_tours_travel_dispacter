package com.traveldispatcher.app.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.traveldispatcher.app.data.model.Inquiry
import com.traveldispatcher.app.data.model.LedgerRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodPreset(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    ALL_TIME("All time")
}

class ExportManager(private val context: Context) {

    private val exportsDir: File by lazy {
        File(context.cacheDir, "exports").apply { mkdirs() }
    }

    /**
     * Filters inquiries by period preset and optional status filter
     */
    fun filterInquiries(
        list: List<Inquiry>,
        preset: PeriodPreset,
        statusFilter: String? = null // null means "all"
    ): List<Inquiry> {
        val (startDate, endDate) = getPresetDateRange(preset)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return list.filter { inquiry ->
            val matchesStatus = statusFilter == null || inquiry.status.equals(statusFilter, ignoreCase = true)
            if (!matchesStatus) return@filter false

            if (startDate == null || endDate == null) return@filter true

            val itemDateStr = inquiry.startDate?.take(10).orEmpty()
            try {
                val itemDate = isoFormat.parse(itemDateStr) ?: return@filter true
                !itemDate.before(startDate) && !itemDate.after(endDate)
            } catch (_: Exception) {
                true
            }
        }
    }

    /**
     * Generates CSV for inquiries, strictly masking client phone unless includePhone = true.
     */
    fun generateInquiriesCsv(
        inquiries: List<Inquiry>,
        includePhone: Boolean
    ): File {
        val file = File(exportsDir, "travel_inquiries_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { writer ->
            writer.write("Inquiry Number,Category,Client Name,Phone,Destination,Pickup,Drop,Start Date,End Date,Adults,Children,Status,Dispatched Count\n")
            inquiries.forEach { inq ->
                val phoneVal = if (includePhone) "\"${inq.clientPhone}\"" else "\"[HIDDEN]\""
                writer.write(
                    "${inq.inquiryNumber},\"${inq.category}\",\"${inq.clientName}\",$phoneVal," +
                            "\"${inq.destination}\",\"${inq.pickupPoint}\",\"${inq.dropPoint}\"," +
                            "\"${inq.startDate}\",\"${inq.endDate}\",${inq.adults},${inq.children}," +
                            "\"${inq.status}\",${inq.dispatches.size}\n"
                )
            }
        }
        return file
    }

    /**
     * Generates PDF for inquiries using native Android PdfDocument
     */
    fun generateInquiriesPdf(
        inquiries: List<Inquiry>,
        includePhone: Boolean
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            isFakeBoldText = true
        }

        val paintSub = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
        }

        val paintText = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
        }

        val paintLine = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        canvas.drawText("Travel Inquiries Report", 40f, 50f, paintTitle)
        val dateStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated on $dateStr · ${inquiries.size} record(s)", 40f, 68f, paintSub)

        canvas.drawLine(40f, 80f, 555f, 80f, paintLine)

        var y = 110f
        inquiries.take(15).forEach { inq ->
            canvas.drawText("#${inq.inquiryNumber}  ${inq.destination} (${inq.category.replaceFirstChar { it.uppercase() }})", 40f, y, paintTitle.apply { textSize = 12f })
            val phonePart = if (includePhone) " · Phone: ${inq.clientPhone}" else " · Phone: [Hidden]"
            canvas.drawText("Client: ${inq.clientName}$phonePart", 40f, y + 14f, paintSub)
            canvas.drawText("Dates: ${inq.startDate} to ${inq.endDate} | Status: ${inq.status.uppercase()}", 40f, y + 28f, paintText)

            y += 46f
            canvas.drawLine(40f, y - 6f, 555f, y - 6f, paintLine)
        }

        pdfDocument.finishPage(page)

        val file = File(exportsDir, "travel_inquiries_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    /**
     * Generates PDF for Khatabook Middleman Ledger
     */
    fun generateLedgerPdf(records: List<LedgerRecord>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            isFakeBoldText = true
        }
        val paintSub = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
        }
        val paintText = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
        }
        val paintLine = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val totalReceived = records.sumOf { it.receivedAmount }
        val totalSent = records.sumOf { it.totalSent }
        val balanceHeld = (totalReceived - totalSent).coerceAtLeast(0.0)

        canvas.drawText("Khatabook Middleman Ledger", 40f, 50f, paintTitle)
        canvas.drawText("Total Received: Rs. %,.0f  |  Total Sent: Rs. %,.0f  |  Balance: Rs. %,.0f".format(totalReceived, totalSent, balanceHeld), 40f, 70f, paintSub)

        canvas.drawLine(40f, 85f, 555f, 85f, paintLine)

        var y = 115f
        records.take(12).forEach { record ->
            canvas.drawText("${record.clientName} (Date: ${record.receivedDate})", 40f, y, paintTitle.apply { textSize = 12f })
            canvas.drawText("Received: Rs. %,.0f | Sent: Rs. %,.0f | Balance Held: Rs. %,.0f".format(
                record.receivedAmount, record.totalSent, record.balanceHeld
            ), 40f, y + 14f, paintSub)

            if (record.sends.isNotEmpty()) {
                val sendsStr = record.sends.joinToString(", ") { "${it.agentName}: Rs. %,.0f".format(it.amountSent) }
                canvas.drawText("Disbursements: $sendsStr", 40f, y + 28f, paintText)
                y += 14f
            }

            y += 34f
            canvas.drawLine(40f, y - 6f, 555f, y - 6f, paintLine)
        }

        pdfDocument.finishPage(page)
        val file = File(exportsDir, "travel_ledger_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    /**
     * Share file via Android FileProvider and system share sheet
     */
    fun shareFile(file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun getPresetDateRange(preset: PeriodPreset): Pair<Date?, Date?> {
        val cal = Calendar.getInstance()
        val now = cal.time

        return when (preset) {
            PeriodPreset.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.time, now)
            }
            PeriodPreset.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.time, now)
            }
            PeriodPreset.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.time, now)
            }
            PeriodPreset.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                val start = cal.time
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = cal.time
                Pair(start, end)
            }
            PeriodPreset.ALL_TIME -> Pair(null, null)
        }
    }
}
