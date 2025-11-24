package com.novel.everywhere.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.novel.everywhere.core.data.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class ApiClient(
    private val baseUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun build(sessionManager: SessionManager): NovelApi {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val authInterceptor = Interceptor { chain ->
            val token = kotlinx.coroutines.runBlocking { sessionManager.tokenFlow.first() }
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(authInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(NovelApi::class.java)
    }
}
