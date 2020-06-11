package hr.ferit.whereisbozidarkelava

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//vlastita klasa koja predstavlja aplikaciju
class WhereIsBK : Application() {
    companion object {
        lateinit var ApplicationContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationContext = this
    }
}