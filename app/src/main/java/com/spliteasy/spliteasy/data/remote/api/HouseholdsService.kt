package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HouseholdsService {
    @GET("households")
    suspend fun list(): List<HouseholdDto>

    @POST("households")
    suspend fun create(@Body body: CreateHouseholdRequest): HouseholdDto
}

data class CreateHouseholdRequest(
    val name: String,
    val description: String,
    val currency: String,
    val representanteId: Long? = null
)
