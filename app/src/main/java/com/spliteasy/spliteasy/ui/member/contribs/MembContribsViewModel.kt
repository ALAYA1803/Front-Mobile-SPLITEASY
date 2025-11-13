package com.spliteasy.spliteasy.ui.member.contribs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val statusUi: String,
    val qr: String?,
    val numero: String?
)

sealed interface ContribsUiState {
    data object Loading : ContribsUiState
    data class Ready(
        val rows: List<ContribRow>,
        val dialogForContrib: ContribRow? = null
    ) : ContribsUiState
    data class Error(val message: String) : ContribsUiState
}

@HiltViewModel
class MembContribsViewModel @Inject constructor(
    app: Application,
    private val repo: MemberRepository
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow<ContribsUiState>(ContribsUiState.Loading)
    val ui = _ui.asStateFlow()

    fun load(currentUserId: Long) {
        viewModelScope.launch {
            _ui.value = ContribsUiState.Loading

            val app = getApplication<Application>()

            val hh = repo.findMyHouseholdByScanning(currentUserId).getOrNull()
            if (hh == null) {
                _ui.value = ContribsUiState.Error(app.getString(R.string.memb_contribs_vm_error_no_household))
                return@launch
            }

            val mcs = repo.listMyMemberContributions(currentUserId, hh.id).getOrElse {
                _ui.value = ContribsUiState.Error(it.message ?: app.getString(R.string.memb_contribs_vm_error_list_contribs))
                return@launch
            }

            val rows = coroutineScope {
                mcs.map { mc ->
                    async {
                        val contrib = repo.getContribution(mc.contributionId).getOrNull()
                        val bill = contrib?.billId?.let { repo.getBill(it).getOrNull() }
                        val receipts = repo.listReceipts(mc.id).getOrElse { emptyList() }
                        val hasPendingReceipt = receipts.any { it.status.equals("PENDING", true) }
                        val statusUi = when {
                            mc.status.equals("PAID", true) || mc.status.equals("PAGADO", true) -> "PAGADO"
                            hasPendingReceipt -> "EN_REVISION"
                            mc.status.equals("RECHAZADO", true) || mc.status.equals("REJECTED", true) -> "RECHAZADO"
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
                            statusUi = statusUi,
                            qr = contrib?.qr,
                            numero = contrib?.numero
                        )
                    }
                }.awaitAll()
            }
            _ui.value = ContribsUiState.Ready(rows.filter { it.statusUi != "PAGADO" })
        }
    }

    fun uploadReceipt(memberContributionId: Long, filePart: MultipartBody.Part, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repo.uploadReceipt(memberContributionId, filePart)
            onDone(res.isSuccess)
        }
    }
    fun openPaymentDialog(row: ContribRow) {
        _ui.update {
            if (it is ContribsUiState.Ready) {
                it.copy(dialogForContrib = row)
            } else {
                it
            }
        }
    }
    fun closePaymentDialog() {
        _ui.update {
            if (it is ContribsUiState.Ready) {
                it.copy(dialogForContrib = null)
            } else {
                it
            }
        }
    }
}