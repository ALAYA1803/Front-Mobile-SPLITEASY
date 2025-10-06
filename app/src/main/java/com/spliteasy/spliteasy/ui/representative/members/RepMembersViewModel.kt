package com.spliteasy.spliteasy.ui.representative.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.remote.api.HouseholdMembersService
import com.spliteasy.spliteasy.data.remote.api.CreateHouseholdMemberRequest
import com.spliteasy.spliteasy.data.remote.dto.HouseholdMemberDto
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto
import com.spliteasy.spliteasy.data.remote.dto.normalizedHouseholdId
import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepMembersUi(
    val loading: Boolean = true,
    val error: String? = null,
    val householdId: Long? = null,
    val members: List<RawUserDto> = emptyList(),
    val links: List<HouseholdMemberDto> = emptyList(),
    val showAddDialog: Boolean = false,
    val saving: Boolean = false
)

@HiltViewModel
class RepMembersViewModel @Inject constructor(
    private val repo: RepresentativeRepository,
    private val membersApi: HouseholdMembersService
) : ViewModel() {

    private val _ui = MutableStateFlow(RepMembersUi())
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val meId = repo.meId().getOrThrow()
                val myHousehold = repo.listAllHouseholds().getOrThrow()
                    .firstOrNull { it.representanteId == meId }

                if (myHousehold == null) {
                    _ui.value = RepMembersUi(loading = false, error = "No tienes un hogar creado aún.")
                    return@launch
                }
                val hhId = myHousehold.id

                // 1) PRIMERO: intentamos la ruta fuerte /households/{id}/members
                val linksRaw = runCatching { membersApi.listByHousehold(hhId) }
                    .getOrElse {
                        // 2) SI FALLA: usamos /household-members?householdId=...
                        runCatching { membersApi.list(householdId = hhId) }
                            .getOrElse {
                                // 3) ÚLTIMO RECURSO: /household-members y filtramos en cliente
                                membersApi.list().filter { it.normalizedHouseholdId() == hhId }
                            }
                    }

                // (por si el backend igual regresó todo, filtramos otra vez en cliente)
                val links = linksRaw.filter { it.normalizedHouseholdId() == hhId }

                val users: List<RawUserDto> =
                    if (links.isEmpty()) emptyList()
                    else links.mapNotNull { link ->
                        link.userId?.let { runCatching { membersApi.getUser(it) }.getOrNull() }
                    }

                _ui.value = RepMembersUi(
                    loading = false,
                    error = null,
                    householdId = hhId,
                    members = users,
                    links = links
                )
            } catch (t: Throwable) {
                _ui.value = RepMembersUi(loading = false, error = t.message ?: "Error al cargar miembros.")
            }
        }
    }

    fun openAddDialog(open: Boolean) {
        _ui.value = _ui.value.copy(showAddDialog = open)
    }

    fun addByEmail(email: String) {
        val hhId = _ui.value.householdId ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            try {
                val matches = membersApi.searchUsersByEmail(email)
                val user = matches.firstOrNull { it.email?.equals(email, ignoreCase = true) == true }
                if (user == null) {
                    _ui.value = _ui.value.copy(saving = false, error = "No se encontró un usuario con ese email.")
                    return@launch
                }

                // evitar duplicados
                if (_ui.value.members.any { it.id == user.id }) {
                    _ui.value = _ui.value.copy(saving = false, error = "El usuario ya es miembro.")
                    return@launch
                }

                membersApi.create(CreateHouseholdMemberRequest(userId = user.id, householdId = hhId))
                openAddDialog(false)
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(saving = false, error = t.message ?: "No se pudo añadir.")
            } finally {
                _ui.value = _ui.value.copy(saving = false)
            }
        }
    }

    fun deleteMember(userId: Long) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            try {
                val link = _ui.value.links.firstOrNull { it.userId == userId }
                if (link == null) {
                    _ui.value = _ui.value.copy(saving = false, error = "Relación no encontrada.")
                    return@launch
                }
                membersApi.delete(link.id)
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(saving = false, error = t.message ?: "No se pudo eliminar.")
            } finally {
                _ui.value = _ui.value.copy(saving = false)
            }
        }
    }
}
