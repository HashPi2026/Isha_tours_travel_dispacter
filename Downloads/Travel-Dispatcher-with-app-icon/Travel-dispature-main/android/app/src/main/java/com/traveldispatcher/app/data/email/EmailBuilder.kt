package com.traveldispatcher.app.data.email

import com.traveldispatcher.app.data.model.InquiryStop
import java.text.SimpleDateFormat
import java.util.Locale

data class BuiltEmail(
    val subject: String,
    val plainText: String,
    val htmlBody: String
)

/**
 * Builds quotation dispatch emails for partner travel agents.
 *
 * CRITICAL PRIVACY & ARCHITECTURE INVARIANT:
 * This function NEVER receives, accepts, or parses the client's phone number.
 * All dynamic parameters are strictly HTML-escaped to protect against injection.
 */
object EmailBuilder {

    fun buildEmail(
        inquiryNumber: Int,
        category: String, // "package" or "ticket"
        clientName: String,
        destination: String,
        pickupPoint: String = "",
        dropPoint: String = "",
        dateType: String, // "fixed" or "flexible"
        startDate: String = "",
        endDate: String = "",
        flexibleDateRange: String? = null,
        adults: Int,
        children: Int,
        stops: List<InquiryStop> = emptyList(),
        flightTripType: String? = null,
        flightClass: String? = null,
        preferredAirline: String? = null,
        specialNotes: String? = null
    ): BuiltEmail {
        val safeClientName = escapeHtml(clientName.trim())
        val safeDestination = escapeHtml(destination.trim())
        val safePickup = escapeHtml(pickupPoint.trim())
        val safeDrop = escapeHtml(dropPoint.trim())
        val safeCategory = if (category.equals("ticket", ignoreCase = true)) "Flight Ticket" else "Package"
        val safeSubject = "New $safeCategory Inquiry #$inquiryNumber - $safeDestination"

        // Format dates
        val formattedDates = if (dateType.equals("flexible", ignoreCase = true)) {
            if (!flexibleDateRange.isNullOrBlank()) {
                flexibleDateRange.trim()
            } else if (startDate.isNotBlank() && endDate.isNotBlank()) {
                "Anytime between ${formatHumanDate(startDate)} and ${formatHumanDate(endDate)}"
            } else {
                "Flexible (Anytime Between)"
            }
        } else {
            if (startDate.isNotBlank() && endDate.isNotBlank()) {
                "${formatHumanDate(startDate)} to ${formatHumanDate(endDate)}"
            } else if (startDate.isNotBlank()) {
                formatHumanDate(startDate)
            } else {
                "Dates to be decided"
            }
        }

        // Format passengers
        val passengersStr = buildString {
            append("$adults Adult${if (adults > 1) "s" else ""}")
            if (children > 0) {
                append(", $children Child${if (children > 1) "ren" else ""}")
            }
        }

        // Build Plain Text Version
        val plainText = buildString {
            appendLine("NEW TRAVEL INQUIRY #$inquiryNumber")
            appendLine("----------------------------------------")
            appendLine("Category: $safeCategory")
            appendLine("Client Name: $clientName")
            appendLine("Destination: $destination")
            appendLine("Pickup Point: $pickupPoint")
            appendLine("Drop Point: $dropPoint")
            appendLine("Travel Dates: $formattedDates")
            appendLine("Passengers: $passengersStr")

            if (category.equals("package", ignoreCase = true) && stops.isNotEmpty()) {
                val stopsList = stops.joinToString(" → ") { "${it.placeName} (${it.nights}N)" }
                appendLine("Itinerary Stops: $stopsList")
            } else if (category.equals("ticket", ignoreCase = true)) {
                val tripTypeLabel = if (flightTripType == "round_trip") "Round-Trip (Going & Return)" else "One-Way (Single)"
                appendLine("Trip Type: $tripTypeLabel")
                if (!flightClass.isNullOrBlank()) appendLine("Class: $flightClass")
                if (!preferredAirline.isNullOrBlank()) appendLine("Preferred Airline: $preferredAirline")
            }

            if (!specialNotes.isNullOrBlank()) {
                appendLine("Special Notes: $specialNotes")
            }

            appendLine("----------------------------------------")
            appendLine("Please reply with your best quote.")
        }

        // Build Clean HTML Version
        val htmlBody = buildString {
            append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\" />")
            append("<style>")
            append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b; background: #f8fafc; margin: 0; padding: 24px; }")
            append(".card { max-width: 600px; margin: 0 auto; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }")
            append(".header { background: #0f172a; color: #ffffff; padding: 20px 24px; }")
            append(".header h1 { margin: 0; font-size: 18px; font-weight: 700; }")
            append(".header p { margin: 4px 0 0; font-size: 13px; color: #94a3b8; }")
            append(".content { padding: 24px; }")
            append(".row { display: flex; margin-bottom: 12px; font-size: 14px; }")
            append(".label { font-weight: 600; color: #64748b; width: 140px; flex-shrink: 0; }")
            append(".value { color: #0f172a; flex: 1; }")
            append(".stops-badge { background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 6px; padding: 8px 12px; margin: 12px 0; font-size: 13px; color: #334155; }")
            append(".footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 16px 24px; text-align: center; font-size: 14px; font-weight: 600; color: #0f172a; }")
            append("</style></head><body>")

            append("<div class=\"card\">")
            append("<div class=\"header\">")
            append("<h1>New $safeCategory Inquiry #$inquiryNumber</h1>")
            append("<p>Partner Quotation Request · $safeDestination</p>")
            append("</div>")

            append("<div class=\"content\">")
            append("<div class=\"row\"><div class=\"label\">Client Name</div><div class=\"value\"><strong>$safeClientName</strong></div></div>")
            append("<div class=\"row\"><div class=\"label\">Destination</div><div class=\"value\">$safeDestination</div></div>")
            append("<div class=\"row\"><div class=\"label\">Pickup Point</div><div class=\"value\">$safePickup</div></div>")
            append("<div class=\"row\"><div class=\"label\">Drop Point</div><div class=\"value\">$safeDrop</div></div>")
            append("<div class=\"row\"><div class=\"label\">Travel Dates</div><div class=\"value\">$formattedDates</div></div>")
            append("<div class=\"row\"><div class=\"label\">Passengers</div><div class=\"value\">$passengersStr</div></div>")

            if (category.equals("package", ignoreCase = true) && stops.isNotEmpty()) {
                val stopsHtml = stops.joinToString(" &rarr; ") {
                    "${escapeHtml(it.placeName)} (${it.nights}N)"
                }
                append("<div class=\"stops-badge\"><strong>Itinerary:</strong> $stopsHtml</div>")
            } else if (category.equals("ticket", ignoreCase = true)) {
                val tripTypeLabel = if (flightTripType == "round_trip") "Round-Trip (Going & Return)" else "One-Way (Single)"
                append("<div class=\"row\"><div class=\"label\">Trip Type</div><div class=\"value\">$tripTypeLabel</div></div>")
                if (!flightClass.isNullOrBlank()) {
                    append("<div class=\"row\"><div class=\"label\">Flight Class</div><div class=\"value\">${escapeHtml(flightClass)}</div></div>")
                }
                if (!preferredAirline.isNullOrBlank()) {
                    append("<div class=\"row\"><div class=\"label\">Preferred Airline</div><div class=\"value\">${escapeHtml(preferredAirline)}</div></div>")
                }
            }

            if (!specialNotes.isNullOrBlank()) {
                append("<div class=\"row\"><div class=\"label\">Special Notes</div><div class=\"value\">${escapeHtml(specialNotes)}</div></div>")
            }

            append("</div>") // content
            append("<div class=\"footer\">Please reply with your best quote.</div>")
            append("</div></body></html>")
        }

        return BuiltEmail(
            subject = safeSubject,
            plainText = plainText,
            htmlBody = htmlBody
        )
    }

    private fun escapeHtml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun formatHumanDate(rawDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(rawDate) ?: return rawDate
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
            formatter.format(date)
        } catch (_: Exception) {
            rawDate
        }
    }
}
