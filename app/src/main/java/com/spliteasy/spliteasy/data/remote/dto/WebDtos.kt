package com.spliteasy.spliteasy.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class RawUserDto(
    val id: Long,
    val username: String? = null,
    val email: String? = null,
    val income: Double? = null,
    val roles: List<String>? = null
)

data class ContributionDto(
    val id: Long,
    val householdId: Long? = null,
    val billId: Long? = null,
    val description: String? = null,
    val strategy: String? = null,
    @Json(name = "fechaLimite")
    val dueDate: String? = null
)

data class BillDto(
    val id: Long,
    val description: String? = null,
    @Json(name = "monto")
    val amount: Double? = null,
    @Json(name = "fecha")
    val date: String? = null,
    val householdId: Long? = null,
    @Json(name = "household_id")
    val household_id: Long? = null
) {
    fun normalizedHouseholdId(): Long? = householdId ?: household_id
}

data class MemberContributionDto(
    val id: Long,
    val contributionId: Long,
    val memberId: Long,
    @Json(name = "monto")
    val amount: Double,
    val status: String? = null,
    val pagadoEn: String? = null
)

data class PaymentReceiptDto(
    val id: Long,
    val memberContributionId: Long,
    val filename: String,
    val url: String,
    val status: String,
    val uploadedAt: String,
    val reviewedById: Long? = null,
    val notes: String? = null
)
