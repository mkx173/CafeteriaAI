package com.tohoku.cafeteria

import android.app.Application
import com.tohoku.cafeteria.data.AppContainer
import com.tohoku.cafeteria.data.DefaultAppContainer
import com.tohoku.cafeteria.data.MockAppContainer
import com.tohoku.cafeteria.util.ToastManager

class CafeteriaApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        ToastManager.init(this)
    }
}
