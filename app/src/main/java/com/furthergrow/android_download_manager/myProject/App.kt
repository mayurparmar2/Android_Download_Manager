package com.furthergrow.android_download_manager.myProject

import android.app.Application
import io.realm.Realm

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}