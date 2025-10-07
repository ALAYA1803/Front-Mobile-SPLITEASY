package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.BillDto
import retrofit2.http.*

interface BillsService {
    @GET("bills")
    suspend fun listAll(): List<BillDto>

    @GET("bills/{id}")
    suspend fun get(@Path("id") id: Long): BillDto

    @POST("bills")
    suspend fun create(@Body body: CreateBillRequest): BillDto

    @PUT("bills/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: CreateBillRequest): BillDto

    @DELETE("bills/{id}")
    suspend fun delete(@Path("id") id: Long)
}

data class CreateBillRequest(
    val householdId: Long,
    val description: String,
    val monto: Double,
    val createdBy: Long,
    val fecha: String
)
