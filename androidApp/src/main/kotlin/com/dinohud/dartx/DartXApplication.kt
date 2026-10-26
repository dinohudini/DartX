package com.dinohud.dartx

import android.app.Application
import com.dinohud.dartx.di.androidPlatformModule
import com.dinohud.dartx.di.initKoin
import org.koin.android.ext.koin.androidContext

class DartXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(androidPlatformModule) {
            androidContext(this@DartXApplication)
        }
    }
}
