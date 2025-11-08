package com.spliteasy.spliteasy.ui.representative.home.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.data.remote.api.HouseholdsService
import com.spliteasy.spliteasy.data.remote.api.CreateHouseholdRequest
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepCreateHouseholdViewModel @Inject constructor(
    app: Application,
    private val households: HouseholdsService,
    private val accountRepo: AccountRepository
) : AndroidViewModel(app)  {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    var menuExpanded = false
        private set
    fun toggleMenu(value: Boolean = !menuExpanded) { menuExpanded = value }

    fun create(name: String, description: String, currency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _error.value = null
            _loading.value = true

            val app = getApplication<Application>()

            try {
                val me = accountRepo.me().getOrNull()
                val repId = me?.id ?: 0L
                if (repId <= 0L) {
                    _error.value = app.getString(R.string.rep_create_vm_error_user_id)
                    _loading.value = false
                    return@launch
                }
                val req = CreateHouseholdRequest(
                    name = name,
                    description = description,
                    currency = currency,
                    representanteId = repId
                )

                val created = households.create(req)
                if (created.id != null && created.id > 0) {
                    onSuccess()
                } else {
                    _error.value = app.getString(R.string.rep_create_vm_error_generic_fail)
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: app.getString(R.string.rep_create_vm_error_unknown)
            } finally {
                _loading.value = false
            }
        }
    }
}