
package com.traveldispatcher.app.data.repository

import com.traveldispatcher.app.data.model.Dispatch
import com.traveldispatcher.app.data.model.Inquiry
import com.traveldispatcher.app.data.model.InquiryStop
import com.traveldispatcher.app.data.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class InquiryInsert(
    val category: String,
    @SerialName("client_name") val clientName: String,
    @SerialName("client_phone") val clientPhone: String,
    val destination: String,
    @SerialName("pickup_point") val pickupPoint: String,
    @SerialName("drop_point") val dropPoint: String,
    @SerialName("date_type") val dateType: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("flexible_date_range") val flexibleDateRange: String? = null,
    val adults: Int,
    val children: Int,
    @SerialName("flight_trip_type") val flightTripType: String? = null,
    @SerialName("flight_class") val flightClass: String? = null,
    @SerialName("preferred_airline") val preferredAirline: String? = null,
    @SerialName("special_notes") val specialNotes: String? = null,
    val status: String
)

@Serializable
private data class InquiryStopInsert(
    @SerialName("inquiry_id") val inquiryId: String,
    @SerialName("stop_order") val stopOrder: Int,
    @SerialName("place_name") val placeName: String,
    val nights: Int
)

@Serializable
private data class DispatchInsert(
    @SerialName("inquiry_id") val inquiryId: String,
    @SerialName("agent_id") val agentId: String?,
    @SerialName("agent_name") val agentName: String,
    @SerialName("agent_email") val agentEmail: String,
    @SerialName("agent_phone") val agentPhone: String?,
    val status: String = "pending"
)

class InquiryRepository {

    suspend fun getInquiries(): Result<List<Inquiry>> =
        withContext(Dispatchers.IO) {
            val client = SupabaseConfig.client
                ?: return@withContext Result.failure(
                    IllegalStateException("Supabase not configured")
                )

            runCatching {
                val inquiries = client.from("inquiries").select {
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<Inquiry>()

                inquiries.map { inquiry ->
                    val stops = client.from("inquiry_stops").select {
                        filter { eq("inquiry_id", inquiry.id) }
                        order("stop_order", order = Order.ASCENDING)
                    }.decodeList<InquiryStop>()

                    val dispatches = client.from("dispatches").select {
                        filter { eq("inquiry_id", inquiry.id) }
                        order("created_at", order = Order.ASCENDING)
                    }.decodeList<Dispatch>()

                    inquiry.copy(
                        stops = stops,
                        dispatches = dispatches
                    )
                }
            }
        }

    suspend fun createInquiry(
        inquiry: Inquiry,
        stops: List<InquiryStop>,
        agentIds: List<String>,
        agentsMap: Map<String, com.traveldispatcher.app.data.model.Agent>
    ): Result<Inquiry> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(inquiry.clientName.isNotBlank()) {
                "Client name is required"
            }
            require(inquiry.destination.isNotBlank()) {
                "Destination is required"
            }

            // Validate selected agents before creating the inquiry.
            val selectedAgents = agentIds.distinct().map { agentId ->
                require(agentId.isUuid()) {
                    "Invalid agent ID. Reload the agents and try again."
                }

                agentsMap[agentId]
                    ?: throw IllegalStateException(
                        "Selected agent was not found. Reload agents and try again."
                    )

            }

            // 1. Insert only actual inquiries-table columns.
            // The database generates the ID, inquiry number and timestamps.
            val insertedInquiry = client.from("inquiries").insert(
                InquiryInsert(
                    category = inquiry.category,
                    clientName = inquiry.clientName.trim(),
                    clientPhone = inquiry.clientPhone.trim(),
                    destination = inquiry.destination.trim(),
                    pickupPoint = inquiry.pickupPoint.trim(),
                    dropPoint = inquiry.dropPoint.trim(),
                    dateType = inquiry.dateType,
                    startDate = inquiry.startDate,
                    endDate = inquiry.endDate,
                    flexibleDateRange = inquiry.flexibleDateRange,
                    adults = inquiry.adults,
                    children = inquiry.children,
                    flightTripType = inquiry.flightTripType,
                    flightClass = inquiry.flightClass,
                    preferredAirline = inquiry.preferredAirline,
                    specialNotes = inquiry.specialNotes,
                    status = inquiry.status
                )
            ) {
                select()
            }.decodeSingle<Inquiry>()

            // 2. Insert stops using the database-generated inquiry UUID.
            val insertedStops = if (stops.isNotEmpty()) {
                val stopPayloads = stops.mapIndexed { index, stop ->
                    InquiryStopInsert(
                        inquiryId = insertedInquiry.id,
                        stopOrder = index + 1,
                        placeName = stop.placeName.trim(),
                        nights = stop.nights
                    )
                }

                client.from("inquiry_stops").insert(stopPayloads) {
                    select()
                }.decodeList<InquiryStop>()
            } else {
                emptyList()
            }

            // 3. Create dispatch records for selected real agents.
            val dispatchPayloads = selectedAgents.map { agent ->
                DispatchInsert(
                    inquiryId = insertedInquiry.id,
                    agentId = agent.id,
                    agentName = agent.name,
                    agentEmail = agent.email,
                    agentPhone = agent.phone,
                    status = "pending"
                )
            }

            val insertedDispatches = if (dispatchPayloads.isNotEmpty()) {
                client.from("dispatches").insert(dispatchPayloads) {
                    select()
                }.decodeList<Dispatch>()
            } else {
                emptyList()
            }

            insertedInquiry.copy(
                stops = insertedStops,
                dispatches = insertedDispatches
            )
        }
    }

    suspend fun updateInquiryStatus(
        inquiryId: String,
        newStatus: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(inquiryId.isUuid()) { "Invalid inquiry ID" }

            client.from("inquiries").update(
                mapOf("status" to newStatus)
            ) {
                filter { eq("id", inquiryId) }
            }
            Unit
        }
    }



    suspend fun updateDispatchStatus(
        dispatchId: String,
        status: String,
        errorMessage: String? = null,
        sentAt: String? = null,
        emailHtml: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(dispatchId.isUuid()) { "Invalid dispatch ID" }

            val payload = mutableMapOf<String, String?>(
                "status" to status
            )

            if (errorMessage != null) {
                payload["error_message"] = errorMessage
            }
            if (sentAt != null) {
                payload["sent_at"] = sentAt
            }
            if (emailHtml != null) {
                payload["email_html"] = emailHtml
            }

            client.from("dispatches").update(payload) {
                filter { eq("id", dispatchId) }
            }
            Unit
        }
    }

    suspend fun deleteInquiry(
        inquiryId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(inquiryId.isUuid()) { "Invalid inquiry ID" }

            client.from("inquiries").delete {
                filter { eq("id", inquiryId) }
            }
            Unit
        }
    }



    private fun String.isUuid(): Boolean =
        Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        ).matches(this)
}
