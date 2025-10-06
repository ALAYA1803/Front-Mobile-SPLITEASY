package com.spliteasy.spliteasy.ui.representative.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.remote.api.BillsService
import com.spliteasy.spliteasy.data.remote.api.CreateBillRequest
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.dto.BillDto
import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BillUi(
    val id: Long,
    val householdId: Long?,
    val description: String?,
    val monto: Double?,
    val fecha: String?,
    val createdBy: Long?,
    val createdByName: String? = null
)

data class RepBillsUi(
    val loading: Boolean = true,
    val error: String? = null,
    val isRepresentante: Boolean = true,
    val householdId: Long? = null,
    val householdName: String = "",
    val currency: String = "PEN",
    val bills: List<BillUi> = emptyList(),
    // Dialog/Form
    val formVisible: Boolean = false,
    val editingId: Long? = null,
    val formDescription: String = "",
    val formMonto: String = "",
    val formFecha: String = ""
)

@HiltViewModel
class RepBillsViewModel @Inject constructor(
    private val repo: RepresentativeRepository,
    private val billsApi: BillsService,
    private val usersApi: UsersService        // <- AÑADIDO para resolver createdByName
) : ViewModel() {

    private val _ui = MutableStateFlow(RepBillsUi())
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                // 1) identificar hogar del representante actual
                val meId = repo.meId().getOrThrow()
                val households = repo.listAllHouseholds().getOrThrow()
                val myHouse = households.firstOrNull { it.representanteId == meId }

                if (myHouse == null) {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = "Como representante, aún no has creado un hogar.",
                        householdId = null
                    )
                    return@launch
                }

                val hhId = myHouse.id

                // 2) traer todas las bills y FILTRAR por householdId del hogar del representante
                val billsAll = billsApi.listAll()
                val billsMine = billsAll.filter { it.normalizedHouseholdId() == hhId }

                // 3) mapear a UI
                val billsUi = billsMine.map { it.toUi() }

                // 4) enriquecer con nombre del creador (opcional si backend lo trae)
                val users = runCatching { usersApi.list() }.getOrDefault(emptyList())
                val byId = users.associateBy { it.id }
                val finalBills = billsUi.map { b ->
                    val name = b.createdBy?.let { byId[it]?.username } ?: "—"
                    b.copy(createdByName = name)
                }

                _ui.value = _ui.value.copy(
                    loading = false,
                    householdId = hhId,
                    householdName = myHouse.name ?: "Mi Hogar",
                    currency = myHouse.currency ?: "PEN",
                    bills = finalBills
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = t.message ?: "Error al cargar facturas.")
            }
        }
    }

    fun openForm(edit: BillUi? = null) {
        if (edit == null) {
            _ui.value = _ui.value.copy(
                formVisible = true,
                editingId = null,
                formDescription = "",
                formMonto = "",
                formFecha = today()
            )
        } else {
            _ui.value = _ui.value.copy(
                formVisible = true,
                editingId = edit.id,
                formDescription = edit.description.orEmpty(),
                formMonto = (edit.monto ?: 0.0).toString(),
                formFecha = edit.fecha ?: today()
            )
        }
    }

    fun closeForm() {
        _ui.value = _ui.value.copy(formVisible = false, editingId = null)
    }

    fun onDescChange(v: String) { _ui.value = _ui.value.copy(formDescription = v) }
    fun onMontoChange(v: String) { _ui.value = _ui.value.copy(formMonto = v) }
    fun onFechaChange(v: String) { _ui.value = _ui.value.copy(formFecha = v) }

    fun submit() {
        val hhId = _ui.value.householdId ?: return
        val desc = _ui.value.formDescription.trim()
        val monto = _ui.value.formMonto.trim().toDoubleOrNull()
        val fecha = _ui.value.formFecha.trim()

        if (desc.isBlank() || monto == null || monto <= 0.0 || fecha.isBlank()) {
            _ui.value = _ui.value.copy(error = "Completa los datos correctamente.")
            return
        }

        viewModelScope.launch {
            try {
                val meId = repo.meId().getOrThrow()
                if (_ui.value.editingId == null) {
                    billsApi.create(
                        CreateBillRequest(
                            householdId = hhId,
                            description = desc,
                            monto = monto,
                            createdBy = meId,
                            fecha = fecha
                        )
                    )
                } else {
                    billsApi.update(
                        _ui.value.editingId!!,
                        CreateBillRequest(
                            householdId = hhId,
                            description = desc,
                            monto = monto,
                            createdBy = meId,
                            fecha = fecha
                        )
                    )
                }
                closeForm()
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: "No se pudo guardar la factura.")
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                billsApi.delete(id)
                load()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: "No se pudo eliminar.")
            }
        }
    }

    private fun today(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }
}

/* -------- Helpers -------- */
private fun BillDto.toUi(): BillUi = BillUi(
    id = this.id,
    householdId = this.normalizedHouseholdId(),
    description = this.description,
    monto = this.amount,   // en tu DTO lo llamaste "amount"
    fecha = this.date,     // en tu DTO lo llamaste "date"
    createdBy = try {
        val member = this::class.members.firstOrNull { it.name == "createdBy" }
        (member?.call(this) as? Long)
    } catch (_: Throwable) { null },
    createdByName = null
)

private fun BillDto.normalizedHouseholdId(): Long? = this.householdId ?: this.household_id
