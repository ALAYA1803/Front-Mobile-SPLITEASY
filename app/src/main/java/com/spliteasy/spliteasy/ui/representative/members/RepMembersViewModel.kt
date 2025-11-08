package com.spliteasy.spliteasy.ui.representative.members

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
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
    app: Application,
    private val repo: RepresentativeRepository,
    private val membersApi: HouseholdMembersService
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(RepMembersUi())
    val ui = _ui.asStateFlow()

    private val app: Application = getApplication()

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val meId = repo.meId().getOrThrow()
                val myHousehold = repo.listAllHouseholds().getOrThrow()
                    .firstOrNull { it.representanteId == meId }

                if (myHousehold == null) {
                    _ui.value = RepMembersUi(loading = false, error = app.getString(R.string.rep_members_vm_error_no_household))
                    return@launch
                }
                val hhId = myHousehold.id
                val linksRaw = runCatching { membersApi.listByHousehold(hhId) }
                    .getOrElse {
                        runCatching { membersApi.list(householdId = hhId) }
                            .getOrElse {
                                membersApi.list().filter { it.normalizedHouseholdId() == hhId }
                            }
                    }
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
                _ui.value = RepMembersUi(loading = false, error = t.message ?: app.getString(R.string.rep_members_vm_error_load_fail))
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
                    _ui.value = _ui.value.copy(saving = false, error = app.getString(R.string.rep_members_vm_error_email_not_found))
                    return@launch
                }

                if (_ui.value.members.any { it.id == user.id }) {
                    _ui.value = _ui.value.copy(saving = false, error = app.getString(R.string.rep_members_vm_error_already_member))
                    return@launch
                }

                membersApi.create(CreateHouseholdMemberRequest(userId = user.id, householdId = hhId))
                openAddDialog(false)
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(saving = false, error = t.message ?: app.getString(R.string.rep_members_vm_error_add_fail))
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
                    _ui.value = _ui.value.copy(saving = false, error = app.getString(R.string.rep_members_vm_error_link_not_found))
                    return@launch
                }
                membersApi.delete(link.id)
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(saving = false, error = t.message ?: app.getString(R.string.rep_members_vm_error_delete_fail))
            } finally {
                _ui.value = _ui.value.copy(saving = false)
            }
        }
    }
}