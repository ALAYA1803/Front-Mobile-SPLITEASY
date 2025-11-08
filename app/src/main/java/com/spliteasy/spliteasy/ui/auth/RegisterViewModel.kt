package com.spliteasy.spliteasy.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.recaptcha.RecaptchaAction
import com.spliteasy.spliteasy.core.RecaptchaHelper
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import com.spliteasy.spliteasy.R

@HiltViewModel
class RegisterViewModel @Inject constructor(
    app: Application,
    private val repo: AuthRepository
) : AndroidViewModel(app) {

    var loading: Boolean = false
        private set
    var error: String? = null
        private set
    fun register(
        username: String,
        email: String,
        password: String,
        income: Double,
        role: String,
        onDone: (Boolean) -> Unit
    ) {
        loading = true
        error = null
        val app = getApplication<Application>()

        viewModelScope.launch {
            val genericError = app.getString(R.string.register_vm_error_generic)

            try {
                val ctx = app.applicationContext
                val captchaToken = RecaptchaHelper.getToken(
                    context = ctx,
                    action = RecaptchaAction.SIGNUP
                )

                if (captchaToken.isNullOrBlank()) {
                    loading = false
                    error = app.getString(R.string.register_vm_error_recaptcha)
                    onDone(false)
                    return@launch
                }

                val req = SignUpRequest(
                    username = username,
                    email = email,
                    password = password,
                    income = income,
                    roles = listOf(role),
                    captchaToken = captchaToken
                )

                val res = repo.signup(req)
                loading = false

                if (res.isSuccess) {
                    onDone(true)
                } else {
                    error = res.exceptionOrNull()?.message ?: genericError
                    onDone(false)
                }
            } catch (e: Exception) {
                loading = false
                error = e.message ?: genericError
                onDone(false)
            }
        }
    }
}