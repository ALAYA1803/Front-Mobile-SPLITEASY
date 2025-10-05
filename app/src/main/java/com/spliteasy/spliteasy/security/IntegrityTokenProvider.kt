package com.spliteasy.spliteasy.security

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.spliteasy.spliteasy.BuildConfig
import kotlinx.coroutines.tasks.await

object IntegrityTokenProvider {
    suspend fun getToken(context: Context): String {
        val manager = IntegrityManagerFactory.create(context)
        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(BuildConfig.PROJECT_NUMBER)
            .build()
        val response = manager.requestIntegrityToken(request).await()
        return response.token()
    }
}
