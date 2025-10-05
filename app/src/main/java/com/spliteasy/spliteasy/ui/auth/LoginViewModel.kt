package com.spliteasy.spliteasy.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.recaptcha.RecaptchaAction
import com.spliteasy.spliteasy.core.RecaptchaHelper
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    app: Application,
    private val repo: AuthRepository
) : AndroidViewModel(app) {

    var loading: Boolean = false
        private set
    var error: String? = null
        private set

    suspend fun login(username: String, password: String): Boolean? {
        loading = true
        error = null

        return try {
            val ctx = getApplication<Application>().applicationContext

            val captchaToken = RecaptchaHelper.getToken(
                context = ctx,
                action = RecaptchaAction("LOGIN")
            )

            val res = repo.login(
                LoginRequest(
                    username = username,
                    password = password,
                    captchaToken = captchaToken
                )
            )

            loading = false
            res.getOrNull()?.second
        } catch (e: Exception) {
            loading = false
            error = e.message ?: "No se pudo iniciar sesión."
            null
        }
    }
}
