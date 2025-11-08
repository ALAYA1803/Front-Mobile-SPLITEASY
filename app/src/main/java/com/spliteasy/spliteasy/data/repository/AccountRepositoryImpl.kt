package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.remote.api.*
import com.spliteasy.spliteasy.domain.repository.AccountRepository
import retrofit2.HttpException
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val api: AccountService
) : AccountRepository {

    override suspend fun me() = runCatching { api.me() }

    override suspend fun updateProfile(username: String, email: String) = runCatching {
        api.updateProfile(UpdateProfileRequest(username, email)); Unit
    }.mapError()

    override suspend fun changePassword(current: String, new: String) = runCatching {
        api.changePassword(ChangePasswordRequest(current, new)); Unit
    }.mapError()

    override suspend fun deleteAccount() = runCatching {
        api.deleteAccount(); Unit
    }.mapError()

    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { t ->
        if (t is HttpException) throw IllegalStateException("HTTP ${t.code()}: ${t.message()}")
        else throw t
    }
}
