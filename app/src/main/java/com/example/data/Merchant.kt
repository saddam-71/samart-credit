package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class representing a Merchant profile with Moshi JSON serialization annotations.
 */
@JsonClass(generateAdapter = true)
data class Merchant(
    @Json(name = "merchant_id")
    val merchantId: String,

    @Json(name = "business_name")
    val businessName: String,

    @Json(name = "business_address")
    val businessAddress: String,

    @Json(name = "business_contact")
    val businessContact: String,

    @Json(name = "business_email")
    val businessEmail: String,

    @Json(name = "authorized_person")
    val authorizedPerson: String,

    @Json(name = "upi_id")
    val upiId: String
)
