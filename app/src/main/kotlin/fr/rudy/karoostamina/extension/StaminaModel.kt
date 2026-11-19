package fr.rudy.karoostamina.extension

import fr.rudy.karoostamina.StaminaSettings
import kotlin.math.max
import kotlin.math.min

enum class Trend { DOWN, STABLE, UP }

data class StaminaSnapshot(
    val stamina: Int,
    val potential: Int,
    /** Minutes avant 0 % à l'effort actuel, null si plus de 4 h. */
    val minutesToZero: Int?,
    val trend: Trend,
)

/**
 * Estimateur de stamina (PAS le calcul Firstbeat de Garmin, qui est propriétaire).
 *
 * - W'bal : modèle différentiel (Froncioni-Clarke). Au-dessus de CP la réserve se vide,
 *   en dessous elle se recharge proportionnellement à (CP - P) et au déficit.
 * - Fatigue lente : travail cumulé rapporté à une capacité d'endurance.
 * - stamina = 100 x reste_capacité x (0,4 + 0,6 x W'bal / W')
 *
 * Les constantes (0,4 / 0,6, capacité automatique, horizon) sont des valeurs de départ à calibrer.
 */
class StaminaModel(private val s: StaminaSettings) {
    private var wBal = s.wPrimeJ
    private var workJ = 0.0

    private val recent = ArrayDeque<Double>() // puissance des 120 dernières secondes
    private val history = ArrayDeque<Double>() // stamina des 60 dernières secondes

    fun reset() {
        wBal = s.wPrimeJ
        workJ = 0.0
        recent.clear()
        history.clear()
    }

    fun step(powerW: Double, dtS: Double) {
        val st = advance(powerW, dtS, wBal, workJ)
        wBal = st.first
        workJ = st.second
        recent.addLast(powerW)
        if (recent.size > 120) recent.removeFirst()
        history.addLast(staminaOf(wBal, workJ))
        if (history.size > 60) history.removeFirst()
    }

    private fun advance(p: Double, dt: Double, w: Double, work: Double): Pair<Double, Double> {
        val cp = s.cpWatts.toDouble()
        val w0 = s.wPrimeJ
        val nw = if (p > cp) {
            max(0.0, w - (p - cp) * dt)
        } else {
            min(w0, w + (cp - p) * (w0 - w) / w0 * dt)
        }
        return nw to (work + max(0.0, p) * dt)
    }

    private fun staminaOf(w: Double, work: Double): Double {
        val fatigue = (1.0 - work / s.capacityJ).coerceIn(0.0, 1.0)
        val wFrac = (w / s.wPrimeJ).coerceIn(0.0, 1.0)
        return 100.0 * fatigue * (0.4 + 0.6 * wFrac)
    }

    fun snapshot(): StaminaSnapshot {
        val now = staminaOf(wBal, workJ)
        val avgP = if (recent.isEmpty()) 0.0 else recent.average()

        // Projection seconde par seconde à la puissance moyenne récente
        var w = wBal
        var work = workJ
        var potential = now
        var zeroAtS: Int? = null
        var t = 0
        while (t < MAX_PROJECTION_S) {
            val st = advance(avgP, 1.0, w, work)
            w = st.first
            work = st.second
            t++
            val v = staminaOf(w, work)
            if (t == HORIZON_S) potential = v
            if (v <= 0.5) {
                zeroAtS = t
                if (t < HORIZON_S) potential = 0.0
                break
            }
        }

        val trend = if (history.size < 30) {
            Trend.STABLE
        } else {
            val d = now - history.first()
            when {
                d < -0.4 -> Trend.DOWN
                d > 0.4 -> Trend.UP
                else -> Trend.STABLE
            }
        }
        return StaminaSnapshot(
            stamina = now.toInt().coerceIn(0, 100),
            potential = potential.toInt().coerceIn(0, 100),
            minutesToZero = zeroAtS?.let { (it + 30) / 60 },
            trend = trend,
        )
    }

    companion object {
        const val HORIZON_S = 600
        const val MAX_PROJECTION_S = 4 * 3600
    }
}
