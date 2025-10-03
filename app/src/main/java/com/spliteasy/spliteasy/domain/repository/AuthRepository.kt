package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest

interface AuthRepository {
    suspend fun login(req: LoginRequest): Result<Pair<String, Boolean>>
    suspend fun signup(req: SignUpRequest): Result<Unit>
}
