package com.musicsocial.app

import android.app.Application
import com.musicsocial.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MusicSocialApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MusicSocialApp)
            modules(appModule)
        }
    }
}
