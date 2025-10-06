package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.data.remote.dto.*
import kotlin.Result

data class MemberHomeSnapshot(
    val household: HouseholdDto?,
    val members: List<RawUserDto>,
    val contributionsPending: List<MemberContributionDto>,
    val contributionsPaid: List<MemberContributionDto>,
    val totalPending: Double,
    val totalPaid: Double
)

interface MemberRepository {
    suspend fun getActiveGroupId(): Long?
    suspend fun setActiveGroupId(id: Long)

    suspend fun findMyHouseholdByScanning(currentUserId: Long): Result<HouseholdDto?>
    suspend fun fetchHousehold(id: Long): Result<HouseholdDto>
    suspend fun fetchHouseholdMembers(id: Long): Result<List<Any>>
    suspend fun getUser(id: Long): Result<RawUserDto>

    suspend fun listMyMemberContributions(memberId: Long, householdId: Long? = null): Result<List<MemberContributionDto>>
    suspend fun getContribution(id: Long): Result<ContributionDto>
    suspend fun getBill(id: Long): Result<BillDto>

    suspend fun listReceipts(memberContributionId: Long): Result<List<PaymentReceiptDto>>
    suspend fun uploadReceipt(memberContributionId: Long, filePart: okhttp3.MultipartBody.Part): Result<PaymentReceiptDto>

    suspend fun buildHomeSnapshot(currentUserId: Long): Result<MemberHomeSnapshot>
}
