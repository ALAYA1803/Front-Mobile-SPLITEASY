package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.BillDto
import com.spliteasy.spliteasy.data.remote.dto.ContributionDto
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpensesService {
    @GET("contributions/{id}")
    suspend fun getContribution(@Path("id") id: Long): ContributionDto

    @GET("bills/{id}")
    suspend fun getBill(@Path("id") id: Long): BillDto

    @GET("member-contributions")
    suspend fun listMemberContributions(
        @Query("householdId") householdId: Long? = null,
        @Query("memberId") memberId: Long? = null
    ): List<MemberContributionDto>

    @GET("households/{id}/contributions")
    suspend fun listHouseholdContributions(@Path("id") householdId: Long): List<ContributionDto>
}
