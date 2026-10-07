package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.data.model.PaymentSource
import com.example.data.model.TransactionType
import com.example.data.repository.SmartCreditRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service handling cloud synchronization with Firestore for merchant customers and ledger transactions.
 */
class CloudSyncService(private val repository: SmartCreditRepository) {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    companion object {
        private const val TAG = "CloudSyncService"
    }

    /**
     * Back up all local customers and ledger entries to Cloud Firestore
     */
    suspend fun backupMerchantDataToCloud(merchantId: String): Result<SyncSummary> = withContext(Dispatchers.IO) {
        try {
            val merchant = repository.getMerchantById(merchantId)
                ?: return@withContext Result.failure(IllegalStateException("Merchant not found"))

            var customersSynced = 0
            var txnsSynced = 0

            // 1. Sync Customers
            val customers = repository.getCustomersForMerchantSync(merchantId)
            val customersCol = firestore.collection("merchants").document(merchantId).collection("customers")

            for (cust in customers) {
                val data = hashMapOf(
                    "id" to cust.id,
                    "merchantId" to cust.merchantId,
                    "fullName" to cust.fullName,
                    "mobileNumber" to cust.mobileNumber,
                    "fullAddress" to cust.fullAddress,
                    "currentBalancePaise" to cust.currentBalancePaise,
                    "totalCreditPaise" to cust.totalCreditPaise,
                    "totalRepaymentPaise" to cust.totalRepaymentPaise,
                    "autoPayMandateStatus" to cust.autoPayMandateStatus.name,
                    "autoPayMandateId" to (cust.autoPayMandateId ?: ""),
                    "updatedAt" to System.currentTimeMillis()
                )
                customersCol.document(cust.id).set(data).await()
                customersSynced++
            }

            // 2. Sync Ledger Transactions
            val transactions = repository.getTransactionsForMerchantSync(merchantId)
            val txnsCol = firestore.collection("merchants").document(merchantId).collection("transactions")

            for (txn in transactions) {
                val data = hashMapOf(
                    "id" to txn.id,
                    "merchantId" to txn.merchantId,
                    "customerId" to txn.customerId,
                    "transactionType" to txn.transactionType.name,
                    "amountPaise" to txn.amountPaise,
                    "balanceAfterPaise" to txn.balanceAfterPaise,
                    "description" to txn.description,
                    "paymentSource" to txn.paymentSource.name,
                    "timestamp" to txn.timestamp,
                    "isReversed" to txn.isReversed
                )
                txnsCol.document(txn.id).set(data).await()
                txnsSynced++
            }

            // Record backup metadata
            val metadata = hashMapOf(
                "lastBackupTimestamp" to System.currentTimeMillis(),
                "customerCount" to customersSynced,
                "transactionCount" to txnsSynced,
                "status" to "SUCCESS"
            )
            firestore.collection("merchants").document(merchantId)
                .collection("backup_metadata").document("latest").set(metadata).await()

            Result.success(SyncSummary(customersSynced, txnsSynced, System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.e(TAG, "Cloud backup failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Restore cloud data from Firestore into local Room database
     */
    suspend fun restoreMerchantDataFromCloud(merchantId: String): Result<SyncSummary> = withContext(Dispatchers.IO) {
        try {
            var customersRestored = 0
            var txnsRestored = 0

            val customersSnapshot = firestore.collection("merchants").document(merchantId)
                .collection("customers").get().await()

            for (doc in customersSnapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val fullName = doc.getString("fullName") ?: "Customer"
                val mobile = doc.getString("mobileNumber") ?: ""
                val address = doc.getString("fullAddress") ?: ""
                val currentBal = doc.getLong("currentBalancePaise") ?: 0L
                val totalCredit = doc.getLong("totalCreditPaise") ?: 0L
                val totalRepay = doc.getLong("totalRepaymentPaise") ?: 0L

                val customer = Customer(
                    id = id,
                    merchantId = merchantId,
                    fullName = fullName,
                    mobileNumber = mobile,
                    fullAddress = address,
                    currentBalancePaise = currentBal,
                    totalCreditPaise = totalCredit,
                    totalRepaymentPaise = totalRepay
                )
                repository.insertOrUpdateCustomer(customer)
                customersRestored++
            }

            Result.success(SyncSummary(customersRestored, txnsRestored, System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.e(TAG, "Cloud restore failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

data class SyncSummary(
    val customersCount: Int,
    val transactionsCount: Int,
    val timestamp: Long
)
