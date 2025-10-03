package com.spliteasy.spliteasy.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: com.spliteasy.spliteasy.data.local.TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        val path = req.url.encodedPath
        val isAuthEndpoint = path.contains("/authentication/")

        if (isAuthEndpoint) {
            return chain.proceed(req)
        }

        val token = runBlocking { tokenStore.readToken() }

        val newReq = if (!token.isNullOrBlank()) {
            req.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            req
        }
        return chain.proceed(newReq)
    }
}
