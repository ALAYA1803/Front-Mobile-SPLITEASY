package com.spliteasy.spliteasy.data.repository

import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.data.remote.api.GroupsService
import com.spliteasy.spliteasy.data.remote.api.ExpensesService
import com.spliteasy.spliteasy.domain.repository.MemberRepository
import com.spliteasy.spliteasy.domain.repository.models.*
import javax.inject.Singleton
import javax.inject.Inject
@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val groupsApi: GroupsService,
    private val expensesApi: ExpensesService,
    private val tokenStore: TokenDataStore
) : MemberRepository {

    override suspend fun getActiveGroupId(): Long? = tokenStore.readActiveGroupId()

    override suspend fun setActiveGroupId(groupId: Long) {
        tokenStore.saveActiveGroupId(groupId)
    }

    override suspend fun getHomeSnapshot(groupId: Long, forceRefresh: Boolean): Result<MemberHomeSnapshot> = runCatching {
        val balanceDto = groupsApi.getMyBalance(groupId)
        val recentDto  = expensesApi.getRecentExpenses(groupId)

        MemberHomeSnapshot(
            balance = Balance(iOwe = balanceDto.iOwe, meOwe = balanceDto.meOwe),
            recent = recentDto.map { Expense(it.id, it.description, it.amount) }
        )
    }
}
