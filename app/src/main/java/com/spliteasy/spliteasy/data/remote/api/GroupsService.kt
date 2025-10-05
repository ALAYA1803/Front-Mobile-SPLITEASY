package com.spliteasy.spliteasy.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path

data class BalanceDto(
    val iOwe: Double,
    val meOwe: Double
)

interface GroupsService {
    @GET("groups/{id}/balance")
    suspend fun getMyBalance(@Path("id") groupId: Long): BalanceDto
}
