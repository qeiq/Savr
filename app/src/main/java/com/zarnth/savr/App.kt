package com.zarnth.savr

import android.app.Application
import com.zarnth.savr.di.savrModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(savrModule)
        }
    }
}