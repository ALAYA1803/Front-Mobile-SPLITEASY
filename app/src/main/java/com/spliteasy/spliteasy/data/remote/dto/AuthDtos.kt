package com.spliteasy.spliteasy.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignInRequest(
    val username: String,
    val password: String,
    val captchaToken: String
)
typealias LoginRequest = SignInRequest
data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(
    val message: String? = null,
    val resetToken: String? = null
)
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
data class SimpleMessageResponse(val message: String? = null)
@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val income: Double,
    val roles: List<String>,
    val captchaToken: String
)

@JsonClass(generateAdapter = true)
data class SignInResponse(
    val id: Long,
    val username: String,
    val token: String
)
