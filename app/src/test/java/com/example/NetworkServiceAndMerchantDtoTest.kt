package com.example

import com.example.data.api.NetworkClient
import com.example.data.api.model.CreateMerchantRequest
import com.example.data.api.model.MerchantDto
import com.example.data.api.model.toDto
import com.example.data.api.model.toEntity
import com.example.data.model.AccountStatus
import com.example.data.model.MerchantProfile
import com.example.data.model.SubscriptionPlan
import org.junit.Assert.*
import org.junit.Test

class NetworkServiceAndMerchantDtoTest {

    @Test
    fun testMerchantDtoSerializationAndDeserialization() {
        val adapter = NetworkClient.moshi.adapter(MerchantDto::class.java)

        val originalDto = MerchantDto(
            id = "MERCH-TEST-101",
            businessName = "Shree Balaji Traders",
            businessAddress = "Shop 21, Market Yard, Pune, Maharashtra",
            contactNumber = "9822334455",
            email = "balaji@example.com",
            authorisedPersonName = "Vikram Patel",
            businessUpiId = "balaji@okaxis",
            planType = "PREMIUM",
            accountStatus = "ACTIVE",
            totalCreditPaise = 2500000L,
            totalRepaymentPaise = 1800000L,
            platformFeesAccruedPaise = 0L,
            autoPayEnabled = true
        )

        // Serialize to JSON
        val json = adapter.toJson(originalDto)
        assertTrue(json.contains("MERCH-TEST-101"))
        assertTrue(json.contains("Shree Balaji Traders"))
        assertTrue(json.contains("business_upi_id"))

        // Deserialize from JSON
        val parsedDto = adapter.fromJson(json)
        assertNotNull(parsedDto)
        assertEquals(originalDto.id, parsedDto?.id)
        assertEquals(originalDto.businessName, parsedDto?.businessName)
        assertEquals(originalDto.businessUpiId, parsedDto?.businessUpiId)
        assertEquals(originalDto.totalCreditPaise, parsedDto?.totalCreditPaise)
        assertEquals(true, parsedDto?.autoPayEnabled)
    }

    @Test
    fun testCreateMerchantRequestSerialization() {
        val adapter = NetworkClient.moshi.adapter(CreateMerchantRequest::class.java)

        val request = CreateMerchantRequest(
            businessName = "Kaveri Enterprises",
            businessAddress = "12 MG Road, Bengaluru, Karnataka",
            contactNumber = "9900112233",
            email = "kaveri@example.com",
            authorisedPersonName = "Suresh Reddy",
            businessUpiId = "kaveri@okhdfcbank"
        )

        val json = adapter.toJson(request)
        assertTrue(json.contains("Kaveri Enterprises"))
        assertTrue(json.contains("9900112233"))
        assertTrue(json.contains("business_address"))
    }

    @Test
    fun testMerchantMapperBetweenDtoAndEntity() {
        val dto = MerchantDto(
            id = "MERCH-9999",
            businessName = "Heritage Silks",
            businessAddress = "Kanchipuram, Tamil Nadu",
            contactNumber = "9444112233",
            email = "heritage@example.com",
            authorisedPersonName = "Meenakshi Sundaram",
            businessUpiId = "heritage@icici",
            planType = "PREMIUM",
            accountStatus = "ACTIVE"
        )

        val entity: MerchantProfile = dto.toEntity()
        assertEquals("MERCH-9999", entity.id)
        assertEquals(SubscriptionPlan.PREMIUM, entity.planType)
        assertEquals(AccountStatus.ACTIVE, entity.accountStatus)
        assertEquals("Heritage Silks", entity.businessName)

        val mappedBackDto = entity.toDto()
        assertEquals(dto.id, mappedBackDto.id)
        assertEquals(dto.planType, mappedBackDto.planType)
        assertEquals(dto.accountStatus, mappedBackDto.accountStatus)
    }

    @Test
    fun testRetrofitMerchantApiServiceNotNull() {
        val service = NetworkClient.merchantApiService
        assertNotNull(service)
    }

    @Test
    fun testMerchantDataClassSerialization() {
        val adapter = NetworkClient.moshi.adapter(com.example.data.Merchant::class.java)
        val merchant = com.example.data.Merchant(
            merchantId = "MERCH-7788",
            businessName = "Sunrise Grocery",
            businessAddress = "Shop 5, Sector 4, Rohini, Delhi",
            businessContact = "9876543210",
            businessEmail = "sunrise@example.com",
            authorizedPerson = "Ramesh Gupta",
            upiId = "sunrise@okaxis"
        )

        val json = adapter.toJson(merchant)
        assertTrue(json.contains("merchant_id"))
        assertTrue(json.contains("MERCH-7788"))
        assertTrue(json.contains("authorized_person"))

        val parsed = adapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals("MERCH-7788", parsed?.merchantId)
        assertEquals("Sunrise Grocery", parsed?.businessName)
        assertEquals("Ramesh Gupta", parsed?.authorizedPerson)
        assertEquals("sunrise@okaxis", parsed?.upiId)
    }

    @Test
    fun testNetworkPackageMerchantApiServiceCreation() {
        val retrofit = NetworkClient.createRetrofit()
        val networkMerchantService = retrofit.create(com.example.network.MerchantApiService::class.java)
        assertNotNull(networkMerchantService)
    }

    @Test
    fun testApiClientSingletonProvider() {
        val retrofitInstance = com.example.network.ApiClient.retrofit
        assertNotNull(retrofitInstance)

        val service = com.example.network.ApiClient.merchantApiService
        assertNotNull(service)

        val customService = com.example.network.ApiClient.createService(com.example.network.MerchantApiService::class.java)
        assertNotNull(customService)
    }
}
