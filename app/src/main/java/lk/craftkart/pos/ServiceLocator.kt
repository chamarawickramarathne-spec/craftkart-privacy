package lk.craftkart.pos

import android.content.Context
import lk.craftkart.pos.data.AppRepository
import lk.craftkart.pos.data.CraftKartDatabase
import lk.craftkart.pos.data.SettingsStore

object ServiceLocator {
    lateinit var database: CraftKartDatabase
        private set
    lateinit var repository: AppRepository
        private set
    lateinit var settings: SettingsStore
        private set

    fun init(context: Context) {
        val app = context.applicationContext
        database = CraftKartDatabase.build(app)
        repository = AppRepository(database)
        settings = SettingsStore(app)
    }
}
