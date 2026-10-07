package com.example.data.api.model

import com.example.data.model.AccountStatus
import com.example.data.model.MerchantProfile
import com.example.data.model.SubscriptionPlan

/**
 * Extension mapper to convert network MerchantDto to local Room MerchantProfile entity
 */
fun MerchantDto.toEntity(): MerchantProfile {
    return MerchantProfile(
        id = this.id,
        businessName = this.businessName,
        businessAddress = this.businessAddress,
        contactNumber = this.contactNumber,
        email = this.email,
        authorisedPersonName = this.authorisedPersonName,
        businessUpiId = this.businessUpiId,
        registrationDate = this.registrationDate,
        planType = if (this.planType.equals("PREMIUM", ignoreCase = true)) SubscriptionPlan.PREMIUM else SubscriptionPlan.FREE,
        accountStatus = when (this.accountStatus.uppercase()) {
            "SUSPENDED" -> AccountStatus.SUSPENDED
            "RESTRICTED" -> AccountStatus.RESTRICTED
            else -> AccountStatus.ACTIVE
        },
        suspensionReason = this.suspensionReason,
        nextBillingDate = this.nextBillingDate,
        customerCount = this.customerCount,
        totalCreditPaise = this.totalCreditPaise,
        totalRepaymentPaise = this.totalRepaymentPaise,
        platformFeesAccruedPaise = this.platformFeesAccruedPaise,
        platformFeesSettledPaise = this.platformFeesSettledPaise,
        pendingSettlementPaise = this.pendingSettlementPaise,
        autoPayEnabled = this.autoPayEnabled
    )
}

/**
 * Extension mapper to convert local Room MerchantProfile entity to network MerchantDto
 */
fun MerchantProfile.toDto(): MerchantDto {
    return MerchantDto(
        id = this.id,
        businessName = this.businessName,
        businessAddress = this.businessAddress,
        contactNumber = this.contactNumber,
        email = this.email,
        authorisedPersonName = this.authorisedPersonName,
        businessUpiId = this.businessUpiId,
        registrationDate = this.registrationDate,
        planType = this.planType.name,
        accountStatus = this.accountStatus.name,
        suspensionReason = this.suspensionReason,
        nextBillingDate = this.nextBillingDate,
        customerCount = this.customerCount,
        totalCreditPaise = this.totalCreditPaise,
        totalRepaymentPaise = this.totalRepaymentPaise,
        platformFeesAccruedPaise = this.platformFeesAccruedPaise,
        platformFeesSettledPaise = this.platformFeesSettledPaise,
        pendingSettlementPaise = this.pendingSettlementPaise,
        autoPayEnabled = this.autoPayEnabled
    )
}
