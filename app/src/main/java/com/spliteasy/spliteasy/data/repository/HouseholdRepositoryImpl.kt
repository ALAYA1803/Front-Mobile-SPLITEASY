package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.remote.api.CreateHouseholdRequest
import com.spliteasy.spliteasy.data.remote.api.HouseholdsService
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.domain.model.HouseholdMini
import com.spliteasy.spliteasy.domain.repository.HouseholdRepository
import retrofit2.HttpException
import javax.inject.Inject

class HouseholdRepositoryImpl @Inject constructor(
    private val api: HouseholdsService
) : HouseholdRepository {

    override suspend fun getHouseholdByRepresentative(representativeId: Long): Result<HouseholdMini?> =
        runCatching {
            val list = api.list()
            list.firstOrNull { it.representanteId == representativeId }?.toMini()
        }.mapError()

    override suspend fun createHousehold(
        name: String,
        description: String,
        currency: String,
        representativeId: Long
    ): Result<HouseholdMini> =
        runCatching {
            val dto = api.create(
                CreateHouseholdRequest(
                    name = name,
                    description = description,
                    currency = currency,
                    representanteId = representativeId
                )
            )
            dto.toMini()
        }.mapError()

    private fun HouseholdDto.toMini() = HouseholdMini(
        id = id,
        name = name ?: "",
        description = description ?: "",
        currency = currency ?: "PEN",
        representanteId = requireNotNull(representanteId) { "HouseholdDto.representanteId es null" }
    )

    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { t ->
        if (t is HttpException) throw IllegalStateException("HTTP ${t.code()}: ${t.message()}")
        else throw t
    }
}
