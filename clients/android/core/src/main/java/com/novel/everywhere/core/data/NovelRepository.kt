package com.novel.everywhere.core.data

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import com.novel.everywhere.core.network.LoginRequest
import com.novel.everywhere.core.network.NovelApi
import com.novel.everywhere.core.network.TtsRequest

class NovelRepository(
    private val api: NovelApi,
    private val sessionManager: SessionManager,
) {

    suspend fun login(email: String, password: String) {
        val token = api.login(LoginRequest(email, password))
        sessionManager.saveToken(token.accessToken)
    }

    suspend fun fetchProfile(): UserDto = api.profile()

    suspend fun fetchNovels(): List<Novel> = api.listNovels().map { it.toDomain() }

    suspend fun uploadNovel(
        resolver: ContentResolver,
        uri: Uri,
        title: String?,
        author: String?,
        description: String?,
    ): Novel {
        val file = withContext(Dispatchers.IO) { copyFile(resolver, uri) }
        val payload = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("application/octet-stream".toMediaTypeOrNull()),
        )
        val response = api.uploadNovel(payload, title, author, description)
        file.delete()
        return response.toDomain()
    }

    suspend fun updateProgress(novelId: Int, chapter: String, offset: Int): ProgressDto {
        return api.updateProgress(novelId, ProgressDto(novelId, chapter, offset))
    }

    suspend fun getProgress(novelId: Int): ProgressDto = api.progress(novelId)

    suspend fun getSettings(): ReadingSettingsDto = api.getSettings()

    suspend fun requestTts(text: String, voice: String?) = api.createTts(TtsRequest(text, voice, null))

    suspend fun logout() {
        sessionManager.clear()
    }

    private fun copyFile(resolver: ContentResolver, uri: Uri): File {
        val dest = File.createTempFile("novel_", ".tmp")
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        return dest
    }
}
