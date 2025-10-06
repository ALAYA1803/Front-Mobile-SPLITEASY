package com.spliteasy.spliteasy.data.remote.dto

import com.squareup.moshi.Json

// Hogar
data class HouseholdDto(
    val id: Long,
    val name: String? = null,
    val description: String? = null,
    val currency: String? = null,
    val representanteId: Long? = null
)

// Usuario "crudo"
data class RawUserDto(
    val id: Long,
    val username: String? = null,
    val email: String? = null,
    val income: Double? = null,
    val roles: List<String>? = null
)

// Contribution (web a veces usa "fechaLimite")
data class ContributionDto(
    val id: Long,
    val householdId: Long? = null,
    val billId: Long? = null,
    val description: String? = null,
    val strategy: String? = null,
    @Json(name = "fechaLimite")
    val dueDate: String? = null
)

// Bill (web a veces usa "monto"/"fecha")
data class BillDto(
    val id: Long,
    val description: String? = null,
    @Json(name = "monto")
    val amount: Double? = null,
    @Json(name = "fecha")
    val date: String? = null
)

// Member-contribution (web usa "monto", status mixto)
data class MemberContributionDto(
    val id: Long,
    val contributionId: Long,
    val memberId: Long,
    @Json(name = "monto")
    val amount: Double,
    val status: String? = null,
    val pagadoEn: String? = null
)

// *** NUEVO *** PaymentReceiptDto (para boletas)
data class PaymentReceiptDto(
    val id: Long,
    val memberContributionId: Long,
    val filename: String,
    val url: String,
    val status: String,           // PENDING | APPROVED | REJECTED
    val uploadedAt: String,
    val reviewedById: Long? = null,
    val notes: String? = null
)
