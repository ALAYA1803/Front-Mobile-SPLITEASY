package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.AuthResponse
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("authentication/sign-in")
    suspend fun signin(@Body req: LoginRequest): AuthResponse

    @POST("authentication/sign-up")
    suspend fun signup(@Body req: SignUpRequest): AuthResponse
}
