package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.ContributionDto
import retrofit2.http.GET

interface ContributionsService {
    @GET("contributions")
    suspend fun listAll(): List<ContributionDto>
}
