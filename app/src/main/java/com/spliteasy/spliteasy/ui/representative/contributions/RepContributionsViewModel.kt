package com.spliteasy.spliteasy.ui.representative.contributions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.data.remote.api.BillsService
import com.spliteasy.spliteasy.data.remote.api.ContributionsService
import com.spliteasy.spliteasy.data.remote.api.CreateContributionRequest
import com.spliteasy.spliteasy.data.remote.api.HouseholdMembersService
import com.spliteasy.spliteasy.data.remote.api.MemberContributionsService
import com.spliteasy.spliteasy.data.remote.api.PaymentReceiptsService
import com.spliteasy.spliteasy.data.remote.api.UsersService
import com.spliteasy.spliteasy.data.remote.dto.BillDto
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.domain.repository.RepresentativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ContributionDetailUi(
    val id: Long,
    val memberId: Long?,
    val userId: Long?,
    val displayName: String,
    val displayRole: String,
    val monto: Double,
    val status: String,
    val pagadoEn: String?,
    val pendingReceiptsCount: Int
)

data class ContributionUi(
    val id: Long,
    val billId: Long?,
    val billDescription: String?,
    val description: String?,
    val strategy: String?,
    val fechaLimite: String?,
    val montoTotal: Double,
    val details: List<ContributionDetailUi>,
    val expanded: Boolean = false
)

data class MemberLite(
    val memberId: Long,
    val userId: Long,
    val username: String,
    val isRepresentative: Boolean
)

data class RepContribUi(
    val loading: Boolean = true,
    val error: String? = null,
    val isRepresentante: Boolean = true,
    val householdId: Long? = null,
    val householdName: String = "",
    val currency: String = "PEN",
    val contributions: List<ContributionUi> = emptyList(),
    val formVisible: Boolean = false,
    val editingId: Long? = null,
    val formBillId: Long? = null,
    val formDescription: String = "",
    val formFechaLimite: String = "",
    val formStrategy: String = "EQUAL",
    val formSelectedMembers: Set<Long> = emptySet(),
    val allBills: List<BillDto> = emptyList(),
    val allMembers: List<MemberLite> = emptyList(),
    val reviewVisible: Boolean = false,
    val reviewLoading: Boolean = false,
    val reviewForDetail: ContributionDetailUi? = null,
    val reviewReceipts: List<PaymentReceiptDto> = emptyList()
)

@HiltViewModel
class RepContributionsViewModel @Inject constructor(
    private val app: Application,
    private val repo: RepresentativeRepository,
    private val billsApi: BillsService,
    private val usersApi: UsersService,
    private val contribApi: ContributionsService,
    private val mcApi: MemberContributionsService,
    private val receiptsApi: PaymentReceiptsService,
    private val hhMembersApi: HouseholdMembersService
) : ViewModel() {

    private val _ui = MutableStateFlow(RepContribUi())
    val ui = _ui.asStateFlow()

    fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val meId = repo.meId().getOrThrow()
            val households = repo.listAllHouseholds().getOrThrow()
            val myHouse = households.firstOrNull { it.representanteId == meId }
                ?: run {
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = app.getString(R.string.rep_contrib_vm_error_no_household)
                    )
                    return@launch
                }

            val hhId = myHouse.id
            val allBills = runCatching { billsApi.listAll() }.getOrDefault(emptyList())
            val allUsers = runCatching { usersApi.list() }.getOrDefault(emptyList())
            val allContribs = runCatching { contribApi.listAll() }.getOrDefault(emptyList())
            val allMcs = runCatching { mcApi.listAll() }.getOrDefault(emptyList())

            val billsById: Map<Long, BillDto> = allBills.associateBy { it.id }
            val usersById = allUsers.associateBy { it.id }

            val memberLinks = runCatching { hhMembersApi.listByHousehold(hhId) }
                .getOrElse {
                    runCatching { hhMembersApi.list() }.getOrDefault(emptyList())
                        .filter { it.normalizedHouseholdId() == hhId }
                }

            val membersLite: List<MemberLite> = memberLinks.map { link ->
                val uId = link.userId ?: -1L
                val uname = usersById[uId]?.username ?: "Usuario $uId"
                MemberLite(
                    memberId = link.id,
                    userId = uId,
                    username = uname,
                    isRepresentative = (uId == myHouse.representanteId)
                )
            }

            val memberByMemberId = membersLite.associateBy { it.memberId }
            val memberByUserId = membersLite.associateBy { it.userId }

            val mine = allContribs.filter { it.householdId == hhId }

            val mcsByContrib: Map<Long, List<MemberContributionDto>> =
                allMcs.groupBy { it.contributionId }

            val contribUi: List<ContributionUi> = mine.map { c ->
                val cid = c.id
                val mcs = mcsByContrib[cid] ?: emptyList()

                val details: List<ContributionDetailUi> = mcs.map { mc ->
                    val rawId = mc.memberId
                    val member = rawId?.let {
                        memberByMemberId[it] ?: memberByUserId[it]
                    }

                    val displayName = member?.username ?: "Miembro #${mc.memberId}"
                    val role = if (member?.isRepresentative == true) "REPRESENTANTE" else "MIEMBRO"

                    ContributionDetailUi(
                        id = mc.id,
                        memberId = mc.memberId,
                        userId = member?.userId,
                        displayName = displayName,
                        displayRole = role,
                        monto = mc.amount,
                        status = normalizeStatus(mc.status),
                        pagadoEn = mc.pagadoEn,
                        pendingReceiptsCount = 0
                    )
                }

                val total = details.sumOf { it.monto }
                val bId = c.billId
                val billDesc = bId?.let { billsById[it]?.description }

                ContributionUi(
                    id = cid,
                    billId = bId,
                    billDescription = billDesc,
                    description = c.description,
                    strategy = c.strategy,
                    fechaLimite = c.dueDate,
                    montoTotal = total,
                    details = details
                )
            }.sortedBy { it.fechaLimite ?: "" }

            val withCounts = contribUi.map { cu ->
                val updatedDetails = cu.details.map { d ->
                    val receipts = runCatching { receiptsApi.list(d.id) }.getOrDefault(emptyList())
                    val pending = receipts.count { it.status.equals("PENDING", ignoreCase = true) }
                    d.copy(pendingReceiptsCount = pending)
                }
                cu.copy(details = updatedDetails)
            }

            _ui.value = _ui.value.copy(
                loading = false,
                householdId = hhId,
                householdName = myHouse.name ?: app.getString(R.string.rep_contrib_vm_default_household),
                currency = myHouse.currency ?: "PEN",
                contributions = withCounts,
                allBills = allBills.filter { it.normalizedHouseholdId() == hhId },
                allMembers = membersLite
            )
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(
                loading = false,
                error = t.message ?: app.getString(R.string.rep_contrib_vm_error_load_fail)
            )
        }
    }

    fun toggleExpanded(id: Long) {
        _ui.value = _ui.value.copy(
            contributions = _ui.value.contributions.map {
                if (it.id == id) it.copy(expanded = !it.expanded) else it
            }
        )
    }

    fun openForm() {
        _ui.value = _ui.value.copy(
            formVisible = true,
            editingId = null,
            formBillId = null,
            formDescription = "",
            formFechaLimite = today(),
            formStrategy = "EQUAL",
            formSelectedMembers = emptySet()
        )
    }

    fun closeForm() {
        _ui.value = _ui.value.copy(
            formVisible = false,
            editingId = null
        )
    }

    fun onBill(v: Long?) {
        _ui.value = _ui.value.copy(formBillId = v)
    }

    fun onDesc(v: String) {
        _ui.value = _ui.value.copy(formDescription = v)
    }

    fun onDate(v: String) {
        _ui.value = _ui.value.copy(formFechaLimite = v)
    }

    fun onStrategy(v: String) {
        _ui.value = _ui.value.copy(formStrategy = v)
    }

    fun toggleMember(memberId: Long) {
        val set = _ui.value.formSelectedMembers.toMutableSet()
        if (!set.add(memberId)) set.remove(memberId)
        _ui.value = _ui.value.copy(formSelectedMembers = set)
    }

    fun submit() = viewModelScope.launch {
        val hhId = _ui.value.householdId ?: return@launch
        val bill = _ui.value.formBillId
        val desc = _ui.value.formDescription.trim()
        val date = _ui.value.formFechaLimite.trim()
        val strategy = _ui.value.formStrategy
        val members = _ui.value.formSelectedMembers.toList()

        if (bill == null || desc.isBlank() || date.isBlank() || members.isEmpty()) {
            _ui.value = _ui.value.copy(
                error = app.getString(R.string.rep_contrib_vm_error_form_invalid)
            )
            return@launch
        }

        try {
            contribApi.create(
                CreateContributionRequest(
                    billId = bill,
                    householdId = hhId,
                    description = desc,
                    strategy = strategy,
                    fechaLimite = date,
                    memberIds = members
                )
            )
            closeForm()
            load()
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(
                error = t.message ?: app.getString(R.string.rep_contrib_vm_error_create_fail)
            )
        }
    }

    fun openReview(detail: ContributionDetailUi) {
        _ui.value = _ui.value.copy(
            reviewVisible = true,
            reviewLoading = true,
            reviewForDetail = detail,
            reviewReceipts = emptyList()
        )
        viewModelScope.launch {
            val list = runCatching { receiptsApi.list(detail.id) }.getOrDefault(emptyList())
            _ui.value = _ui.value.copy(
                reviewLoading = false,
                reviewReceipts = list
            )
        }
    }

    fun closeReview() {
        _ui.value = _ui.value.copy(
            reviewVisible = false,
            reviewLoading = false,
            reviewForDetail = null,
            reviewReceipts = emptyList()
        )
    }

    fun approveReceiptAndRefresh(receiptId: Long) = viewModelScope.launch {
        runCatching { receiptsApi.approve(receiptId) }.onSuccess {
            val cur = _ui.value
            val detail = cur.reviewForDetail
            if (detail != null) {
                val updatedDetail = detail.copy(
                    status = "PAGADO",
                    pendingReceiptsCount = (detail.pendingReceiptsCount - 1).coerceAtLeast(0)
                )
                _ui.value = cur.copy(reviewForDetail = updatedDetail)
            }
            detail?.let { openReview(it) }
            load()
        }
    }

    fun rejectReceiptAndRefresh(receiptId: Long, notes: String?) = viewModelScope.launch {
        runCatching { receiptsApi.reject(receiptId, notes) }.onSuccess {
            val cur = _ui.value
            val detail = cur.reviewForDetail
            if (detail != null && detail.status == "EN_REVISION") {
                val updatedDetail = detail.copy(
                    pendingReceiptsCount = (detail.pendingReceiptsCount - 1).coerceAtLeast(0)
                )
                _ui.value = cur.copy(reviewForDetail = updatedDetail)
            }
            detail?.let { openReview(it) }
            load()
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        try {
            contribApi.delete(id)
            load()
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(
                error = t.message ?: app.getString(R.string.rep_contrib_vm_error_delete_fail)
            )
        }
    }

    private fun today(): String {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    private fun normalizeStatus(s: String?): String = when ((s ?: "").uppercase()) {
        "PAID", "PAGADO" -> "PAGADO"
        "PENDING_REVIEW", "EN_REVISION", "EN-REVISION", "REVIEW" -> "EN_REVISION"
        "REJECTED", "RECHAZADO" -> "RECHAZADO"
        else -> "PENDIENTE"
    }
}

private fun Any.normalizedHouseholdId(): Long? = try {
    val k = this::class.members.firstOrNull { it.name in setOf("householdId", "household_id") }
    (k?.call(this) as? Long)
} catch (_: Throwable) {
    null
}
