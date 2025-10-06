package com.spliteasy.spliteasy.ui.representative.home.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val households: HouseholdsService,
    private val accountRepo: AccountRepository                     // <-- NUEVO
) : ViewModel()  {

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
            try {
                // 1) obtener el id del usuario actual
                val me = accountRepo.me().getOrNull()
                val repId = me?.id ?: 0L
                if (repId <= 0L) {
                    _error.value = "No se pudo obtener el ID del representante. Vuelve a iniciar sesión."
                    _loading.value = false
                    return@launch
                }

                // 2) construir el request CON representanteId
                val req = CreateHouseholdRequest(
                    name = name,
                    description = description,
                    currency = currency,
                    representanteId = repId                    // <-- CLAVE
                )

                // 3) crear en backend
                val created = households.create(req)
                if (created.id != null && created.id > 0) {
                    onSuccess()
                } else {
                    _error.value = "No se pudo crear el hogar."
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: "Error creando el hogar."
            } finally {
                _loading.value = false
            }
        }
    }
}
