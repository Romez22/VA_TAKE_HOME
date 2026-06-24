package com.virginactive.android

import android.app.Application
import com.virginactive.android.di.viewModelModule
import com.virginactive.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class VaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@VaApplication)
            modules(viewModelModule)
        }
    }
}
