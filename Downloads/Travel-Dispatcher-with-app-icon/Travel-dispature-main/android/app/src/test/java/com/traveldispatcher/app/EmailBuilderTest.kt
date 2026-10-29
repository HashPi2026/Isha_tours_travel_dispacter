package com.traveldispatcher.app

import com.traveldispatcher.app.data.email.EmailBuilder
import com.traveldispatcher.app.data.model.InquiryStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailBuilderTest {

    @Test
    fun testClientPhoneIsNeverPresentInEmail() {
        val testPhone = "+91 98765 43210"
        val clientName = "Ramesh Patel"

        // Notice: EmailBuilder.buildEmail function does not even have a clientPhone parameter!
        val email = EmailBuilder.buildEmail(
            inquiryNumber = 1043,
            category = "package",
            clientName = clientName,
            destination = "Goa",
            pickupPoint = "Surat",
            dropPoint = "Surat",
            dateType = "flexible",
            startDate = "2027-01-18",
            endDate = "2027-01-31",
            adults = 3,
            children = 1,
            stops = listOf(
                InquiryStop(placeName = "North Goa", nights = 2),
                InquiryStop(placeName = "South Goa", nights = 3)
            )
        )

        // Strict verification: phone string must NOT appear anywhere in subject, plainText, or HTML
        assertFalse("Subject should not contain phone", email.subject.contains(testPhone))
        assertFalse("Plain text body must never contain client phone", email.plainText.contains(testPhone))
        assertFalse("HTML body must never contain client phone", email.htmlBody.contains(testPhone))
    }

    @Test
    fun testHtmlEscapingSecurity() {
        val dangerousInput = "Goa <script>alert('xss')</script> & Special \"Deals\" 'Luxury'"

        val email = EmailBuilder.buildEmail(
            inquiryNumber = 1050,
            category = "package",
            clientName = "Malicious <User>",
            destination = dangerousInput,
            pickupPoint = "Airport & Hub",
            dropPoint = "Resort <Main>",
            dateType = "fixed",
            startDate = "2027-02-01",
            endDate = "2027-02-05",
            adults = 2,
            children = 0,
            specialNotes = "Prefer <VIP> \"Treatment\" & 5-star"
        )

        // Verify no raw unescaped HTML tags
        assertFalse(email.htmlBody.contains("<script>"))
        assertTrue(email.htmlBody.contains("&lt;script&gt;"))
        assertTrue(email.htmlBody.contains("&amp; Special"))
        assertTrue(email.htmlBody.contains("&quot;Deals&quot;"))
        assertTrue(email.htmlBody.contains("&#39;Luxury&#39;"))
        assertTrue(email.htmlBody.contains("&lt;VIP&gt;"))
    }

    @Test
    fun testSubjectAndDateFormatting() {
        val email = EmailBuilder.buildEmail(
            inquiryNumber = 1043,
            category = "package",
            clientName = "Rahul Shah",
            destination = "Goa",
            pickupPoint = "Surat",
            dropPoint = "Surat",
            dateType = "flexible",
            startDate = "2027-01-18",
            endDate = "2027-01-31",
            adults = 3,
            children = 1
        )

        assertEquals("New Package Inquiry #1043 - Goa", email.subject)
        assertTrue(email.plainText.contains("Anytime between 18 Jan 2027 and 31 Jan 2027"))
        assertTrue(email.htmlBody.contains("Anytime between 18 Jan 2027 and 31 Jan 2027"))
        assertTrue(email.plainText.contains("Please reply with your best quote."))
    }

    @Test
    fun testFlightTicketTripTypeFormatting() {
        val email = EmailBuilder.buildEmail(
            inquiryNumber = 1042,
            category = "ticket",
            clientName = "Neha Patel",
            destination = "Dubai",
            pickupPoint = "Surat",
            dropPoint = "Dubai",
            dateType = "fixed",
            startDate = "2027-02-10",
            endDate = "2027-02-17",
            adults = 2,
            children = 0,
            flightTripType = "round_trip",
            flightClass = "Economy",
            preferredAirline = "Emirates"
        )

        assertEquals("New Flight Ticket Inquiry #1042 - Dubai", email.subject)
        assertTrue(email.plainText.contains("Round-Trip (Going & Return)"))
        assertTrue(email.plainText.contains("Class: Economy"))
        assertTrue(email.plainText.contains("Preferred Airline: Emirates"))
    }

    @Test
    fun testFlexibleDateRangeText() {
        val email = EmailBuilder.buildEmail(
            inquiryNumber = 1045,
            category = "ticket",
            clientName = "Amit Kumar",
            destination = "Bangkok",
            dateType = "flexible",
            flexibleDateRange = "Anytime in March 2027 (10-day trip)",
            adults = 2,
            children = 0
        )

        assertEquals("New Flight Ticket Inquiry #1045 - Bangkok", email.subject)
        assertTrue(email.plainText.contains("Anytime in March 2027 (10-day trip)"))
        assertTrue(email.htmlBody.contains("Anytime in March 2027 (10-day trip)"))
    }
}
