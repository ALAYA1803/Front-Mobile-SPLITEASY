package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MemberContributionsService {
    @GET("member-contributions")
    suspend fun listAll(): List<MemberContributionDto>

    @GET("member-contributions")
    suspend fun listByContribution(@Query("contributionId") contributionId: Long): List<MemberContributionDto>

    @GET("member-contributions/{id}")
    suspend fun get(@Path("id") id: Long): MemberContributionDto
}
