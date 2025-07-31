package com.tobiasdroste.papercups.app

import android.app.Application
import com.tobiasdroste.papercups.BuildConfig
import com.tobiasdroste.papercups.app.logging.LoggingStrategy
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@HiltAndroidApp
class CupsPrintApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize logging strategy
        LoggingStrategy.init(BuildConfig.DEBUG)

        startKoin {
            androidLogger()
            androidContext(this@CupsPrintApp)
        }
    }
}
