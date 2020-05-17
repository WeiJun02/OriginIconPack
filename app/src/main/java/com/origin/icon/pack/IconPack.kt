package com.origin.icon.pack

import android.app.Application
import android.content.Context
import com.origin.icon.pack.util.CrashHandler

class IconPack : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(applicationContext)
    }
}