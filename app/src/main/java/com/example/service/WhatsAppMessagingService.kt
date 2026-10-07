package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service providing WhatsApp Business messaging integration and automated overdue payment reminders.
 */
object WhatsAppMessagingService {

    private const val TAG = "WhatsAppMessaging"

    /**
     * Prepares and dispatches an immediate transaction notification for WhatsApp.
     */
    fun buildTransactionNotificationText(
        businessName: String,
        customer: Customer,
        transaction: LedgerTransaction
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(transaction.timestamp))
        val amountRupees = transaction.amountPaise / 100.0
        val balanceRupees = transaction.balanceAfterPaise / 100.0

        val txnTypeTitle = when (transaction.transactionType) {
            TransactionType.CREDIT_SALE -> "💳 New Credit Added"
            TransactionType.CASH_REPAYMENT, TransactionType.ONLINE_REPAYMENT -> "✅ Repayment Received"
            TransactionType.REVERSAL -> "🔄 Transaction Reversal"
            TransactionType.OPENING_BALANCE -> "📋 Opening Balance"
            else -> "📝 Account Adjustment"
        }

        return """
            *$businessName — Credit Alert*
            
            Hello *${customer.fullName}*,
            
            $txnTypeTitle:
            • Amount: *₹$amountRupees*
            • Date: $dateStr
            • Ref/Note: ${transaction.description}
            
            📊 *Updated Balance Due: ₹$balanceRupees*
            
            Thank you for your business!
            _Smart Credit Verified Notification_
        """.trimIndent()
    }

    /**
     * Dispatches notification to customer's WhatsApp using official intent or direct deep-link.
     */
    fun sendWhatsAppNotification(
        context: Context,
        phone: String,
        message: String
    ): Boolean {
        return try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
            val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp dispatch failed: ${e.message}", e)
            false
        }
    }

    /**
     * Builds an automated overdue payment reminder message.
     */
    fun buildOverduePaymentReminderText(
        businessName: String,
        customer: Customer,
        upiId: String
    ): String {
        val dueAmount = customer.currentBalancePaise / 100.0
        return """
            *Payment Reminder from $businessName*
            
            Dear *${customer.fullName}*,
            
            This is a friendly reminder that your outstanding udhaar balance of *₹$dueAmount* is currently pending.
            
            Please settle your payment using UPI to:
            👉 *UPI ID: $upiId*
            
            If already paid, please ignore this notice.
            _Smart Credit Automated Reminder_
        """.trimIndent()
    }
}

/**
 * Service for exporting customer credit history and ledger entries into standard CSV files.
 */
object CsvExportService {

    fun exportLedgerToCsv(
        context: Context,
        businessName: String,
        customer: Customer,
        transactions: List<LedgerTransaction>
    ): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileName = "Statement_${customer.fullName.replace(" ", "_")}_${customer.mobileNumber}.csv"
        val file = File(context.cacheDir, fileName)

        val csvBuilder = StringBuilder()
        csvBuilder.append("Smart Credit - Ledger Statement\n")
        csvBuilder.append("Merchant:,\"${businessName}\"\n")
        csvBuilder.append("Customer:,\"${customer.fullName}\"\n")
        csvBuilder.append("Mobile Number:,\"${customer.mobileNumber}\"\n")
        csvBuilder.append("Outstanding Balance:,\"₹${customer.currentBalancePaise / 100.0}\"\n\n")

        // Header Row
        csvBuilder.append("Transaction ID,Date & Time,Type,Channel,Description,Amount (INR),Balance After (INR),Status\n")

        for (txn in transactions) {
            val dateStr = dateFormat.format(Date(txn.timestamp))
            val amount = txn.amountPaise / 100.0
            val balance = txn.balanceAfterPaise / 100.0
            val status = if (txn.isReversed) "REVERSED" else "CONFIRMED"

            csvBuilder.append("\"${txn.id}\",")
            csvBuilder.append("\"$dateStr\",")
            csvBuilder.append("\"${txn.transactionType.name}\",")
            csvBuilder.append("\"${txn.paymentSource.name}\",")
            csvBuilder.append("\"${txn.description.replace("\"", "\"\"")}\",")
            csvBuilder.append("$amount,")
            csvBuilder.append("$balance,")
            csvBuilder.append("$status\n")
        }

        FileOutputStream(file).use { out ->
            out.write(csvBuilder.toString().toByteArray())
        }

        return file
    }
}
