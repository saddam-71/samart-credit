package com.example.data.api

import com.example.data.api.model.ApiResponse
import com.example.data.api.model.CreateMerchantRequest
import com.example.data.api.model.MerchantDto
import com.example.data.api.model.UpdateMerchantStatusRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit Service Interface for Merchant APIs
 */
interface MerchantApiService {

    /**
     * Fetch all registered merchants (for Super Admin dashboard)
     */
    @GET("api/v1/merchants")
    suspend fun getMerchants(): Response<ApiResponse<List<MerchantDto>>>

    /**
     * Fetch a specific merchant profile by ID
     */
    @GET("api/v1/merchants/{id}")
    suspend fun getMerchantById(
        @Path("id") merchantId: String
    ): Response<ApiResponse<MerchantDto>>

    /**
     * Fetch a merchant profile by mobile contact number
     */
    @GET("api/v1/merchants/by-phone")
    suspend fun getMerchantByPhone(
        @Query("phone") phone: String
    ): Response<ApiResponse<MerchantDto>>

    /**
     * Register a new merchant with the 6 required business fields
     */
    @POST("api/v1/merchants/register")
    suspend fun registerMerchant(
        @Body request: CreateMerchantRequest
    ): Response<ApiResponse<MerchantDto>>

    /**
     * Update merchant profile details
     */
    @PUT("api/v1/merchants/{id}")
    suspend fun updateMerchantProfile(
        @Path("id") merchantId: String,
        @Body merchantDto: MerchantDto
    ): Response<ApiResponse<MerchantDto>>

    /**
     * Update merchant account status (Suspend, Reactivate, Restrict)
     */
    @PATCH("api/v1/merchants/{id}/status")
    suspend fun updateMerchantStatus(
        @Path("id") merchantId: String,
        @Body request: UpdateMerchantStatusRequest
    ): Response<ApiResponse<MerchantDto>>
}
