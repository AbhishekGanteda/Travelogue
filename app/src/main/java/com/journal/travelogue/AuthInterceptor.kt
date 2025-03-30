package com.journal.travelogue

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider.invoke() // Retrieve token from SharedPreferences

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token") // Attach token in the header
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}