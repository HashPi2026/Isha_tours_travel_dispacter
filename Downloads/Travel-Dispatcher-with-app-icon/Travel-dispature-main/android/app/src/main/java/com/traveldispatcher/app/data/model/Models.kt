package com.traveldispatcher.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppOwner(
    val id: String,
    val email: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Agent(
    val id: String = "",
    val name: String,
    val email: String,
    val phone: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class Inquiry(
    val id: String = "",
    @SerialName("inquiry_number") val inquiryNumber: Int = 0,
    val category: String, // "package" or "ticket"
    @SerialName("client_name") val clientName: String,
    @SerialName("client_phone") val clientPhone: String,
    val destination: String,
    @SerialName("pickup_point") val pickupPoint: String = "",
    @SerialName("drop_point") val dropPoint: String = "",
    @SerialName("date_type") val dateType: String, // "fixed" or "flexible"
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("flexible_date_range") val flexibleDateRange: String? = null,
    val adults: Int = 1,
    val children: Int = 0,
    @SerialName("flight_trip_type") val flightTripType: String? = null, // "one_way" or "round_trip"
    @SerialName("flight_class") val flightClass: String? = null,
    @SerialName("preferred_airline") val preferredAirline: String? = null,
    @SerialName("special_notes") val specialNotes: String? = null,
    val status: String = "inquiry", // "inquiry", "confirmed", "cancelled"
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,

    // Loaded separately from their own Supabase tables.
    @kotlinx.serialization.Transient
    val stops: List<InquiryStop> = emptyList(),

    @kotlinx.serialization.Transient
    val dispatches: List<Dispatch> = emptyList()

//    // Joined relations:
//    val stops: List<InquiryStop> = emptyList(),
//    val dispatches: List<Dispatch> = emptyList(),
    // Loaded separately from their own Supabase tables.

)

@Serializable
data class InquiryStop(
    val id: String = "",
    @SerialName("inquiry_id") val inquiryId: String = "",
    @SerialName("stop_order") val stopOrder: Int = 1,
    @SerialName("place_name") val placeName: String,
    val nights: Int = 1,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Dispatch(
    val id: String = "",
    @SerialName("inquiry_id") val inquiryId: String = "",
    @SerialName("agent_id") val agentId: String? = null,
    @SerialName("agent_name") val agentName: String,
    @SerialName("agent_email") val agentEmail: String,
    @SerialName("agent_phone") val agentPhone: String? = null,
    val status: String = "pending", // "pending", "sent", "failed"
    @SerialName("sent_at") val sentAt: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("email_html") val emailHtml: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LedgerRecord(
    val id: String = "",
    @SerialName("inquiry_id") val inquiryId: String? = null,
    @SerialName("client_name") val clientName: String,
    @SerialName("received_amount") val receivedAmount: Double = 0.0,
    @SerialName("received_date") val receivedDate: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val sends: List<LedgerSend> = emptyList()
) {
    val totalSent: Double
        get() = sends.sumOf { it.amountSent }

    val balanceHeld: Double
        get() = (receivedAmount - totalSent).coerceAtLeast(0.0)
}

@Serializable
data class LedgerSend(
    val id: String = "",
    @SerialName("ledger_record_id") val ledgerRecordId: String = "",
    @SerialName("agent_id") val agentId: String? = null,
    @SerialName("agent_name") val agentName: String,
    @SerialName("amount_sent") val amountSent: Double,
    @SerialName("disbursement_date") val disbursementDate: String,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

data class EmailSettings(
    val gmailAddress: String = "bookings.ishatours@gmail.com",
    val appPassword: String = "",
    val senderDisplayName: String = "Isha Tours & Travel",
    val replyToEmail: String = ""
)
