package com.example.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        val skipAuth = path.contains("/api/auth/login") ||
                path.contains("/api/auth/signup") ||
                path.contains("/api/auth/verify") ||
                path.contains("/api/webhooks/")

        val session = runBlocking { sessionManager.currentSession() }
        val token = session.authToken
        val locale = session.locale

        val requestBuilder = original.newBuilder()

        if (!skipAuth && !token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        requestBuilder.header("Accept-Language", locale)

        val response = chain.proceed(requestBuilder.build())

        if (!skipAuth && response.code == 401) {
            runBlocking { sessionManager.clearSession() }
        }

        return response
    }
}
