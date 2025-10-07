package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignInResponse
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    @POST("authentication/sign-in")
    suspend fun signIn(
        @Body req: LoginRequest,
        @Header("X-Integrity-Token") integrityToken: String? = null
    ): SignInResponse

    @POST("authentication/sign-up")
    suspend fun signUp(
        @Body req: SignUpRequest,
        @Header("X-Integrity-Token") integrityToken: String? = null
    )
}
