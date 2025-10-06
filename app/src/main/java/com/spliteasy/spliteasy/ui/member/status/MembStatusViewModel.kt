package com.spliteasy.spliteasy.ui.member.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

data class StatusRowUi(
    val descripcionFactura: String?,
    val montoFactura: Double?,
    val fechaFactura: String?,
    val descripcionContrib: String?,
    val strategy: String?,
    val fechaLimite: String?,
    val monto: Double,
    val statusUi: String,          // PENDIENTE | EN_REVISION | PAGADO
    val pagadoEn: String?          // fecha de pago si aplica
)

sealed interface StatusUiState {
    data object Loading : StatusUiState
    data class Ready(val rows: List<StatusRowUi>) : StatusUiState
    data class Empty(val reason: String) : StatusUiState
    data class Error(val message: String) : StatusUiState
}

@HiltViewModel
class MembStatusViewModel @Inject constructor(
    private val repo: MemberRepository,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    private val _ui = MutableStateFlow<StatusUiState>(StatusUiState.Loading)
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _ui.value = StatusUiState.Loading

            val userId = tokenStore.readUserId()
            if (userId == null) {
                _ui.value = StatusUiState.Empty("Usuario no logueado.")
                return@launch
            }

            val hh = repo.findMyHouseholdByScanning(userId).getOrNull()
            if (hh == null) {
                _ui.value = StatusUiState.Empty("No perteneces a ningún hogar.")
                return@launch
            }

            val mcs = repo.listMyMemberContributions(userId, hh.id).getOrElse {
                _ui.value = StatusUiState.Error(it.message ?: "Error listando contribuciones")
                return@launch
            }

            if (mcs.isEmpty()) {
                _ui.value = StatusUiState.Ready(emptyList())
                return@launch
            }

            val rows = mcs.map { mc ->
                async {
                    val contrib = repo.getContribution(mc.contributionId).getOrNull()
                    val bill = contrib?.billId?.let { repo.getBill(it).getOrNull() }

                    val receipts: List<PaymentReceiptDto> =
                        repo.listReceipts(mc.id).getOrElse { emptyList() }
                    val hasPendingReceipt = receipts.any { it.status.equals("PENDING", ignoreCase = true) }

                    val mcStatus = anyString(mc, "status")?.uppercase()
                    val statusUi = when {
                        mcStatus == "PAGADO" || mcStatus == "PAID" -> "PAGADO"
                        hasPendingReceipt -> "EN_REVISION"
                        else -> "PENDIENTE"
                    }

                    StatusRowUi(
                        descripcionFactura = anyString(bill, "description", "descripcion"),
                        montoFactura = anyDouble(bill, "amount", "monto"),
                        fechaFactura = anyString(bill, "date", "fecha"),
                        descripcionContrib = anyString(contrib, "description", "descripcion"),
                        strategy = anyString(contrib, "strategy"),
                        fechaLimite = anyString(contrib, "dueDate", "fechaLimite"),
                        monto = round2(getAmount(mc)),
                        statusUi = statusUi,
                        pagadoEn = anyString(mc, "paidAt", "pagadoEn")
                    )
                }
            }.map { it.await() }

            // Igual que tu web: solo mostramos PAGADOS
            _ui.value = StatusUiState.Ready(rows.filter { it.statusUi == "PAGADO" })
        }
    }

    private fun round2(x: Double): Double = round(x * 100.0) / 100.0

    /** Lee el monto de MemberContribution sin romper si el campo se llama 'monto' o 'amount' */
    private fun getAmount(mc: MemberContributionDto): Double {
        anyDouble(mc, "monto", "amount")?.let { return it }
        return 0.0
    }

    /* -------------------- helpers genéricos (Map o reflexión) -------------------- */

    private fun anyString(obj: Any?, vararg keys: String): String? {
        if (obj == null) return null
        // Map
        if (obj is Map<*, *>) {
            for (k in keys) {
                val v = obj[k]
                if (v is String && v.isNotBlank()) return v
            }
        }
        // Reflexión
        for (k in keys) {
            try {
                val f = obj.javaClass.getDeclaredField(k)
                f.isAccessible = true
                val v = f.get(obj)
                if (v is String && v.isNotBlank()) return v
            } catch (_: Throwable) {}
        }
        return null
    }

    private fun anyDouble(obj: Any?, vararg keys: String): Double? {
        if (obj == null) return null
        if (obj is Map<*, *>) {
            for (k in keys) {
                val v = obj[k]
                if (v is Number) return v.toDouble()
                if (v is String) v.toDoubleOrNull()?.let { return it }
            }
        }
        for (k in keys) {
            try {
                val f = obj.javaClass.getDeclaredField(k)
                f.isAccessible = true
                val v = f.get(obj)
                if (v is Number) return v.toDouble()
                if (v is String) v.toDoubleOrNull()?.let { return it }
            } catch (_: Throwable) {}
        }
        return null
    }
}
