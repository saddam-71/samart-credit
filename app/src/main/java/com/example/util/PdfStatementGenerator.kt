package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.data.model.MerchantProfile
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfStatementGenerator {

    /**
     * Generates a formal A4 Ledger Statement PDF with header, summary, and itemised transactions.
     * Saved to app cache directory and made shareable via FileProvider.
     */
    fun generateCustomerStatement(
        context: Context,
        merchant: MerchantProfile,
        customer: Customer,
        transactions: List<LedgerTransaction>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points (72 DPI)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val shortDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Header Background
        paint.color = Color.rgb(2, 132, 199) // #0284C7
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("SMART CREDIT — STATEMENT OF ACCOUNT", 30f, 40f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Merchant: ${merchant.businessName} (ID: ${merchant.id})", 30f, 62f, paint)
        canvas.drawText("Contact: ${merchant.contactNumber} | UPI: ${merchant.businessUpiId}", 30f, 78f, paint)

        // Customer Details Box
        paint.color = Color.rgb(241, 245, 249) // Slate-100
        canvas.drawRoundRect(30f, 105f, 565f, 185f, 8f, 8f, paint)

        paint.color = Color.rgb(15, 23, 42) // Slate-900
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Customer Information", 45f, 125f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Name: ${customer.fullName}", 45f, 145f, paint)
        canvas.drawText("Mobile: +91 ${customer.mobileNumber}", 45f, 160f, paint)
        canvas.drawText("Address: ${customer.fullAddress.take(55)}", 45f, 175f, paint)

        canvas.drawText("Customer ID: ${customer.id}", 330f, 145f, paint)
        canvas.drawText("Statement Date: ${dateFormat.format(Date())}", 330f, 160f, paint)
        paint.isFakeBoldText = true
        paint.textSize = 11f
        paint.color = if (customer.currentBalancePaise > 0) Color.rgb(185, 28, 28) else Color.rgb(21, 128, 61)
        canvas.drawText("Current Balance: ₹${customer.currentBalancePaise / 100.0}", 330f, 178f, paint)

        // Financial Summary Table
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("Total Credit: ₹${customer.totalCreditPaise / 100.0}    |    Total Repaid: ₹${customer.totalRepaymentPaise / 100.0}    |    Net Outstanding: ₹${customer.currentBalancePaise / 100.0}", 45f, 210f, paint)

        // Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, 225f, 565f, 248f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 40f, 241f, paint)
        canvas.drawText("Description & Ref", 110f, 241f, paint)
        canvas.drawText("Type", 320f, 241f, paint)
        canvas.drawText("Amount", 420f, 241f, paint)
        canvas.drawText("Balance", 500f, 241f, paint)

        // Table Rows
        var yPos = 268f
        paint.isFakeBoldText = false

        for (txn in transactions.take(20)) { // Single sheet statement up to 20 recent
            if (yPos > 760f) break

            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(shortDate.format(Date(txn.timestamp)), 40f, yPos, paint)
            canvas.drawText(txn.description.take(34), 110f, yPos, paint)

            val typeStr = when (txn.transactionType) {
                TransactionType.CREDIT_SALE -> "Credit (+)"
                TransactionType.CASH_REPAYMENT -> "Repaid (-)"
                TransactionType.ONLINE_REPAYMENT -> "Online (-)"
                TransactionType.REVERSAL -> "Reversal"
                else -> txn.transactionType.name
            }
            canvas.drawText(typeStr, 320f, yPos, paint)

            val amtColor = if (txn.transactionType == TransactionType.CREDIT_SALE) Color.rgb(185, 28, 28) else Color.rgb(21, 128, 61)
            paint.color = amtColor
            canvas.drawText("₹${txn.amountPaise / 100.0}", 420f, yPos, paint)

            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("₹${txn.balanceAfterPaise / 100.0}", 500f, yPos, paint)

            // Divider
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(30f, yPos + 6f, 565f, yPos + 6f, paint)

            yPos += 24f
        }

        // Disclaimer & Legal Notice
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("Disclaimer: This statement is an authenticated computer-generated record from Smart Credit.", 30f, 800f, paint)
        canvas.drawText("Cash entries recorded by merchant staff. Online AutoPay entries are bank/aggregator verified.", 30f, 814f, paint)
        canvas.drawText("Authorised by: ${merchant.authorisedPersonName} (${merchant.businessName})", 30f, 828f, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Statement_${customer.id}_${System.currentTimeMillis()}.pdf")
        val outStream = FileOutputStream(outputFile)
        pdfDocument.writeTo(outStream)
        outStream.flush()
        outStream.close()
        pdfDocument.close()

        return outputFile
    }

    fun createShareIntent(context: Context, pdfFile: File, customerPhone: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Smart Credit Statement")
            putExtra(Intent.EXTRA_TEXT, "Hello, please find attached your Smart Credit account statement. Kindly review and settle any pending balance.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
