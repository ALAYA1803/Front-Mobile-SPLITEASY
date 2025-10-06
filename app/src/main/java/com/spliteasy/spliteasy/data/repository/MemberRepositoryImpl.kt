package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.api.WebMemberApi
import com.spliteasy.spliteasy.data.remote.dto.*
import com.spliteasy.spliteasy.domain.repository.MemberHomeSnapshot
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val api: WebMemberApi,
    private val tokenStore: TokenDataStore
) : MemberRepository {

    override suspend fun getActiveGroupId(): Long? = tokenStore.readActiveGroupId()
    override suspend fun setActiveGroupId(id: Long) = tokenStore.saveActiveGroupId(id)

    private fun normStatus(s: String?): String {
        val v = s?.uppercase()?.replace("-", "_")?.trim() ?: "PENDING"
        return when (v) {
            "PAGADO", "PAID" -> "PAID"
            "EN_REVISION", "REVIEW" -> "EN_REVISION"
            "RECHAZADO", "REJECTED" -> "RECHAZADO"
            else -> "PENDING"
        }
    }

    override suspend fun findMyHouseholdByScanning(currentUserId: Long) = runCatching {
        val households = api.listHouseholds()
        if (households.isEmpty()) return@runCatching null
        for (h in households) {
            val members = api.listHouseholdMembers(h.id)
            val mine = members.any { m ->
                when (m) {
                    is Map<*, *> -> {
                        val uid = (m["userId"] as? Number)?.toLong()
                            ?: (m["id"] as? Number)?.toLong()
                            ?: ((m["user"] as? Map<*, *>)?.get("id") as? Number)?.toLong()
                        uid == currentUserId
                    }
                    else -> false
                }
            }
            if (mine) return@runCatching h
        }
        null
    }

    override suspend fun fetchHousehold(id: Long) = runCatching { api.getHousehold(id) }
    override suspend fun fetchHouseholdMembers(id: Long) = runCatching { api.listHouseholdMembers(id) }
    override suspend fun getUser(id: Long) = runCatching { api.getUser(id) }

    override suspend fun listMyMemberContributions(memberId: Long, householdId: Long?) = runCatching {
        api.listMemberContributions(memberId = memberId, householdId = householdId).map {
            it.copy(status = normStatus(it.status))
        }
    }

    override suspend fun getContribution(id: Long) = runCatching { api.getContribution(id) }
    override suspend fun getBill(id: Long) = runCatching { api.getBill(id) }

    override suspend fun listReceipts(memberContributionId: Long) = runCatching {
        api.listReceipts(memberContributionId)
    }

    override suspend fun uploadReceipt(memberContributionId: Long, filePart: okhttp3.MultipartBody.Part) =
        runCatching { api.uploadReceipt(memberContributionId, filePart) }

    override suspend fun buildHomeSnapshot(currentUserId: Long) = runCatching {
        val hh = findMyHouseholdByScanning(currentUserId).getOrNull()
            ?: return@runCatching MemberHomeSnapshot(null, emptyList(), emptyList(), emptyList(), 0.0, 0.0)

        coroutineScope {
            val membersDefer = async { fetchHouseholdMembers(hh.id).getOrElse { emptyList() } }
            val myContribsDefer = async { listMyMemberContributions(currentUserId, hh.id).getOrElse { emptyList() } }

            val membersAny = membersDefer.await()
            val memberIds = membersAny.mapNotNull {
                when (it) {
                    is Map<*, *> -> (it["userId"] as? Number)?.toLong()
                        ?: (it["id"] as? Number)?.toLong()
                        ?: ((it["user"] as? Map<*, *>)?.get("id") as? Number)?.toLong()
                    else -> null
                }
            }.distinct()

            val users = memberIds.mapNotNull { id -> getUser(id).getOrNull() }

            val contribs = myContribsDefer.await()
            val pending = contribs.filter { it.status == "PENDING" }
            val paid    = contribs.filter { it.status == "PAID" }

            MemberHomeSnapshot(
                household = hh,
                members = users,
                contributionsPending = pending,
                contributionsPaid = paid,
                totalPending = pending.sumOf { it.amount },
                totalPaid = paid.sumOf { it.amount }
            )
        }
    }
}
