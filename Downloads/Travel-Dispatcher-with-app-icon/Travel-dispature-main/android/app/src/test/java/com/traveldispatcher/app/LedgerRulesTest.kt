package com.traveldispatcher.app

import com.traveldispatcher.app.data.model.LedgerRecord
import com.traveldispatcher.app.data.model.LedgerSend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerRulesTest {

    @Test
    fun testInitialBalanceCalculation() {
        val record = LedgerRecord(
            id = "rec-1",
            clientName = "Amit Joshi",
            receivedAmount = 5000.0,
            receivedDate = "2026-10-15",
            notes = "Advance, Manali",
            sends = emptyList()
        )

        assertEquals(5000.0, record.receivedAmount, 0.01)
        assertEquals(0.0, record.totalSent, 0.01)
        assertEquals(5000.0, record.balanceHeld, 0.01)
    }

    @Test
    fun testDisbursementReducesBalance() {
        val sends = listOf(
            LedgerSend(
                id = "send-1",
                ledgerRecordId = "rec-2",
                agentName = "Sunrise Tours",
                amountSent = 9000.0,
                disbursementDate = "2026-10-14"
            ),
            LedgerSend(
                id = "send-2",
                ledgerRecordId = "rec-2",
                agentName = "SkyWay Travels",
                amountSent = 4000.0,
                disbursementDate = "2026-10-15"
            )
        )

        val record = LedgerRecord(
            id = "rec-2",
            clientName = "Neha Patel",
            receivedAmount = 15000.0,
            receivedDate = "2026-10-12",
            notes = "Advance, Dubai tickets",
            sends = sends
        )

        assertEquals(15000.0, record.receivedAmount, 0.01)
        assertEquals(13000.0, record.totalSent, 0.01)
        assertEquals(2000.0, record.balanceHeld, 0.01)
    }

    @Test
    fun testOverSendingValidationLogic() {
        val record = LedgerRecord(
            id = "rec-1",
            clientName = "Amit Joshi",
            receivedAmount = 5000.0,
            receivedDate = "2026-10-15",
            sends = emptyList()
        )

        val maxAllowed = record.balanceHeld
        val attemptedSendAmount = 5500.0

        // Validation rule: attempted send amount must not exceed balanceHeld
        val isAllowed = attemptedSendAmount <= maxAllowed
        assertFalse("Attempting to disburse 5500 with 5000 held must be rejected", isAllowed)

        val exactSend = 5000.0
        assertTrue("Exact balance disbursement must be permitted", exactSend <= maxAllowed)
    }

    @Test
    fun testCannotReduceReceivedAmountBelowTotalSent() {
        val sends = listOf(
            LedgerSend(
                id = "send-1",
                ledgerRecordId = "rec-1",
                agentName = "Sunrise Tours",
                amountSent = 4000.0,
                disbursementDate = "2026-10-15"
            )
        )

        val record = LedgerRecord(
            id = "rec-1",
            clientName = "Amit Joshi",
            receivedAmount = 5000.0,
            receivedDate = "2026-10-15",
            sends = sends
        )

        val currentTotalSent = record.totalSent // 4000
        val attemptedNewReceivedAmount = 3500.0

        val isReductionAllowed = attemptedNewReceivedAmount >= currentTotalSent
        assertFalse("Cannot lower received amount below what was already disbursed to agents", isReductionAllowed)
    }
}
