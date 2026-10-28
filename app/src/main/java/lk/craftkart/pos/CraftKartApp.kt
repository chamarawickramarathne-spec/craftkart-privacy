package lk.craftkart.pos

import android.app.Application
import android.content.Context
import lk.craftkart.pos.data.LocaleManager

class CraftKartApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.attach(base))
    }

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
