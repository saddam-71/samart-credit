package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_profiles WHERE id = :id")
    suspend fun getAdminById(id: String): AdminProfile?

    @Query("SELECT * FROM admin_profiles WHERE email = :email LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminProfile?

    @Query("SELECT * FROM admin_profiles ORDER BY createdAt ASC")
    fun getAllAdmins(): Flow<List<AdminProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminProfile)

    @Update
    suspend fun updateAdmin(admin: AdminProfile)
}

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchant_profiles ORDER BY registrationDate DESC")
    fun getAllMerchants(): Flow<List<MerchantProfile>>

    @Query("SELECT * FROM merchant_profiles WHERE id = :id")
    suspend fun getMerchantById(id: String): MerchantProfile?

    @Query("SELECT * FROM merchant_profiles WHERE contactNumber = :phone LIMIT 1")
    suspend fun getMerchantByPhone(phone: String): MerchantProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantProfile)

    @Update
    suspend fun updateMerchant(merchant: MerchantProfile)

    @Query("SELECT COUNT(*) FROM merchant_profiles")
    fun getMerchantCount(): Flow<Int>
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE merchantId = :merchantId AND isArchived = 0 ORDER BY registrationDate DESC")
    fun getCustomersForMerchant(merchantId: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE merchantId = :merchantId AND isArchived = 0 AND (fullName LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%')")
    fun searchCustomers(merchantId: String, query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): Customer?

    @Query("SELECT * FROM customers WHERE merchantId = :merchantId AND isArchived = 0")
    suspend fun getCustomersForMerchantSync(merchantId: String): List<Customer>

    @Query("SELECT * FROM customers ORDER BY registrationDate DESC")
    fun getAllCustomersGlobal(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers WHERE merchantId = :merchantId AND isArchived = 0")
    fun getCustomerCountForMerchant(merchantId: String): Flow<Int>
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: String): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun getTransactionsForMerchant(merchantId: String): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE merchantId = :merchantId ORDER BY timestamp ASC")
    suspend fun getTransactionsForMerchantSync(merchantId: String): List<LedgerTransaction>

    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsGlobal(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): LedgerTransaction?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: LedgerTransaction)

    @Update
    suspend fun updateTransaction(transaction: LedgerTransaction)
}

@Dao
interface InstalmentDao {
    @Query("SELECT * FROM instalment_plans WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getPlansForCustomer(customerId: String): Flow<List<InstalmentPlan>>

    @Query("SELECT * FROM instalment_plans WHERE merchantId = :merchantId ORDER BY createdAt DESC")
    fun getPlansForMerchant(merchantId: String): Flow<List<InstalmentPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: InstalmentPlan)

    @Query("SELECT * FROM instalment_items WHERE merchantId = :merchantId ORDER BY dueDate ASC")
    fun getInstalmentItemsForMerchant(merchantId: String): Flow<List<InstalmentItem>>

    @Query("SELECT * FROM instalment_items WHERE customerId = :customerId ORDER BY dueDate ASC")
    fun getInstalmentItemsForCustomer(customerId: String): Flow<List<InstalmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstalmentItems(items: List<InstalmentItem>)

    @Update
    suspend fun updateInstalmentItem(item: InstalmentItem)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM merchant_staff WHERE merchantId = :merchantId ORDER BY fullName ASC")
    fun getStaffForMerchant(merchantId: String): Flow<List<MerchantStaff>>

    @Query("SELECT * FROM merchant_staff WHERE id = :id")
    suspend fun getStaffById(id: String): MerchantStaff?

    @Query("SELECT * FROM merchant_staff WHERE mobileNumber = :phone LIMIT 1")
    suspend fun getStaffByPhone(phone: String): MerchantStaff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: MerchantStaff)

    @Update
    suspend fun updateStaff(staff: MerchantStaff)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_configurations WHERE id = 1")
    fun getAppConfiguration(): Flow<AppConfiguration?>

    @Query("SELECT * FROM app_configurations WHERE id = 1")
    suspend fun getAppConfigSync(): AppConfiguration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppConfiguration)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE targetEntity = :entity ORDER BY timestamp DESC")
    fun getAuditLogsForEntity(entity: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_logs WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun getSmsLogsForMerchant(merchantId: String): Flow<List<SmsLog>>

    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllSmsLogs(): Flow<List<SmsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(log: SmsLog)
}

@Dao
interface ReleaseDao {
    @Query("SELECT * FROM app_releases ORDER BY versionCode DESC")
    fun getAllReleases(): Flow<List<AppRelease>>

    @Query("SELECT * FROM app_releases ORDER BY versionCode DESC LIMIT 1")
    fun getLatestRelease(): Flow<AppRelease?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelease(release: AppRelease)
}

@Database(
    entities = [
        AdminProfile::class,
        MerchantProfile::class,
        Customer::class,
        LedgerTransaction::class,
        InstalmentPlan::class,
        InstalmentItem::class,
        MerchantStaff::class,
        AppConfiguration::class,
        AuditLog::class,
        SmsLog::class,
        AppRelease::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SmartCreditDatabase : RoomDatabase() {
    abstract fun adminDao(): AdminDao
    abstract fun merchantDao(): MerchantDao
    abstract fun customerDao(): CustomerDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun instalmentDao(): InstalmentDao
    abstract fun staffDao(): StaffDao
    abstract fun configDao(): ConfigDao
    abstract fun auditDao(): AuditDao
    abstract fun smsDao(): SmsDao
    abstract fun releaseDao(): ReleaseDao
}
