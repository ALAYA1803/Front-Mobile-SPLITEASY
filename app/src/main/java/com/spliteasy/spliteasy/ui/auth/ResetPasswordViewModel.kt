package com.spliteasy.spliteasy.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repo: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val token: String = savedStateHandle.get<String>("token") ?: ""

    var pass1 by mutableStateOf("")
        private set
    var pass2 by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf<String?>(null)
        private set

    fun onPass1Change(v: String) { pass1 = v }
    fun onPass2Change(v: String) { pass2 = v }

    private fun isValidPassword(p: String) = p.length in 8..100

    fun submit(onDone: () -> Unit) {
        if (!isValidPassword(pass1)) { error = "La contraseña debe tener entre 8 y 100 caracteres."; return }
        if (pass1 != pass2) { error = "Las contraseñas no coinciden."; return }
        if (token.isBlank()) { error = "Token inválido o faltante."; return }

        viewModelScope.launch {
            loading = true; error = null; success = null
            try {
                val msg = repo.resetPassword(token, pass1)
                success = msg.ifBlank { "Contraseña actualizada con éxito." }
                onDone()
            } catch (e: Exception) {
                error = "Token inválido o expirado."
            } finally {
                loading = false
            }
        }
    }
}
