package fr.rudy.karoostamina.extension

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension

class StaminaExtension : KarooExtension("stamina", "1.0") {
    private lateinit var karoo: KarooSystemService
    private lateinit var engine: StaminaEngine
    private var started = false

    override val types by lazy {
        listOf(StaminaDataType(engine, extension))
    }

    override fun onCreate() {
        super.onCreate()
        karoo = KarooSystemService(applicationContext)
        engine = StaminaEngine(applicationContext, karoo)
        karoo.connect { connected ->
            if (connected && !started) {
                started = true
                engine.start()
            }
        }
    }

    override fun onDestroy() {
        engine.stop()
        karoo.disconnect()
        super.onDestroy()
    }
}
