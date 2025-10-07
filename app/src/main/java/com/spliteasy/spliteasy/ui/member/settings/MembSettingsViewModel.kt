package com.spliteasy.spliteasy.ui.member.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUi(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val canSubmitProfile: Boolean = false,
    val msg: String? = null
)

@HiltViewModel
class MembSettingsViewModel @Inject constructor(
    private val repo: AccountRepository,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUi(isLoading = true))
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, msg = null)

            val result = repo.me()
            val me = result.getOrNull()
            if (me == null) {
                val msg = result.exceptionOrNull()?.message ?: "No se pudo cargar el perfil."
                _ui.value = SettingsUi(isLoading = false, msg = msg)
                return@launch
            }

            _ui.value = SettingsUi(
                isLoading = false,
                name = me.username.orEmpty(),
                email = me.email.orEmpty(),
                canSubmitProfile = !me.username.isNullOrBlank() && !me.email.isNullOrBlank()
            )
        }
    }

    fun onNameChange(v: String) {
        _ui.value = _ui.value.copy(name = v, canSubmitProfile = v.isNotBlank() && _ui.value.email.isNotBlank())
    }

    fun onEmailChange(v: String) {
        _ui.value = _ui.value.copy(email = v, canSubmitProfile = _ui.value.name.isNotBlank() && v.isNotBlank())
    }

    fun saveProfile(onDone: (Boolean, String) -> Unit) {
        val (name, email) = _ui.value.let { it.name to it.email }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, msg = null)
            val ok = repo.updateProfile(name, email).isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            onDone(ok, if (ok) "Perfil actualizado correctamente." else "No se pudo actualizar el perfil.")
            if (ok) load()
        }
    }

    fun changePassword(current: String, new: String, onDone: (Boolean, String) -> Unit) {
        if (new.length < 8) {
            onDone(false, "La nueva contraseña debe tener al menos 8 caracteres.")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            val ok = repo.changePassword(current, new).isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            onDone(ok, if (ok) "Contraseña cambiada correctamente." else "No se pudo cambiar la contraseña.")
        }
    }

    fun deleteAccount(onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            val ok = repo.deleteAccount().isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            if (ok) {
                tokenStore.clear()
                onDeleted()
            } else onError("No se pudo eliminar la cuenta.")
        }
    }
}
