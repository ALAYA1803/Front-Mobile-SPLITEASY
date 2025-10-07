package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.ForgotPasswordRequest
import com.spliteasy.spliteasy.data.remote.dto.ForgotPasswordResponse
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.ResetPasswordRequest
import com.spliteasy.spliteasy.data.remote.dto.SignInResponse
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.data.remote.dto.SimpleMessageResponse
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

    @POST("authentication/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("authentication/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): SimpleMessageResponse
}
