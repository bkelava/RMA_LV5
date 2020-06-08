package hr.ferit.soundpoolapp

import android.app.Application
import android.content.Context

//vlastita klasa koja predstavlja aplikaciju
class Soundpool : Application() {
    companion object {
        lateinit var ApplicationContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationContext = this
    }
}