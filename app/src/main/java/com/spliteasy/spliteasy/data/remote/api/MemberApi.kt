package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.data.remote.dto.MemberDto   // ← import nuevo

interface MemberApi {
    suspend fun listHouseholds(): List<HouseholdDto>
    suspend fun getHousehold(id: Long): HouseholdDto

    // ⚠️ Cambiamos Any → MemberDto
    suspend fun listHouseholdMembers(householdId: Long): List<MemberDto>

    suspend fun listMemberContributions(householdId: Long, memberId: Long): List<MemberContributionDto>
}

class MemberApiImpl(
    private val groups: GroupsService,
    private val expenses: ExpensesService
) : MemberApi {
    override suspend fun listHouseholds() = groups.listHouseholds()
    override suspend fun getHousehold(id: Long) = groups.getHousehold(id)

    // ⚠️ Antes llamaba a groups.listHouseholdMembers(hhId)
    //     Ahora el backend es /household-members (sin hhId), así que traemos todo y filtramos.
    override suspend fun listHouseholdMembers(householdId: Long): List<MemberDto> =
        groups
            .listAllHouseholdMembers()                  // ← NUEVO método en GroupsService
            .filter { it.normalizedHouseholdId() == householdId }

    override suspend fun listMemberContributions(householdId: Long, memberId: Long) =
        expenses.listMemberContributions(householdId, memberId)
}
