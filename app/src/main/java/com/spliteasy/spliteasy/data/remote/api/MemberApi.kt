package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto

interface MemberApi {
    suspend fun listHouseholds(): List<HouseholdDto>
    suspend fun getHousehold(id: Long): HouseholdDto
    suspend fun listHouseholdMembers(householdId: Long): List<Any>
    suspend fun listMemberContributions(householdId: Long, memberId: Long): List<MemberContributionDto>
}

class MemberApiImpl(
    private val groups: GroupsService,
    private val expenses: ExpensesService
) : MemberApi {
    override suspend fun listHouseholds() = groups.listHouseholds()
    override suspend fun getHousehold(id: Long) = groups.getHousehold(id)
    override suspend fun listHouseholdMembers(householdId: Long) = groups.listHouseholdMembers(householdId)
    override suspend fun listMemberContributions(householdId: Long, memberId: Long) =
        expenses.listMemberContributions(householdId, memberId)
}
