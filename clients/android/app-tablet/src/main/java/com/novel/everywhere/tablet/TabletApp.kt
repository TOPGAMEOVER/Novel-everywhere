package com.novel.everywhere.tablet

import android.app.Application
import com.novel.everywhere.core.di.AppGraph

class TabletApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
