package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.data.remote.dto.ForgotPasswordResponse

interface AuthRepository {
    suspend fun login(req: LoginRequest): Result<Pair<String, Boolean>>
    suspend fun signup(req: SignUpRequest): Result<Unit>

    suspend fun forgotPassword(email: String): ForgotPasswordResponse

    suspend fun resetPassword(token: String, newPassword: String): String
}
