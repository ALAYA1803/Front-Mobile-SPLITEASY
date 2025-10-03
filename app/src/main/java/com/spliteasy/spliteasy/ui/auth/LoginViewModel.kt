package com.spliteasy.spliteasy.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /**
     * Retorna:
     * - true  -> si es OWNER (representante)
     * - false -> si es MEMBER (miembro)
     * - null  -> si hubo error (revisar `error`)
     */
    suspend fun login(username: String, password: String): Boolean? {
        loading = true
        error = null
        val res = repo.login(LoginRequest(username, password))
        loading = false
        return res.fold(
            onSuccess = { (_, isOwner) -> isOwner },
            onFailure = { e -> error = e.message ?: "Error desconocido"; null }
        )
    }
}
