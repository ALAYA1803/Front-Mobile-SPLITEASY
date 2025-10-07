package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.ContributionDto
import retrofit2.http.*

interface ContributionsService {
    @GET("contributions")
    suspend fun listAll(): List<ContributionDto>

    @GET("contributions/{id}")
    suspend fun get(@Path("id") id: Long): ContributionDto

    @POST("contributions")
    suspend fun create(@Body body: CreateContributionRequest): ContributionDto

    @PUT("contributions/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: UpdateContributionRequest): ContributionDto

    @DELETE("contributions/{id}")
    suspend fun delete(@Path("id") id: Long)
}

data class CreateContributionRequest(
    val billId: Long,
    val householdId: Long,
    val description: String,
    val strategy: String,
    val fechaLimite: String,
    val memberIds: List<Long>
)

data class UpdateContributionRequest(
    val billId: Long,
    val householdId: Long,
    val description: String,
    val strategy: String,
    val fechaLimite: String
)
