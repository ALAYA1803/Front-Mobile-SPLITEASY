package com.spliteasy.spliteasy.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

data class UpdateProfileRequest(val username: String, val email: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

interface AccountService {

    @GET("account/me")
    suspend fun me(): ProfileDto   // <-- Importante: ProfileDto

    @PUT("account/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ResponseBody

    @PUT("account/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ResponseBody

    @DELETE("account")
    suspend fun deleteAccount(): ResponseBody
}
