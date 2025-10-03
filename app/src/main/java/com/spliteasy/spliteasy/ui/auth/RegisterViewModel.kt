package com.spliteasy.spliteasy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.remote.dto.SignUpRequest
import com.spliteasy.spliteasy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

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

        viewModelScope.launch {
            val res = repo.signup(
                SignUpRequest(
                    username = username,
                    email = email,
                    password = password,
                    income = income,
                    roles = listOf(role)
                )
            )
            loading = false


            res.fold(
                onSuccess = { _: Unit ->
                    onDone(true)
                },
                onFailure = { e: Throwable ->
                    error = e.message ?: "No se pudo registrar."
                    onDone(false)
                }
            )
        }
    }
}
