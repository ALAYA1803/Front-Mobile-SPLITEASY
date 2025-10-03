package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.api.AuthService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthService,
    private val usersApi: UsersService,
    private val tokenStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(req: LoginRequest): Result<Pair<String, Boolean>> = runCatching {
        val res = authApi.signin(req)
        tokenStore.saveToken(res.token)

        val user = usersApi.getUserById(res.id)
        val isOwner = user.roles.firstOrNull() == "ROLE_REPRESENTANTE"
        res.token to isOwner
    }

    override suspend fun signup(req: SignUpRequest): Result<Unit> = runCatching {
        authApi.signup(req)
        Unit
    }
}
