package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.data.remote.api.ProfileDto

interface AccountRepository {
    suspend fun me(): Result<ProfileDto>
    suspend fun updateProfile(username: String, email: String): Result<Unit>
    suspend fun changePassword(current: String, new: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}
