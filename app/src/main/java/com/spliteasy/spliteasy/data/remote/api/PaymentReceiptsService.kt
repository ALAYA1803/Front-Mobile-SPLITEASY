package com.spliteasy.spliteasy.data.remote.api

import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface PaymentReceiptsService {
    @GET("member-contributions/{mcId}/receipts")
    suspend fun list(@Path("mcId") memberContributionId: Long): List<PaymentReceiptDto>

    @Multipart
    @POST("member-contributions/{mcId}/receipts")
    suspend fun upload(
        @Path("mcId") memberContributionId: Long,
        @Part file: MultipartBody.Part
    ): PaymentReceiptDto

    @POST("receipts/{receiptId}/approve")
    suspend fun approve(@Path("receiptId") receiptId: Long): PaymentReceiptDto

    @POST("receipts/{receiptId}/reject")
    suspend fun reject(
        @Path("receiptId") receiptId: Long,
        @Query("notes") notes: String? = null
    ): PaymentReceiptDto
}
