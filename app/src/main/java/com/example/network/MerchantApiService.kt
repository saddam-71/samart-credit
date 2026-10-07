package com.example.network

import com.example.data.Merchant
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service Interface for Merchant operations
 * using the [Merchant] data class.
 */
interface MerchantApiService {

    /**
     * Register a new merchant profile.
     */
    @POST("api/v1/merchants")
    suspend fun registerMerchant(
        @Body merchant: Merchant
    ): Response<Merchant>

    /**
     * Fetch a merchant profile by their unique merchant ID.
     */
    @GET("api/v1/merchants/{merchantId}")
    suspend fun getMerchant(
        @Path("merchantId") merchantId: String
    ): Response<Merchant>

    /**
     * Fetch a merchant profile by their business contact number.
     */
    @GET("api/v1/merchants/by-contact")
    suspend fun getMerchantByContact(
        @Query("contact") contact: String
    ): Response<Merchant>

    /**
     * Fetch all registered merchants.
     */
    @GET("api/v1/merchants")
    suspend fun getAllMerchants(): Response<List<Merchant>>

    /**
     * Update an existing merchant profile.
     */
    @PUT("api/v1/merchants/{merchantId}")
    suspend fun updateMerchant(
        @Path("merchantId") merchantId: String,
        @Body merchant: Merchant
    ): Response<Merchant>
}
