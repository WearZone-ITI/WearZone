package com.example.wearzone.data.remote.interceptor

import android.util.Log
import com.example.wearzone.data.local.datasource.IPayMockLocalDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class PayMockAuthInterceptor @Inject constructor(
    private val localDataSource: IPayMockLocalDataSource
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Don't authenticate project creation
        if (request.url.encodedPath.endsWith("/projects")) {
            return chain.proceed(request)
        }

        val apiKey = runBlocking { localDataSource.getApiKey() }

        val authenticatedRequest =
            if (!apiKey.isNullOrBlank()) {
                request.newBuilder()
                    .header("Authorization", "Bearer $apiKey")
                    .header("Accept", "application/json")
                    .build()
            } else {
                request
            }

        val response = chain.proceed(authenticatedRequest)

        if (!response.isSuccessful) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            Log.e("PayMockError", "Code: ${response.code} URL: ${request.url} Body: $body")
        }

        return response
    }
}
