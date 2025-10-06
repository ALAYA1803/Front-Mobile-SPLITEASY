package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.BillDto
import retrofit2.http.GET

interface BillsService {
    @GET("bills")
    suspend fun listAll(): List<BillDto>
}
