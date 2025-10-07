package com.spliteasy.spliteasy.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(v: String) { email = v }

    private fun isValidEmail(s: String) =
        Patterns.EMAIL_ADDRESS.matcher(s).matches()

    fun submit(onToken: (String?) -> Unit) {
        if (!isValidEmail(email)) {
            error = "Ingresa un correo válido."
            return
        }
        viewModelScope.launch {
            loading = true; error = null; success = null
            try {
                val res = repo.forgotPassword(email.trim())
                success = res.message ?: "Si el email existe, enviaremos instrucciones."
                onToken(res.resetToken)
            } catch (e: Exception) {
                error = "Ocurrió un error. Intenta nuevamente."
            } finally {
                loading = false
            }
        }
    }
}
