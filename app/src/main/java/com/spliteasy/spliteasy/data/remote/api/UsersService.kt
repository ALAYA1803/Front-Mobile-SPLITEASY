package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersService {
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserDto
}
