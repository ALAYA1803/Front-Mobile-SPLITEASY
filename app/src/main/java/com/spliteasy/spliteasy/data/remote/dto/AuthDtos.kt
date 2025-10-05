package com.spliteasy.spliteasy.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignInRequest(
    val username: String,
    val password: String,
    val captchaToken: String
)
typealias LoginRequest = SignInRequest

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val income: Double?,
    val roles: List<String>
)

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
data class AuthResponse(
    val id: Long,
    val username: String,
    val token: String
)
