package com.spliteasy.spliteasy.ui.member.contribs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class ContribRow(
    val mc: MemberContributionDto,
    val contribDescription: String?,
    val strategy: String?,
    val dueDate: String?,
    val billDescription: String?,
    val billDate: String?,
    val billAmount: Double?,
    val receipts: List<PaymentReceiptDto>,
    val statusUi: String // PENDIENTE | EN_REVISION | RECHAZADO | PAGADO
)

sealed interface ContribsUiState {
    data object Loading : ContribsUiState
    data class Ready(val rows: List<ContribRow>) : ContribsUiState
    data class Error(val message: String) : ContribsUiState
}

@HiltViewModel
class MembContribsViewModel @Inject constructor(
    private val repo: MemberRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<ContribsUiState>(ContribsUiState.Loading)
    val ui = _ui.asStateFlow()

    fun load(currentUserId: Long) {
        viewModelScope.launch {
            _ui.value = ContribsUiState.Loading

            val hh = repo.findMyHouseholdByScanning(currentUserId).getOrNull()
            if (hh == null) {
                _ui.value = ContribsUiState.Error("No perteneces a ningún hogar.")
                return@launch
            }

            val mcs = repo.listMyMemberContributions(currentUserId, hh.id).getOrElse {
                _ui.value = ContribsUiState.Error(it.message ?: "Error listando contribuciones")
                return@launch
            }

            val rows = mcs.map { mc ->
                async {
                    val contrib = repo.getContribution(mc.contributionId).getOrNull()
                    val bill = contrib?.billId?.let { repo.getBill(it).getOrNull() }
                    val receipts = repo.listReceipts(mc.id).getOrElse { emptyList() }
                    val hasPendingReceipt = receipts.any { it.status == "PENDING" }

                    val statusUi = when {
                        mc.status == "PAID" -> "PAGADO"
                        hasPendingReceipt -> "EN_REVISION"
                        mc.status == "RECHAZADO" -> "RECHAZADO"
                        else -> "PENDIENTE"
                    }

                    ContribRow(
                        mc = mc,
                        contribDescription = contrib?.description,
                        strategy = contrib?.strategy,
                        dueDate = contrib?.dueDate,
                        billDescription = bill?.description,
                        billDate = bill?.date,
                        billAmount = bill?.amount,
                        receipts = receipts,
                        statusUi = statusUi
                    )
                }
            }.map { it.await() }

            // igual que web: filtra pagadas si quieres
            _ui.value = ContribsUiState.Ready(rows.filter { it.statusUi != "PAGADO" })
        }
    }

    fun uploadReceipt(memberContributionId: Long, filePart: MultipartBody.Part, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repo.uploadReceipt(memberContributionId, filePart)
            onDone(res.isSuccess)
            // Puedes refrescar la fila puntual o recargar todo:
            // load(currentUserId)
        }
    }
}
