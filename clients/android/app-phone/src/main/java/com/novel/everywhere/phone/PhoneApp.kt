package com.novel.everywhere.phone

import android.app.Application
import com.novel.everywhere.core.di.AppGraph

class PhoneApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
