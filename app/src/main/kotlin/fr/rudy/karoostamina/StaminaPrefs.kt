package fr.rudy.karoostamina

import android.content.Context

/** Réglages saisis à la main. Lus au démarrage d'une sortie. */
data class StaminaSettings(
    val cpWatts: Int,
    val wPrimeKj: Float,
    /** Capacité d'endurance en kJ. 0 = automatique (CP x 5,4 kJ, soit environ 1 h 30 à CP). */
    val capacityKj: Int,
) {
    val wPrimeJ: Double get() = wPrimeKj * 1000.0
    val capacityJ: Double get() = if (capacityKj > 0) capacityKj * 1000.0 else cpWatts * 5400.0
}

object StaminaPrefs {
    const val CP_MIN = 100
    const val CP_MAX = 500
    const val WPRIME_MIN = 5f
    const val WPRIME_MAX = 40f
    const val CAPACITY_MIN = 300
    const val CAPACITY_MAX = 6000

    private const val NAME = "stamina_prefs"
    private const val K_CP = "cp"
    private const val K_WPRIME_J = "wprime_j"
    private const val K_CAPACITY = "capacity_kj"

    fun load(context: Context): StaminaSettings {
        val p = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return StaminaSettings(
            cpWatts = p.getInt(K_CP, 250),
            wPrimeKj = p.getInt(K_WPRIME_J, 20000) / 1000f,
            capacityKj = p.getInt(K_CAPACITY, 0),
        )
    }

    fun save(context: Context, s: StaminaSettings) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(K_CP, s.cpWatts)
            .putInt(K_WPRIME_J, Math.round(s.wPrimeKj * 1000f))
            .putInt(K_CAPACITY, s.capacityKj)
            .apply()
    }
}
