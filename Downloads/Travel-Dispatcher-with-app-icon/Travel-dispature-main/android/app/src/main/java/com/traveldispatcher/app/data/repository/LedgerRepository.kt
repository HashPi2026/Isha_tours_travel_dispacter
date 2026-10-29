package com.traveldispatcher.app.data.repository

import com.traveldispatcher.app.data.model.LedgerRecord
import com.traveldispatcher.app.data.model.LedgerSend
import com.traveldispatcher.app.data.supabase.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LedgerRepository {

    suspend fun getLedgerRecords(): Result<List<LedgerRecord>> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            val records = client.from("ledger_records").select {
                order("created_at", order = Order.DESCENDING)
            }.decodeList<LedgerRecord>()

            records.map { record ->
                val sends = client.from("ledger_sends").select {
                    filter { eq("ledger_record_id", record.id) }
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<LedgerSend>()

                record.copy(sends = sends)
            }
        }
    }

    suspend fun createLedgerRecord(
        clientName: String,
        receivedAmount: Double,
        receivedDate: String,
        notes: String? = null,
        inquiryId: String? = null
    ): Result<LedgerRecord> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            val record = LedgerRecord(
                clientName = clientName.trim(),
                receivedAmount = receivedAmount,
                receivedDate = receivedDate,
                notes = notes?.trim(),
                inquiryId = inquiryId
            )
            client.from("ledger_records").insert(record) {
                select()
            }.decodeSingle<LedgerRecord>()
        }
    }

    suspend fun updateLedgerRecord(
        id: String,
        clientName: String,
        receivedAmount: Double,
        notes: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            client.from("ledger_records").update(
                mapOf(
                    "client_name" to clientName.trim(),
                    "received_amount" to receivedAmount,
                    "notes" to notes?.trim()
                )
            ) {
                filter { eq("id", id) }
            }
            Unit
        }
    }

    /**
     * Adds a sent amount inside the existing ledger record.
     * Database triggers validate that total sent does NOT exceed received amount.
     */
    suspend fun addSentAmount(
        ledgerRecordId: String,
        agentId: String?,
        agentName: String,
        amountSent: Double,
        disbursementDate: String,
        note: String? = null
    ): Result<LedgerSend> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            val send = LedgerSend(
                ledgerRecordId = ledgerRecordId,
                agentId = agentId,
                agentName = agentName.trim(),
                amountSent = amountSent,
                disbursementDate = disbursementDate,
                note = note?.trim()
            )
            client.from("ledger_sends").insert(send) {
                select()
            }.decodeSingle<LedgerSend>()
        }
    }

    /**
     * Deletes a single sent amount from a record.
     */
    suspend fun deleteSentAmount(sendId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            client.from("ledger_sends").delete {
                filter { eq("id", sendId) }
            }
            Unit
        }
    }

    /**
     * Permanent delete of ledger record and all cascaded sends.
     */
    suspend fun deleteLedgerRecord(recordId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SupabaseConfig.client ?: return@withContext Result.failure(
            IllegalStateException("Supabase not configured")
        )

        runCatching {
            client.from("ledger_records").delete {
                filter { eq("id", recordId) }
            }
            Unit
        }
    }
}
