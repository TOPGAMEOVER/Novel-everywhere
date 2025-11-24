package com.novel.everywhere.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class NovelDto(
    val id: Int,
    val title: String,
    val author: String,
    val format: String,
    val size: Int,
    val description: String? = null,
)

@Serializable
data class ProgressDto(
    @SerialName("novel_id") val novelId: Int,
    val chapter: String,
    val offset: Int,
)

@Serializable
data class ReadingSettingsDto(
    @SerialName("font_family") val fontFamily: String,
    @SerialName("font_size") val fontSize: Int,
    @SerialName("line_height") val lineHeight: Int,
    val theme: String,
    @SerialName("bg_color") val bgColor: String,
    @SerialName("tts_voice") val ttsVoice: String,
)

@Serializable
data class TtsResponse(val audio_url: String)

data class Novel(
    val id: Int,
    val title: String,
    val author: String,
    val description: String?,
    val format: String,
    val size: Int,
)

fun NovelDto.toDomain(): Novel = Novel(id, title, author, description, format, size)
