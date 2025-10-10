package com.spliteasy.spliteasy.ui.representative.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.remote.api.BillsService
import com.spliteasy.spliteasy.data.remote.api.GroupsService
// ¡LA LÍNEA QUE FALTABA!
import com.spliteasy.spliteasy.data.remote.api.ExpensesService
import com.spliteasy.spliteasy.data.remote.dto.BillDto
import com.spliteasy.spliteasy.data.remote.dto.ContributionDto
import com.spliteasy.spliteasy.data.remote.dto.MemberDto
import com.spliteasy.spliteasy.domain.model.HouseholdMini
import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RepHomeUi(
    val loading: Boolean = true,
    val showOnboarding: Boolean = false,
    val household: HouseholdMini? = null,
    val membersCount: Int = 0,
    val billsCount: Int = 0,
    val contributionsCount: Int = 0,
    val currency: String = "PEN",
    val error: String? = null
)

@HiltViewModel
class RepHomeViewModel @Inject constructor(
    private val repo: RepresentativeRepository,
    private val groups: GroupsService,
    private val billsService: BillsService,
    private val expensesService: ExpensesService
) : ViewModel() {

    private val _ui = MutableStateFlow(RepHomeUi())
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = RepHomeUi(loading = true)
            val meId = repo.meId().getOrElse {
                _ui.value = RepHomeUi(loading = false, error = it.message ?: "No se pudo obtener el usuario.")
                return@launch
            }
            val households = repo.listAllHouseholds().getOrElse {
                _ui.value = RepHomeUi(loading = false, error = it.message ?: "No se pudo listar hogares.")
                return@launch
            }

            val mine = households.firstOrNull { it.representanteId == meId }
            if (mine == null) {
                _ui.value = RepHomeUi(loading = false, showOnboarding = true)
                return@launch
            }

            try {
                val hhId = mine.id
                val membersDef = async { groups.listAllHouseholdMembers() }
                val billsDef   = async { billsService.listAll() }
                val contribDef = async { expensesService.listHouseholdContributions(hhId) }

                val allMembers: List<MemberDto>   = membersDef.await()
                val allBills: List<BillDto>       = billsDef.await()
                val contributions: List<ContributionDto> = contribDef.await()

                val members    = allMembers.filter { it.normalizedHouseholdId() == hhId }
                val billsCount = allBills.count   { it.normalizedHouseholdId() == hhId }
                val currency = mine.currency ?: "PEN"

                _ui.value = RepHomeUi(
                    loading = false,
                    showOnboarding = false,
                    household = mine,
                    membersCount = members.size,
                    billsCount = billsCount,
                    contributionsCount = contributions.size,
                    currency = currency,
                    error = null
                )
            } catch (t: Throwable) {
                _ui.value = RepHomeUi(
                    loading = false,
                    showOnboarding = false,
                    household = mine,
                    error = t.message ?: "Error al cargar el panel."
                )
            }
        }
    }

    // Aquí la función 'normalizedHouseholdId' que estaba faltando en el archivo
    private fun MemberDto.normalizedHouseholdId(): Long? {
        return this.householdId ?: this.household_id ?: this.household?.id
    }

    private fun BillDto.normalizedHouseholdId(): Long? {
        return this.householdId ?: this.household_id
    }
}