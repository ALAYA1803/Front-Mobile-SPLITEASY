package com.spliteasy.spliteasy.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    app: Application,
    private val repo: AuthRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

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
        val app = getApplication<Application>()

        if (!isValidPassword(pass1)) {
            error = app.getString(R.string.reset_pass_vm_error_length)
            return
        }
        if (pass1 != pass2) {
            error = app.getString(R.string.reset_pass_vm_error_mismatch)
            return
        }
        if (token.isBlank()) {
            error = app.getString(R.string.reset_pass_vm_error_token_missing)
            return
        }

        viewModelScope.launch {
            loading = true; error = null; success = null
            try {
                val msg = repo.resetPassword(token, pass1)
                success = msg.ifBlank { app.getString(R.string.reset_pass_vm_success) }
                onDone()
            } catch (e: Exception) {
                error = app.getString(R.string.reset_pass_vm_error_token_invalid)
            } finally {
                loading = false
            }
        }
    }
}