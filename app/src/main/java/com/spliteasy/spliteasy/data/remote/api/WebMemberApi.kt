package com.spliteasy.spliteasy.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

import com.spliteasy.spliteasy.data.remote.dto.BillDto
import com.spliteasy.spliteasy.data.remote.dto.ContributionDto
import com.spliteasy.spliteasy.data.remote.dto.MemberContributionDto
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
interface WebMemberApi {

    @GET("households")
    suspend fun listHouseholds(): List<HouseholdDto>

    @GET("households/{id}")
    suspend fun getHousehold(@Path("id") id: Long): HouseholdDto

    @GET("households/{id}/members")
    suspend fun listHouseholdMembers(@Path("id") id: Long): List<Any>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): RawUserDto

    @GET("member-contributions")
    suspend fun listMemberContributions(
        @Query("memberId") memberId: Long,
        @Query("householdId") householdId: Long? = null
    ): List<MemberContributionDto>

    @GET("contributions/{id}")
    suspend fun getContribution(@Path("id") id: Long): ContributionDto

    @GET("bills/{id}")
    suspend fun getBill(@Path("id") id: Long): BillDto

    @GET("member-contributions/{mcId}/receipts")
    suspend fun listReceipts(@Path("mcId") memberContributionId: Long): List<PaymentReceiptDto>

    @Multipart
    @POST("member-contributions/{mcId}/receipts")
    suspend fun uploadReceipt(
        @Path("mcId") memberContributionId: Long,
        @Part file: MultipartBody.Part
    ): PaymentReceiptDto

    @POST("receipts/{receiptId}/approve")
    suspend fun approveReceipt(@Path("receiptId") receiptId: Long): PaymentReceiptDto

    @POST("receipts/{receiptId}/reject")
    suspend fun rejectReceipt(
        @Path("receiptId") receiptId: Long,
        @Query("notes") notes: String? = null
    ): PaymentReceiptDto

    @GET("settings")
    suspend fun listSettings(@Query("user_id") userId: Long): List<Map<String, Any?>>

    @PATCH("settings/{id}")
    suspend fun patchSettings(@Path("id") id: Long, @Body body: Map<String, Any?>): Response<Unit>
}
