package com.spliteasy.spliteasy.ui.member.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
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
    app: Application,
    private val repo: AccountRepository,
    private val tokenStore: TokenDataStore
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(SettingsUi(isLoading = true))
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, msg = null)

            val result = repo.me()
            val me = result.getOrNull()
            if (me == null) {
                val msg = result.exceptionOrNull()?.message
                    ?: getApplication<Application>().getString(R.string.settings_vm_profile_load_error)
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
            val result = repo.updateProfile(name, email)
            val ok = result.isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            val msg = if (ok) {
                getApplication<Application>().getString(R.string.settings_vm_profile_update_success)
            } else {
                result.exceptionOrNull()?.message
                    ?: getApplication<Application>().getString(R.string.settings_vm_profile_update_error)
            }
            onDone(ok, msg)
            if (ok) load()
        }
    }

    fun changePassword(current: String, new: String, confirm: String, onDone: (Boolean, String) -> Unit) {
        if (new.length < 8) {
            onDone(false, getApplication<Application>().getString(R.string.settings_vm_pass_min_length))
            return
        }
        if (new != confirm) {
            onDone(false, getApplication<Application>().getString(R.string.settings_security_pass_mismatch))
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            val result = repo.changePassword(current, new)
            val ok = result.isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            val msg = if (ok) {
                getApplication<Application>().getString(R.string.settings_vm_pass_change_success)
            } else {
                result.exceptionOrNull()?.message
                    ?: getApplication<Application>().getString(R.string.settings_vm_pass_change_error)
            }
            onDone(ok, msg)
        }
    }

    fun deleteAccount(onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            val result = repo.deleteAccount()
            val ok = result.isSuccess
            _ui.value = _ui.value.copy(isLoading = false)
            if (ok) {
                tokenStore.clear()
                onDeleted()
            } else {
                val msg = result.exceptionOrNull()?.message
                    ?: getApplication<Application>().getString(R.string.settings_vm_delete_error)
                onError(msg)
            }
        }
    }
}