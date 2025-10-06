package com.spliteasy.spliteasy.data.repository

import android.util.Log
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.api.AuthService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthService,
    private val usersApi: UsersService,
    private val tokenStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(req: LoginRequest): Result<Pair<String, Boolean>> = runCatching {
        // 1) Sign-in
        val auth = authApi.signIn(req, integrityToken = null)
        val token = auth.token
        require(!token.isNullOrBlank()) { "El backend no devolvió 'token' en SignInResponse." }

        // 2) Guardar token
        tokenStore.saveToken(token)

        // 3) Leer usuario con Authorization
        val user = usersApi.getUserById(auth.id, bearer = "Bearer $token")

        // 👉 ADICIÓN: persistir userId para que el app pueda usarlo luego
        // (asumiendo que user.id es Long; si fuera Int, usa user.id.toLong())
        tokenStore.saveUserId(user.id)

        // --- ARREGLO previo: no usar primer rol; usar contains sobre la lista completa ---
        val rolesUpper = user.roles.map { it.trim().uppercase() }
        Log.d("AuthRepo", "roles=$rolesUpper (userId=${user.id}, username=${user.username})")

        // Acepta español e inglés
        val isRepresentative = rolesUpper.any {
            it == "ROLE_REPRESENTANTE" || it == "ROLE_REPRESENTATIVE"
        }

        // Guarda también el rol principal “normalizado” (útil para UI si lo lees)
        val mainRole = if (isRepresentative) "ROLE_REPRESENTANTE"
        else rolesUpper.firstOrNull() ?: ""
        tokenStore.saveRole(mainRole)

        Pair(token, isRepresentative)
    }.recoverCatching { t ->
        if (t is HttpException) {
            throw IllegalStateException("HTTP ${t.code()}: ${t.message()}")
        } else throw t
    }

    override suspend fun signup(req: SignUpRequest): Result<Unit> = runCatching {
        authApi.signUp(req, integrityToken = null)
        Unit
    }.recoverCatching { t ->
        if (t is HttpException) {
            throw IllegalStateException("HTTP ${t.code()}: ${t.message()}")
        } else throw t
    }
}
