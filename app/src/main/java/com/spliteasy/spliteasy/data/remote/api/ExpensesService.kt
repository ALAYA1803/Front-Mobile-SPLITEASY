package com.spliteasy.spliteasy.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ExpenseDto(
    val id: Long,
    val description: String,
    val amount: Double
)

interface ExpensesService {
    @GET("groups/{id}/expenses")
    suspend fun getRecentExpenses(
        @Path("id") groupId: Long,
        @Query("limit") limit: Int = 5
    ): List<ExpenseDto>
}
