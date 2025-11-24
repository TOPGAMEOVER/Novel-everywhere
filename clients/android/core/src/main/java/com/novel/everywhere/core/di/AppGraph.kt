package com.novel.everywhere.core.di

import android.content.Context
import com.novel.everywhere.core.data.NovelRepository
import com.novel.everywhere.core.data.SessionManager
import com.novel.everywhere.core.network.ApiClient

class AppGraph(context: Context, baseUrl: String = DEFAULT_BASE_URL) {

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    }

    private val sessionManager = SessionManager(context)
    private val api = ApiClient(baseUrl).build(sessionManager)

    val repository: NovelRepository = NovelRepository(api, sessionManager)
}
