package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.UserDto
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersService {
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long,
        @Header("Authorization") bearer: String
    ): UserDto
    @GET("users")
    suspend fun list(): List<RawUserDto>

    @GET("users")
    suspend fun searchByEmail(@Query("email") email: String): List<RawUserDto>

}
