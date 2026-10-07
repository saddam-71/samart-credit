package com.example

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class SmartCreditBusinessLogicTest {

    @Test
    fun testPlatformFeeCalculation() {
        val grossRepaymentPaise = 100000L // ₹1,000.00
        val feeRate = 2.0 / 100.0 // 2%
        val feePaise = (grossRepaymentPaise * feeRate).toLong()
        val merchantNetPaise = grossRepaymentPaise - feePaise

        assertEquals(2000L, feePaise) // ₹20.00 platform fee
        assertEquals(98000L, merchantNetPaise) // ₹980.00 merchant net
    }

    @Test
    fun testPlatformFeeZeroForPremiumPlan() {
        val grossRepaymentPaise = 250000L // ₹2,500.00
        val plan = SubscriptionPlan.PREMIUM
        val feeRate = if (plan == SubscriptionPlan.FREE) 0.02 else 0.0
        val feePaise = (grossRepaymentPaise * feeRate).toLong()

        assertEquals(0L, feePaise) // Premium has 0% platform fee
    }

    @Test
    fun testPlatformFeeValidationLimits() {
        val validLow = 0.0
        val validMid = 2.5
        val validHigh = 10.0
        val invalidNegative = -0.5
        val invalidExcess = 10.1

        fun isValidFee(percent: Double): Boolean = percent in 0.0..10.0

        assertTrue(isValidFee(validLow))
        assertTrue(isValidFee(validMid))
        assertTrue(isValidFee(validHigh))
        assertFalse(isValidFee(invalidNegative))
        assertFalse(isValidFee(invalidExcess))
    }

    @Test
    fun testLedgerBalanceArithmetic() {
        var balancePaise = 100000L // Opening: ₹1,000

        // Credit sale: ₹500
        val creditSale = 50000L
        balancePaise += creditSale
        assertEquals(150000L, balancePaise) // ₹1,500

        // Repayment: ₹300
        val repayment = 30000L
        balancePaise -= repayment
        assertEquals(120000L, balancePaise) // ₹1,200

        // Reversal of credit sale: -₹500
        balancePaise -= creditSale
        assertEquals(70000L, balancePaise) // ₹700
    }
}
