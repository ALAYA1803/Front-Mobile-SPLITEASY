package com.spliteasy.spliteasy.ui.auth

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.recaptcha.RecaptchaAction
import com.spliteasy.spliteasy.R // 👈 AÑADIDO
import com.spliteasy.spliteasy.core.RecaptchaHelper
import com.spliteasy.spliteasy.data.remote.dto.LoginRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.net.ssl.SSLException

enum class LoginPhase { IDLE, CHECKING_RECAPTCHA, SIGNING_IN }

@HiltViewModel
class LoginViewModel @Inject constructor(
    app: Application,
    private val repo: AuthRepository
) : AndroidViewModel(app) {

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var phase by mutableStateOf(LoginPhase.IDLE)
        private set

    // ❗️ LÓGICA DE ERROR MOVIDA DESDE LA PANTALLA
    private fun mapAuthError(e: String?): String? {
        if (e.isNullOrBlank()) return null
        val msg = e.lowercase()
        val ctx = getApplication<Application>()
        return when {
            "unable to resolve host" in msg || "failed to connect" in msg || "network" in msg ->
                ctx.getString(R.string.login_vm_error_io)
            "timeout" in msg || "time-out" in msg ->
                ctx.getString(R.string.login_vm_error_io) // Re-usamos el de IO
            "401" in msg || "forbidden" in msg || "unauthorized" in msg ||
                    "contraseña incorrect" in msg || "usuario o contraseña" in msg || "invalid credential" in msg ->
                ctx.getString(R.string.login_vm_error_401)
            // ... (puedes añadir más mapeos si quieres)
            else -> e // Devuelve el error original si no coincide
        }
    }

    suspend fun login(username: String, password: String): Boolean? {
        loading = true
        error = null
        phase = LoginPhase.CHECKING_RECAPTCHA

        return try {
            val ctx = getApplication<Application>().applicationContext
            delay(500)
            val captchaToken = RecaptchaHelper.getToken(
                context = ctx,
                action = RecaptchaAction.LOGIN
            )
            Log.d("Login", "reCAPTCHA token=${captchaToken?.take(25)}...")

            if (captchaToken.isNullOrBlank()) {
                loading = false
                phase = LoginPhase.IDLE
                error = getApplication<Application>().getString(R.string.login_vm_recaptcha_fail) // ❗️ CAMBIADO
                return null
            }

            phase = LoginPhase.SIGNING_IN
            val result = repo.login(
                LoginRequest(username = username, password = password, captchaToken = captchaToken)
            )

            loading = false
            phase = LoginPhase.IDLE

            result.fold(
                onSuccess = { pair -> pair.second },
                onFailure = { t ->
                    // ❗️ CAMBIADO (ahora usamos mapAuthError)
                    error = mapAuthError(extractNiceError(t))
                    Log.e("Login", "Error login: ${error ?: t.message}", t)
                    null
                }
            )
        } catch (t: Throwable) {
            loading = false
            phase = LoginPhase.IDLE
            // ❗️ CAMBIADO (ahora usamos mapAuthError)
            error = mapAuthError(extractNiceError(t))
            Log.e("Login", "Error login (catch): ${error ?: t.message}", t)
            null
        }
    }

    fun clearError() { error = null }

    private fun extractNiceError(t: Throwable): String {
        val ctx = getApplication<Application>()
        // ❗️ CAMBIADO
        return when (t) {
            is HttpException -> {
                if (t.code() == 401) ctx.getString(R.string.login_vm_error_401)
                else parseHttpError(t)
            }
            is IOException -> ctx.getString(R.string.login_vm_error_io)
            is SSLException -> ctx.getString(R.string.login_vm_error_ssl)
            else -> t.message ?: ctx.getString(R.string.login_vm_error_unknown)
        }
    }

    private fun parseHttpError(e: HttpException): String {
        val code = e.code()
        val raw = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        val pretty = raw?.let { tryParseRecaptchaAware(it) }
        val base = if (!pretty.isNullOrBlank()) pretty else (raw ?: "Error HTTP $code")
        // ❗️ CAMBIADO
        return getApplication<Application>().getString(R.string.login_vm_error_http, code.toString(), base)
    }

    private fun tryParseRecaptchaAware(raw: String): String? {
        // ... (Esta función se queda igual, no tiene strings de UI)
        return try {
            val jo = JSONObject(raw)
            if (jo.has("message")) {
                val msg = jo.optString("message").trim()
                if (msg.isNotEmpty()) return msg
            }
            val hasCaptchaShape = jo.has("ok") || jo.has("source") || jo.has("reason") || jo.has("score")
            if (hasCaptchaShape) {
                val ok = if (jo.has("ok")) jo.optBoolean("ok") else null
                val source = jo.optString("source", null)
                val reason = jo.optString("reason", null)
                val score = if (jo.has("score")) jo.optDouble("score") else null
                val parts = buildList {
                    if (ok != null) add("ok=$ok")
                    if (!source.isNullOrBlank()) add("source=$source")
                    if (!reason.isNullOrBlank()) add("reason=$reason")
                    if (score != null) add("score=$score")
                }
                if (parts.isNotEmpty()) return parts.joinToString(", ")
            }
            if (jo.has("errors")) {
                val arr = jo.opt("errors")
                if (arr is JSONArray && arr.length() > 0) {
                    val items = (0 until arr.length()).mapNotNull { i -> arr.optString(i, null) }
                    if (items.isNotEmpty()) return "errors=${items.joinToString(", ")}"
                }
            }
            jo.toString()
        } catch (_: Exception) {
            null
        }
    }
}