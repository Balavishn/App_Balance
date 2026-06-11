package com.aibudgetplanner.app.sms

import com.aibudgetplanner.app.domain.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsExpenseParserTest {

    @Test
    fun parseTransaction_detectsDebitUpiTemplate() {
        val message = "A/C XX1234 debited by INR 450.50 on UPI to SWIGGY ref 9ABCD12345"

        val result = SmsExpenseParser.parseTransaction(message)

        assertNotNull(result)
        assertEquals(450.50, result!!.amount, 0.0001)
        assertEquals(ExpenseCategory.FOOD, result.category)
        assertEquals("UPI", result.paymentMethod)
        assertEquals("SWIGGY", result.merchant.uppercase())
    }

    @Test
    fun parseTransaction_ignoresCreditMessage() {
        val message = "INR 1200 credited to your account via IMPS"

        val result = SmsExpenseParser.parseTransaction(message)

        assertNull(result)
    }
}
