package com.novel.everywhere.core.network

import com.novel.everywhere.core.data.NovelDto
import com.novel.everywhere.core.data.ProgressDto
import com.novel.everywhere.core.data.ReadingSettingsDto
import com.novel.everywhere.core.data.TokenResponse
import com.novel.everywhere.core.data.TtsResponse
import com.novel.everywhere.core.data.UserDto
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface NovelApi {

    @POST("auth/login")
    suspend fun login(@Body payload: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body payload: RegisterRequest): UserDto

    @GET("profile/me")
    suspend fun profile(): UserDto

    @GET("novels")
    suspend fun listNovels(): List<NovelDto>

    @Multipart
    @POST("novels")
    suspend fun uploadNovel(
        @Part file: MultipartBody.Part,
        @Part("title") title: String?,
        @Part("author") author: String?,
        @Part("description") description: String?,
    ): NovelDto

    @GET("novels/{id}/progress")
    suspend fun progress(@Path("id") novelId: Int): ProgressDto

    @POST("novels/{id}/progress")
    suspend fun updateProgress(@Path("id") novelId: Int, @Body body: ProgressDto): ProgressDto

    @GET("settings/reading")
    suspend fun getSettings(): ReadingSettingsDto

    @POST("tts")
    suspend fun createTts(@Body body: TtsRequest): TtsResponse
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @kotlinx.serialization.SerialName("display_name") val displayName: String,
)

@Serializable
data class TtsRequest(val text: String, val voice: String? = null, val rate: Int? = null)
