package com.spliteasy.spliteasy.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    app: Application,
    private val repo: AuthRepository
) : AndroidViewModel(app) {

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
            error = getApplication<Application>().getString(R.string.forgot_password_error_invalid_email)
            return
        }
        viewModelScope.launch {
            loading = true; error = null; success = null
            try {
                val res = repo.forgotPassword(email.trim())
                val defaultSuccessMsg = getApplication<Application>().getString(R.string.forgot_password_success_default)
                success = res.message ?: defaultSuccessMsg
                onToken(res.resetToken)
            } catch (e: Exception) {
                error = getApplication<Application>().getString(R.string.forgot_password_error_api)
            } finally {
                loading = false
            }
        }
    }
}