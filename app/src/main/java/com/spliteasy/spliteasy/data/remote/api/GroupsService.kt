package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import com.spliteasy.spliteasy.data.remote.dto.MemberDto
import com.spliteasy.spliteasy.data.remote.api.CreateHouseholdRequest
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body

interface GroupsService {
    @GET("households")
    suspend fun listHouseholds(): List<HouseholdDto>

    @GET("households/{id}")
    suspend fun getHousehold(@Path("id") id: Long): HouseholdDto

    @GET("household-members")
    suspend fun listAllHouseholdMembers(): List<MemberDto>

    @POST("households")
    suspend fun createHousehold(@Body body: Map<String, Any>): HouseholdDto
}
