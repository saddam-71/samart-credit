package com.example.repository

import com.example.data.Merchant
import com.example.network.ApiClient
import com.example.network.MerchantApiService
import retrofit2.Response

/**
 * Repository abstracting network operations for Merchant profiles,
 * connecting the [MerchantApiService] to UI ViewModels.
 */
class MerchantRepository(
    private val apiService: MerchantApiService = ApiClient.merchantApiService
) {

    /**
     * Register a new merchant profile via API
     */
    suspend fun registerMerchant(merchant: Merchant): Result<Merchant> {
        return try {
            val response: Response<Merchant> = apiService.registerMerchant(merchant)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()} (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch merchant profile by ID
     */
    suspend fun getMerchant(merchantId: String): Result<Merchant> {
        return try {
            val response = apiService.getMerchant(merchantId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Merchant not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch merchant profile by contact number
     */
    suspend fun getMerchantByContact(contact: String): Result<Merchant> {
        return try {
            val response = apiService.getMerchantByContact(contact)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Merchant not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing merchant profile
     */
    suspend fun updateMerchant(merchantId: String, merchant: Merchant): Result<Merchant> {
        return try {
            val response = apiService.updateMerchant(merchantId, merchant)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update merchant: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
