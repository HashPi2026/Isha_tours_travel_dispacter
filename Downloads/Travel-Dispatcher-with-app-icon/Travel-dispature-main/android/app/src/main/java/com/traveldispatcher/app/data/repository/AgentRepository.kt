package com.traveldispatcher.app.data.repository

import com.traveldispatcher.app.data.model.Agent
import com.traveldispatcher.app.data.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class AgentInsert(
    val name: String,
    val email: String,
    val phone: String,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
private data class AgentUpdate(
    val name: String,
    val email: String,
    val phone: String,
    @SerialName("is_active")
    val isActive: Boolean
)

class AgentRepository {

    suspend fun getAgents(): Result<List<Agent>> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            client.from("agents").select {
                order("name", order = Order.ASCENDING)
            }.decodeList<Agent>()
        }
    }

    /**
     * Finds an agent by email without regard to letter case.
     * This prevents the inquiry screen from creating a duplicate agent
     * when the same email is already in the directory.
     */
    suspend fun findAgentByEmail(email: String): Result<Agent?> =
        withContext(Dispatchers.IO) {
            val client = SupabaseConfig.client
                ?: return@withContext Result.failure(
                    IllegalStateException("Supabase not configured")
                )

            runCatching {
                val normalizedEmail = email.trim()
                require(normalizedEmail.isNotBlank()) { "Agent email is required" }

                client.from("agents")
                    .select()
                    .decodeList<Agent>()
                    .firstOrNull {
                        it.email.trim().equals(normalizedEmail, ignoreCase = true)
                    }
            }
        }

    /**
     * Creates an agent. Email is required; name and phone can be omitted by
     * the inquiry quick-add flow. A readable name is generated from the email
     * prefix if the caller passes a blank name, and phone is stored as an empty
     * string. The agents table must permit an empty phone string.
     */
    suspend fun createAgent(
        name: String,
        email: String,
        phone: String
    ): Result<Agent> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            val normalizedEmail = email.trim()
            require(normalizedEmail.isNotBlank()) { "Agent email is required" }

            val resolvedName = name.trim().ifBlank {
                normalizedEmail.substringBefore("@").ifBlank { normalizedEmail }
            }

            // Check for an existing email before inserting a new directory entry.
            val existing = client.from("agents")
                .select()
                .decodeList<Agent>()
                .firstOrNull {
                    it.email.trim().equals(normalizedEmail, ignoreCase = true)
                }

            existing ?: client.from("agents").insert(
                AgentInsert(
                    name = resolvedName,
                    email = normalizedEmail,
                    phone = phone.trim(),
                    isActive = true
                )
            ) {
                select()
            }.decodeSingle<Agent>()
        }
    }

    suspend fun updateAgent(
        id: String,
        name: String,
        email: String,
        phone: String,
        isActive: Boolean
    ): Result<Agent> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(id.isUuid()) { "Invalid agent ID. Reload agents and try again." }
            require(name.isNotBlank()) { "Agent name is required" }
            require(email.isNotBlank()) { "Agent email is required" }
            require(phone.isNotBlank()) { "Agent phone is required" }

            client.from("agents").update(
                AgentUpdate(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    isActive = isActive
                )
            ) {
                filter { eq("id", id) }
                select()
            }.decodeSingle<Agent>()
        }
    }

    suspend fun deleteAgent(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase not configured")
            )

        runCatching {
            require(id.isUuid()) { "Invalid agent ID. Reload agents and try again." }

            client.from("agents").delete {
                filter { eq("id", id) }
            }
            Unit
        }
    }

    private fun String.isUuid(): Boolean =
        Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        ).matches(this)
}
