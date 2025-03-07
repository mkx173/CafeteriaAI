package com.tohoku.cafeteria;

import android.app.Application
import com.tohoku.cafeteria.data.AppContainer
import com.tohoku.cafeteria.data.MockAppContainer

class CafeteriaApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = MockAppContainer(this)
    }
}
