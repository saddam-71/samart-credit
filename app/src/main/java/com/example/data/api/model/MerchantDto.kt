package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network Data Transfer Object (DTO) for Merchant registration, profile retrieval, and status updates.
 * Serialized/Deserialized using Moshi.
 */
@JsonClass(generateAdapter = true)
data class MerchantDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "business_name")
    val businessName: String,

    @Json(name = "business_address")
    val businessAddress: String,

    @Json(name = "contact_number")
    val contactNumber: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "authorised_person_name")
    val authorisedPersonName: String,

    @Json(name = "business_upi_id")
    val businessUpiId: String,

    @Json(name = "registration_date")
    val registrationDate: Long = System.currentTimeMillis(),

    @Json(name = "plan_type")
    val planType: String = "FREE", // "FREE" or "PREMIUM"

    @Json(name = "account_status")
    val accountStatus: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "RESTRICTED"

    @Json(name = "suspension_reason")
    val suspensionReason: String? = null,

    @Json(name = "next_billing_date")
    val nextBillingDate: Long? = null,

    @Json(name = "customer_count")
    val customerCount: Int = 0,

    @Json(name = "total_credit_paise")
    val totalCreditPaise: Long = 0L,

    @Json(name = "total_repayment_paise")
    val totalRepaymentPaise: Long = 0L,

    @Json(name = "platform_fees_accrued_paise")
    val platformFeesAccruedPaise: Long = 0L,

    @Json(name = "platform_fees_settled_paise")
    val platformFeesSettledPaise: Long = 0L,

    @Json(name = "pending_settlement_paise")
    val pendingSettlementPaise: Long = 0L,

    @Json(name = "autopay_enabled")
    val autoPayEnabled: Boolean = false
)

/**
 * Request payload when creating/registering a new merchant (the 6 required business fields)
 */
@JsonClass(generateAdapter = true)
data class CreateMerchantRequest(
    @Json(name = "business_name")
    val businessName: String,

    @Json(name = "business_address")
    val businessAddress: String,

    @Json(name = "contact_number")
    val contactNumber: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "authorised_person_name")
    val authorisedPersonName: String,

    @Json(name = "business_upi_id")
    val businessUpiId: String
)

/**
 * Request payload for updating merchant account status (e.g. by Super Admin)
 */
@JsonClass(generateAdapter = true)
data class UpdateMerchantStatusRequest(
    @Json(name = "account_status")
    val accountStatus: String,

    @Json(name = "reason")
    val reason: String
)

/**
 * Generic API response envelope
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "data")
    val data: T? = null
)
