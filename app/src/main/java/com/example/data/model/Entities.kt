package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Super Admin Profile and Permissions
 */
@Entity(tableName = "admin_profiles")
data class AdminProfile(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val role: AdminRole,
    val isPrimaryOwner: Boolean = false,
    val isActive: Boolean = true,
    val mfaEnabled: Boolean = true,
    val lastLoginTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AdminRole {
    SUPER_ADMIN,
    FINANCE_ADMIN,
    SUPPORT_ADMIN,
    TECHNICAL_ADMIN,
    AUDITOR
}

/**
 * Merchant Profile
 * Registration fields: Business Name, Address, Contact Number, Email, Authorised Person, Business UPI ID
 */
@Entity(tableName = "merchant_profiles")
data class MerchantProfile(
    @PrimaryKey val id: String, // e.g. "MERCH-1001"
    val businessName: String,
    val businessAddress: String,
    val contactNumber: String,
    val email: String,
    val authorisedPersonName: String,
    val businessUpiId: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val planType: SubscriptionPlan = SubscriptionPlan.FREE,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val suspensionReason: String? = null,
    val nextBillingDate: Long? = null,
    val customerCount: Int = 0,
    val totalCreditPaise: Long = 0L,
    val totalRepaymentPaise: Long = 0L,
    val platformFeesAccruedPaise: Long = 0L,
    val platformFeesSettledPaise: Long = 0L,
    val pendingSettlementPaise: Long = 0L,
    val autoPayEnabled: Boolean = false
)

enum class SubscriptionPlan {
    FREE,
    PREMIUM
}

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    RESTRICTED
}

/**
 * Customer profile under a specific merchant.
 * Note: Prompt requirement: Display the customer's mobile number as the displayed Customer ID.
 */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String, // Internal ID or mobile number
    val merchantId: String,
    val fullName: String,
    val mobileNumber: String,
    val fullAddress: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val openingBalancePaise: Long = 0L,
    val currentBalancePaise: Long = 0L,
    val totalCreditPaise: Long = 0L,
    val totalRepaymentPaise: Long = 0L,
    val notes: String = "",
    val dueDateTimestamp: Long? = null,
    val isArchived: Boolean = false,
    val autoPayMandateStatus: MandateStatus = MandateStatus.NOT_CONFIGURED,
    val autoPayMandateId: String? = null,
    val nextInstalmentDateTimestamp: Long? = null,
    val monthlyInstalmentPaise: Long = 0L
)

enum class MandateStatus {
    NOT_SET_UP,
    SETUP_REQUIRED,
    ACTIVATION_LINK_SENT,
    PENDING_CUSTOMER_APPROVAL,
    PENDING_PROVIDER_CONFIRMATION,
    ACTIVE,
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    CANCELLATION_REQUESTED,
    CANCELLED,
    EXPIRED,
    NOT_CONFIGURED
}

/**
 * Instalment Plan for customer credit
 */
@Entity(tableName = "instalment_plans")
data class InstalmentPlan(
    @PrimaryKey val id: String, // e.g. "PLAN-101"
    val merchantId: String,
    val customerId: String,
    val principalPaise: Long,
    val interestRatePercent: Double, // 0.0% to 10.0%
    val interestCalculationMethod: InterestMethod = InterestMethod.NO_INTEREST,
    val monthlyInstalmentPaise: Long,
    val firstInstalmentDate: Long,
    val monthlyDueDateDay: Int, // e.g. 5th of every month
    val totalInstalments: Int,
    val totalInterestPaise: Long,
    val totalPayablePaise: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isConfirmed: Boolean = true
)

enum class InterestMethod {
    NO_INTEREST,
    SIMPLE_INTEREST,
    REDUCING_BALANCE
}

/**
 * Individual instalment schedule item
 */
@Entity(tableName = "instalment_items")
data class InstalmentItem(
    @PrimaryKey val id: String, // e.g. "INST-101-1"
    val planId: String,
    val merchantId: String,
    val customerId: String,
    val instalmentNumber: Int,
    val dueDate: Long,
    val amountDuePaise: Long,
    val principalComponentPaise: Long,
    val interestComponentPaise: Long,
    val amountPaidPaise: Long = 0L,
    val status: InstalmentStatus = InstalmentStatus.UPCOMING
)

enum class InstalmentStatus {
    UPCOMING,
    DUE_TODAY,
    PAID,
    OVERDUE,
    PAYMENT_FAILED
}

/**
 * Append-only ledger transactions
 * Amounts stored in integer paise
 */
@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey val id: String,
    val merchantId: String,
    val customerId: String,
    val transactionType: TransactionType,
    val amountPaise: Long,
    val balanceAfterPaise: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val createdByUserId: String,
    val paymentSource: PaymentSource,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val referenceNumber: String? = null,
    val linkedReversalTxnId: String? = null,
    val isReversed: Boolean = false
)

enum class TransactionType {
    OPENING_BALANCE,
    CREDIT_SALE,
    CASH_REPAYMENT,
    ONLINE_REPAYMENT,
    DEBIT_ADJUSTMENT,
    CREDIT_ADJUSTMENT,
    REVERSAL
}

enum class PaymentSource {
    CASH,
    UPI_AUTOPAY,
    BANK_TRANSFER,
    ADJUSTMENT
}

enum class TransactionStatus {
    CONFIRMED,
    PENDING,
    REVERSED,
    FAILED
}

/**
 * Merchant Staff Accounts & Granular Permissions
 */
@Entity(tableName = "merchant_staff")
data class MerchantStaff(
    @PrimaryKey val id: String,
    val merchantId: String,
    val fullName: String,
    val mobileNumber: String,
    val roleName: String,
    val isActive: Boolean = true,
    val canViewCustomers: Boolean = true,
    val canAddCustomers: Boolean = false,
    val canEditCustomers: Boolean = false,
    val canArchiveCustomers: Boolean = false,
    val canGiveCredit: Boolean = false,
    val canRecordCashRepayments: Boolean = false,
    val canViewLedger: Boolean = true,
    val canGeneratePdf: Boolean = true,
    val canSharePdf: Boolean = true,
    val canInitiateAutoPay: Boolean = false,
    val canManageStaff: Boolean = false
)

/**
 * Central Remote Configuration & Branding
 */
@Entity(tableName = "app_configurations")
data class AppConfiguration(
    @PrimaryKey val id: Int = 1,
    val configVersion: Int = 1,
    val appDisplayName: String = "Smart Credit",
    val merchantBrandName: String = "Smart Credit Business",
    val primaryColorHex: String = "#0A192F", // Dark Navy
    val secondaryColorHex: String = "#0F766E", // Teal
    val accentColorHex: String = "#14B8A6", // Bright Teal
    val backgroundColorHex: String = "#F8FAFC",
    val surfaceColorHex: String = "#FFFFFF",
    val cardColorHex: String = "#FFFFFF",
    val textPrimaryHex: String = "#0A192F",
    val textSecondaryHex: String = "#64748B",
    val navBarColorHex: String = "#0A192F",
    val isDarkMode: Boolean = false,
    val visualStyle: String = "Rounded-Card Style",
    val premiumMonthlyPriceRupees: Int = 500,
    val freePlanPlatformFeePercent: Double = 2.0,
    val proposedSettlementUpiId: String = "9485144186-2@ybl",
    val settlementStatusNote: String = "Awaiting Payment Aggregator Marketplace Approval",
    val featureCustomerAutoPay: Boolean = true,
    val featureMerchantSubscriptionAutoPay: Boolean = true,
    val featurePdfStatements: Boolean = true,
    val featureWhatsAppShare: Boolean = true,
    val featureSmsNotifications: Boolean = true,
    val featureStaffManagement: Boolean = true,
    val maintenanceNotice: String = "",
    val promotionalBanner: String = "Welcome to Smart Credit — Digitise your customer credit & instalment management with peace of mind.",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val updatedByAdmin: String = "dr.saddam.co@gmail.com"
)

/**
 * Comprehensive Audit Log for every action
 */
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actorEmailOrId: String,
    val actorRole: String,
    val actionType: String,
    val targetEntity: String,
    val targetEntityId: String,
    val reasonOrDetails: String,
    val ipOrSessionInfo: String = "Secure Android Session"
)

/**
 * SMS Outbox / Delivery Log
 */
@Entity(tableName = "sms_logs")
data class SmsLog(
    @PrimaryKey val id: String,
    val merchantId: String,
    val recipientPhone: String,
    val messageText: String,
    val templateName: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val estimatedCostPaise: Long = 25L
)

/**
 * App Releases Metadata
 */
@Entity(tableName = "app_releases")
data class AppRelease(
    @PrimaryKey val versionCode: Int,
    val versionName: String,
    val releaseDate: Long = System.currentTimeMillis(),
    val isMandatory: Boolean = false,
    val releaseNotes: String,
    val minSupportedVersionCode: Int = 1,
    val publishedBy: String = "dr.saddam.co@gmail.com"
)
