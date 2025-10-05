package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.domain.repository.models.*

interface MemberRepository {
    suspend fun getActiveGroupId(): Long?
    suspend fun setActiveGroupId(groupId: Long)
    suspend fun getHomeSnapshot(groupId: Long, forceRefresh: Boolean = false): Result<MemberHomeSnapshot>
}
