package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.remote.api.AccountService
import com.spliteasy.spliteasy.data.remote.api.HouseholdsService
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.domain.model.HouseholdMini
import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import retrofit2.HttpException
import javax.inject.Inject

class RepresentativeRepositoryImpl @Inject constructor(
    private val account: AccountService,
    private val households: HouseholdsService
) : RepresentativeRepository {

    override suspend fun meId(): Result<Long> = runCatching {
        val me = account.me()
        me.id ?: error("ID de usuario no disponible")
    }.mapError()

    override suspend fun listAllHouseholds(): Result<List<HouseholdMini>> = runCatching {
        households.list().map { it.toMini() }
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
