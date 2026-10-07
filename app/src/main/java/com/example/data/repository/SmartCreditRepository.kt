package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.db.SmartCreditDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class SmartCreditRepository private constructor(context: Context) {

    private val database: SmartCreditDatabase = Room.databaseBuilder(
        context.applicationContext,
        SmartCreditDatabase::class.java,
        "smart_credit_db"
    ).fallbackToDestructiveMigration().build()

    private val adminDao = database.adminDao()
    private val merchantDao = database.merchantDao()
    private val customerDao = database.customerDao()
    private val ledgerDao = database.ledgerDao()
    private val instalmentDao = database.instalmentDao()
    private val staffDao = database.staffDao()
    private val configDao = database.configDao()
    private val auditDao = database.auditDao()
    private val smsDao = database.smsDao()
    private val releaseDao = database.releaseDao()

    init {
        // Pre-populate with owner Super Admin and standard configuration if fresh database
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val existingAdmin = adminDao.getAdminByEmail("dr.saddam.co@gmail.com")
        if (existingAdmin == null) {
            val owner = AdminProfile(
                id = "ADMIN-PRIMARY-001",
                email = "dr.saddam.co@gmail.com",
                fullName = "Dr. Saddam (Primary Owner)",
                role = AdminRole.SUPER_ADMIN,
                isPrimaryOwner = true,
                isActive = true,
                mfaEnabled = true,
                createdAt = System.currentTimeMillis()
            )
            adminDao.insertAdmin(owner)
        }

        val existingConfig = configDao.getAppConfigSync()
        if (existingConfig == null) {
            val defaultConfig = AppConfiguration()
            configDao.insertOrUpdateConfig(defaultConfig)
        }

        // Add initial system release
        val initialRelease = AppRelease(
            versionCode = 1,
            versionName = "1.0.0",
            releaseNotes = "Initial production release of Smart Credit with Super Admin customisation and full merchant ledger.",
            isMandatory = false
        )
        releaseDao.insertRelease(initialRelease)

        // Seed sample merchant if none exists
        val merchants = merchantDao.getMerchantById("MERCH-1001")
        if (merchants == null) {
            val sampleMerchant = MerchantProfile(
                id = "MERCH-1001",
                businessName = "Apex Retail Traders",
                businessAddress = "Shop 14, MG Road, Connaught Place, New Delhi 110001",
                contactNumber = "9876543210",
                email = "apexretail@example.com",
                authorisedPersonName = "Rajesh Sharma",
                businessUpiId = "apexretail@okaxis",
                planType = SubscriptionPlan.FREE,
                accountStatus = AccountStatus.ACTIVE,
                customerCount = 2,
                totalCreditPaise = 150000L, // ₹1,500
                totalRepaymentPaise = 50000L,  // ₹500
                platformFeesAccruedPaise = 1000L, // 2% of 500 = ₹10
                platformFeesSettledPaise = 0L,
                pendingSettlementPaise = 1000L
            )
            merchantDao.insertMerchant(sampleMerchant)

            val cust1 = Customer(
                id = "CUST-901",
                merchantId = "MERCH-1001",
                fullName = "Amit Verma",
                mobileNumber = "9811122233",
                fullAddress = "Flat 4B, Sky Heights, Sector 18, Noida, Uttar Pradesh, 201301",
                openingBalancePaise = 0L,
                currentBalancePaise = 100000L, // ₹1,000
                totalCreditPaise = 150000L,
                totalRepaymentPaise = 50000L,
                notes = "Regular grocery buyer. Payment due every Saturday."
            )
            val cust2 = Customer(
                id = "CUST-902",
                merchantId = "MERCH-1001",
                fullName = "Priya Sundaram",
                mobileNumber = "9844455566",
                fullAddress = "23 Indiranagar, 1st Stage, Bangalore, Karnataka, 560038",
                openingBalancePaise = 0L,
                currentBalancePaise = 0L,
                totalCreditPaise = 0L,
                totalRepaymentPaise = 0L,
                notes = "New customer account."
            )
            customerDao.insertCustomer(cust1)
            customerDao.insertCustomer(cust2)

            val txn1 = LedgerTransaction(
                id = "TXN-101",
                merchantId = "MERCH-1001",
                customerId = "CUST-901",
                transactionType = TransactionType.CREDIT_SALE,
                amountPaise = 150000L,
                balanceAfterPaise = 150000L,
                description = "Provisions & Rice Bags (3x 25kg)",
                createdByUserId = "MERCH-1001",
                paymentSource = PaymentSource.CASH
            )
            val txn2 = LedgerTransaction(
                id = "TXN-102",
                merchantId = "MERCH-1001",
                customerId = "CUST-901",
                transactionType = TransactionType.CASH_REPAYMENT,
                amountPaise = 50000L,
                balanceAfterPaise = 100000L,
                description = "Cash repayment received at counter",
                createdByUserId = "MERCH-1001",
                paymentSource = PaymentSource.CASH
            )
            ledgerDao.insertTransaction(txn1)
            ledgerDao.insertTransaction(txn2)

            val staff1 = MerchantStaff(
                id = "STAFF-01",
                merchantId = "MERCH-1001",
                fullName = "Sunil Kumar",
                mobileNumber = "9877788899",
                roleName = "Counter Cashier",
                isActive = true,
                canViewCustomers = true,
                canAddCustomers = true,
                canEditCustomers = false,
                canArchiveCustomers = false,
                canGiveCredit = true,
                canRecordCashRepayments = true,
                canViewLedger = true,
                canGeneratePdf = true,
                canSharePdf = true,
                canInitiateAutoPay = false,
                canManageStaff = false
            )
            staffDao.insertStaff(staff1)

            auditDao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    actorEmailOrId = "SYSTEM_INITIALIZER",
                    actorRole = "SUPER_ADMIN",
                    actionType = "DATABASE_BOOTSTRAP",
                    targetEntity = "SYSTEM",
                    targetEntityId = "INITIAL_STATE",
                    reasonOrDetails = "Database seeded with authenticated entities & verification test data"
                )
            )
        }
    }

    // Config flows & updates
    fun getAppConfiguration(): Flow<AppConfiguration?> = configDao.getAppConfiguration()
    suspend fun updateAppConfiguration(config: AppConfiguration, actorEmail: String, reason: String) {
        val updated = config.copy(
            configVersion = config.configVersion + 1,
            lastUpdatedTimestamp = System.currentTimeMillis(),
            updatedByAdmin = actorEmail
        )
        configDao.insertOrUpdateConfig(updated)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actorEmail,
                actorRole = "SUPER_ADMIN",
                actionType = "CONFIG_UPDATE",
                targetEntity = "APP_CONFIGURATION",
                targetEntityId = "v${updated.configVersion}",
                reasonOrDetails = reason
            )
        )
    }

    // Admin & Auth
    suspend fun getAdminByEmail(email: String): AdminProfile? = adminDao.getAdminByEmail(email)
    fun getAllAdmins(): Flow<List<AdminProfile>> = adminDao.getAllAdmins()
    suspend fun insertAdmin(admin: AdminProfile) = adminDao.insertAdmin(admin)
    suspend fun updateAdmin(admin: AdminProfile) = adminDao.updateAdmin(admin)

    // Merchant management
    fun getAllMerchants(): Flow<List<MerchantProfile>> = merchantDao.getAllMerchants()
    suspend fun getMerchantById(id: String): MerchantProfile? = merchantDao.getMerchantById(id)
    suspend fun getMerchantByPhone(phone: String): MerchantProfile? = merchantDao.getMerchantByPhone(phone)
    suspend fun registerMerchant(merchant: MerchantProfile) {
        merchantDao.insertMerchant(merchant)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = merchant.contactNumber,
                actorRole = "MERCHANT",
                actionType = "MERCHANT_REGISTER",
                targetEntity = "MERCHANT",
                targetEntityId = merchant.id,
                reasonOrDetails = "New merchant onboarding for ${merchant.businessName}"
            )
        )
    }
    suspend fun updateMerchantStatus(merchantId: String, newStatus: AccountStatus, reason: String, adminActor: String) {
        val current = merchantDao.getMerchantById(merchantId) ?: return
        val updated = current.copy(
            accountStatus = newStatus,
            suspensionReason = if (newStatus != AccountStatus.ACTIVE) reason else null
        )
        merchantDao.updateMerchant(updated)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = adminActor,
                actorRole = "SUPER_ADMIN",
                actionType = "MERCHANT_STATUS_CHANGE_${newStatus.name}",
                targetEntity = "MERCHANT",
                targetEntityId = merchantId,
                reasonOrDetails = reason
            )
        )
    }
    suspend fun updateMerchantProfile(merchant: MerchantProfile, actor: String, reason: String) {
        merchantDao.updateMerchant(merchant)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "ADMIN",
                actionType = "MERCHANT_PROFILE_EDIT",
                targetEntity = "MERCHANT",
                targetEntityId = merchant.id,
                reasonOrDetails = reason
            )
        )
    }

    // Customers
    fun getCustomersForMerchant(merchantId: String): Flow<List<Customer>> = customerDao.getCustomersForMerchant(merchantId)
    suspend fun getCustomersForMerchantSync(merchantId: String): List<Customer> = customerDao.getCustomersForMerchantSync(merchantId)
    suspend fun getTransactionsForMerchantSync(merchantId: String): List<LedgerTransaction> = ledgerDao.getTransactionsForMerchantSync(merchantId)
    suspend fun insertOrUpdateCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    fun searchCustomers(merchantId: String, query: String): Flow<List<Customer>> = customerDao.searchCustomers(merchantId, query)
    suspend fun getCustomerById(id: String): Customer? = customerDao.getCustomerById(id)
    fun getAllCustomersGlobal(): Flow<List<Customer>> = customerDao.getAllCustomersGlobal()

    suspend fun addCustomer(customer: Customer, actor: String) {
        customerDao.insertCustomer(customer)
        // update merchant customer count
        val merchant = merchantDao.getMerchantById(customer.merchantId)
        if (merchant != null) {
            merchantDao.updateMerchant(merchant.copy(customerCount = merchant.customerCount + 1))
        }
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT_STAFF",
                actionType = "CUSTOMER_ADD",
                targetEntity = "CUSTOMER",
                targetEntityId = customer.id,
                reasonOrDetails = "Customer profile added: ${customer.fullName}"
            )
        )
    }

    suspend fun updateCustomer(customer: Customer, actor: String, reason: String) {
        customerDao.updateCustomer(customer)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT_STAFF",
                actionType = "CUSTOMER_UPDATE",
                targetEntity = "CUSTOMER",
                targetEntityId = customer.id,
                reasonOrDetails = reason
            )
        )
    }

    // Ledger transactions (strictly atomic & append-only)
    fun getTransactionsForCustomer(customerId: String): Flow<List<LedgerTransaction>> = ledgerDao.getTransactionsForCustomer(customerId)
    fun getTransactionsForMerchant(merchantId: String): Flow<List<LedgerTransaction>> = ledgerDao.getTransactionsForMerchant(merchantId)
    fun getAllTransactionsGlobal(): Flow<List<LedgerTransaction>> = ledgerDao.getAllTransactionsGlobal()

    suspend fun recordCreditSale(
        merchantId: String,
        customerId: String,
        amountPaise: Long,
        description: String,
        createdByUserId: String
    ): LedgerTransaction {
        val customer = customerDao.getCustomerById(customerId) ?: throw IllegalArgumentException("Customer not found")
        val merchant = merchantDao.getMerchantById(merchantId) ?: throw IllegalArgumentException("Merchant not found")

        val newBalance = customer.currentBalancePaise + amountPaise
        val txnId = "TXN-${System.currentTimeMillis()}-${(100..999).random()}"
        val txn = LedgerTransaction(
            id = txnId,
            merchantId = merchantId,
            customerId = customerId,
            transactionType = TransactionType.CREDIT_SALE,
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            description = description,
            createdByUserId = createdByUserId,
            paymentSource = PaymentSource.CASH
        )
        ledgerDao.insertTransaction(txn)

        customerDao.updateCustomer(
            customer.copy(
                currentBalancePaise = newBalance,
                totalCreditPaise = customer.totalCreditPaise + amountPaise
            )
        )

        merchantDao.updateMerchant(
            merchant.copy(
                totalCreditPaise = merchant.totalCreditPaise + amountPaise
            )
        )

        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = createdByUserId,
                actorRole = "MERCHANT",
                actionType = "CREDIT_SALE_RECORDED",
                targetEntity = "LEDGER",
                targetEntityId = txnId,
                reasonOrDetails = "Amount: ₹${amountPaise / 100.0} for ${customer.fullName}"
            )
        )
        return txn
    }

    suspend fun recordRepayment(
        merchantId: String,
        customerId: String,
        amountPaise: Long,
        description: String,
        source: PaymentSource,
        createdByUserId: String,
        currentConfig: AppConfiguration
    ): LedgerTransaction {
        val customer = customerDao.getCustomerById(customerId) ?: throw IllegalArgumentException("Customer not found")
        val merchant = merchantDao.getMerchantById(merchantId) ?: throw IllegalArgumentException("Merchant not found")

        val newBalance = customer.currentBalancePaise - amountPaise
        val txnId = "TXN-${System.currentTimeMillis()}-${(100..999).random()}"
        val txn = LedgerTransaction(
            id = txnId,
            merchantId = merchantId,
            customerId = customerId,
            transactionType = if (source == PaymentSource.CASH) TransactionType.CASH_REPAYMENT else TransactionType.ONLINE_REPAYMENT,
            amountPaise = amountPaise,
            balanceAfterPaise = newBalance,
            description = description,
            createdByUserId = createdByUserId,
            paymentSource = source
        )
        ledgerDao.insertTransaction(txn)

        customerDao.updateCustomer(
            customer.copy(
                currentBalancePaise = newBalance,
                totalRepaymentPaise = customer.totalRepaymentPaise + amountPaise
            )
        )

        // Platform fee calculation:
        // Free plan: admin configurable fee% (e.g. 2%)
        // Premium plan: 0% fee
        val feeRate = if (merchant.planType == SubscriptionPlan.FREE) {
            currentConfig.freePlanPlatformFeePercent / 100.0
        } else {
            0.0
        }
        val feePaise = (amountPaise * feeRate).toLong()

        merchantDao.updateMerchant(
            merchant.copy(
                totalRepaymentPaise = merchant.totalRepaymentPaise + amountPaise,
                platformFeesAccruedPaise = merchant.platformFeesAccruedPaise + feePaise,
                pendingSettlementPaise = merchant.pendingSettlementPaise + feePaise
            )
        )

        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = createdByUserId,
                actorRole = "MERCHANT",
                actionType = "REPAYMENT_RECORDED",
                targetEntity = "LEDGER",
                targetEntityId = txnId,
                reasonOrDetails = "Repayment ₹${amountPaise / 100.0} (Source: ${source.name}). Accrued Platform Fee: ₹${feePaise / 100.0}"
            )
        )
        return txn
    }

    // Reversal entry (Audited correction without deleting history)
    suspend fun reverseTransaction(
        txnId: String,
        reason: String,
        actor: String
    ): LedgerTransaction {
        val orig = ledgerDao.getTransactionById(txnId) ?: throw IllegalArgumentException("Transaction not found")
        if (orig.isReversed) throw IllegalStateException("Transaction has already been reversed")

        val customer = customerDao.getCustomerById(orig.customerId) ?: throw IllegalArgumentException("Customer not found")
        val merchant = merchantDao.getMerchantById(orig.merchantId) ?: throw IllegalArgumentException("Merchant not found")

        // Invert effect
        val isCredit = orig.transactionType == TransactionType.CREDIT_SALE
        val delta = if (isCredit) -orig.amountPaise else orig.amountPaise
        val newBalance = customer.currentBalancePaise + delta

        val reversalTxnId = "REV-${System.currentTimeMillis()}-${(100..999).random()}"
        val reversalTxn = LedgerTransaction(
            id = reversalTxnId,
            merchantId = orig.merchantId,
            customerId = orig.customerId,
            transactionType = TransactionType.REVERSAL,
            amountPaise = orig.amountPaise,
            balanceAfterPaise = newBalance,
            description = "REVERSAL of ${orig.id}: $reason",
            createdByUserId = actor,
            paymentSource = PaymentSource.ADJUSTMENT,
            linkedReversalTxnId = orig.id
        )

        ledgerDao.insertTransaction(reversalTxn)
        ledgerDao.updateTransaction(orig.copy(isReversed = true, linkedReversalTxnId = reversalTxnId))

        customerDao.updateCustomer(customer.copy(currentBalancePaise = newBalance))

        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "ADMIN_OR_MERCHANT",
                actionType = "TRANSACTION_REVERSAL",
                targetEntity = "LEDGER",
                targetEntityId = orig.id,
                reasonOrDetails = "Reversal logged under $reversalTxnId. Reason: $reason"
            )
        )
        return reversalTxn
    }

    // Instalment Plans & Schedules
    fun getPlansForCustomer(customerId: String): Flow<List<InstalmentPlan>> = instalmentDao.getPlansForCustomer(customerId)
    fun getPlansForMerchant(merchantId: String): Flow<List<InstalmentPlan>> = instalmentDao.getPlansForMerchant(merchantId)
    fun getInstalmentItemsForMerchant(merchantId: String): Flow<List<InstalmentItem>> = instalmentDao.getInstalmentItemsForMerchant(merchantId)
    fun getInstalmentItemsForCustomer(customerId: String): Flow<List<InstalmentItem>> = instalmentDao.getInstalmentItemsForCustomer(customerId)

    suspend fun createInstalmentPlan(
        plan: InstalmentPlan,
        items: List<InstalmentItem>,
        actor: String
    ) {
        instalmentDao.insertPlan(plan)
        instalmentDao.insertInstalmentItems(items)

        // update customer with next instalment info
        val customer = customerDao.getCustomerById(plan.customerId)
        if (customer != null) {
            customerDao.updateCustomer(
                customer.copy(
                    nextInstalmentDateTimestamp = plan.firstInstalmentDate,
                    monthlyInstalmentPaise = plan.monthlyInstalmentPaise
                )
            )
        }

        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT",
                actionType = "INSTALMENT_PLAN_CREATED",
                targetEntity = "INSTALMENT_PLAN",
                targetEntityId = plan.id,
                reasonOrDetails = "Created ${plan.totalInstalments}-month plan for customer ${plan.customerId}. Monthly: ₹${plan.monthlyInstalmentPaise / 100.0}"
            )
        )
    }

    suspend fun updateCustomerMandateStatus(
        customerId: String,
        status: MandateStatus,
        mandateId: String?,
        actor: String
    ) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        customerDao.updateCustomer(
            customer.copy(
                autoPayMandateStatus = status,
                autoPayMandateId = mandateId ?: customer.autoPayMandateId
            )
        )
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT_OR_PROVIDER",
                actionType = "MANDATE_STATUS_UPDATED",
                targetEntity = "AUTOPAY_MANDATE",
                targetEntityId = customerId,
                reasonOrDetails = "Mandate status changed to ${status.name} (Ref: ${mandateId ?: "N/A"})"
            )
        )
    }

    // Staff
    fun getStaffForMerchant(merchantId: String): Flow<List<MerchantStaff>> = staffDao.getStaffForMerchant(merchantId)
    suspend fun addStaff(staff: MerchantStaff, actor: String) {
        staffDao.insertStaff(staff)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT",
                actionType = "STAFF_ADD",
                targetEntity = "MERCHANT_STAFF",
                targetEntityId = staff.id,
                reasonOrDetails = "Staff created: ${staff.fullName} (${staff.roleName})"
            )
        )
    }
    suspend fun updateStaff(staff: MerchantStaff, actor: String, reason: String) {
        staffDao.updateStaff(staff)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "MERCHANT",
                actionType = "STAFF_PERMISSIONS_UPDATE",
                targetEntity = "MERCHANT_STAFF",
                targetEntityId = staff.id,
                reasonOrDetails = reason
            )
        )
    }

    // Audit logs
    fun getAllAuditLogs(): Flow<List<AuditLog>> = auditDao.getAllAuditLogs()

    // SMS logs
    fun getSmsLogsForMerchant(merchantId: String): Flow<List<SmsLog>> = smsDao.getSmsLogsForMerchant(merchantId)
    fun getAllSmsLogs(): Flow<List<SmsLog>> = smsDao.getAllSmsLogs()
    suspend fun recordSmsLog(log: SmsLog) = smsDao.insertSmsLog(log)

    // Releases
    fun getAllReleases(): Flow<List<AppRelease>> = releaseDao.getAllReleases()
    suspend fun publishRelease(release: AppRelease, actor: String) {
        releaseDao.insertRelease(release)
        auditDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorEmailOrId = actor,
                actorRole = "SUPER_ADMIN",
                actionType = "RELEASE_PUBLISHED",
                targetEntity = "APP_RELEASE",
                targetEntityId = "v${release.versionName} (${release.versionCode})",
                reasonOrDetails = release.releaseNotes
            )
        )
    }

    companion object {
        @Volatile
        private var instance: SmartCreditRepository? = null

        fun getInstance(context: Context): SmartCreditRepository {
            return instance ?: synchronized(this) {
                instance ?: SmartCreditRepository(context).also { instance = it }
            }
        }
    }
}
