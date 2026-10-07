package com.musicsocial.app

import android.app.Application
import com.musicsocial.app.demo.DemoData
import com.musicsocial.app.di.appModules
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MusicSocialApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MusicSocialApp)
            modules(appModules)
        }
        // En modo demo cargamos los datos de ejemplo antes de abrir la primera pantalla.
        if (BuildConfig.DEMO_MODE) runBlocking { get<DemoData>().seed() }
    }
}
