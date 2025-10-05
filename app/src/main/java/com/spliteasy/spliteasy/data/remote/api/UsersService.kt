package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UsersService {
    @GET("api/v1/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long,
        @Header("Authorization") bearer: String
    ): UserDto
}
