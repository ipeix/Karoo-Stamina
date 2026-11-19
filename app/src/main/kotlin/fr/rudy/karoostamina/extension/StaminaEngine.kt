package fr.rudy.karoostamina.extension

import android.content.Context
import android.util.Log
import fr.rudy.karoostamina.StaminaPrefs
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Calcule la stamina une fois par seconde pendant qu'une sortie est en cours d'enregistrement.
 * Les réglages (CP, W') sont relus au début de chaque sortie.
 */
class StaminaEngine(
    private val context: Context,
    private val karoo: KarooSystemService,
) {
    private val _state = MutableStateFlow(StaminaSnapshot(100, 100, null, Trend.STABLE))
    val state: StateFlow<StaminaSnapshot> = _state

    @Volatile private var latestPower: Double = 0.0
    @Volatile private var rideState: RideState = RideState.Idle

    private var model = StaminaModel(StaminaPrefs.load(context))
    private val jobs = mutableListOf<Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        jobs += scope.launch {
            karoo.streamDataFlow(DataType.Type.POWER).collect {
                latestPower = (it as? StreamState.Streaming)?.dataPoint?.singleValue ?: 0.0
            }
        }
        jobs += scope.launch {
            karoo.consumerFlow<RideState>().collect { rs ->
                val wasIdle = rideState is RideState.Idle
                rideState = rs
                if (rs is RideState.Recording && wasIdle) {
                    model = StaminaModel(StaminaPrefs.load(context))
                    publish()
                } else if (rs is RideState.Idle) {
                    model = StaminaModel(StaminaPrefs.load(context))
                    publish()
                }
            }
        }
        jobs += scope.launch {
            while (true) {
                delay(1000)
                if (rideState is RideState.Recording) {
                    model.step(latestPower, 1.0)
                    publish()
                }
            }
        }
    }

    private fun publish() {
        try {
            _state.value = model.snapshot()
        } catch (e: Exception) {
            Log.e(TAG, "snapshot failed", e)
        }
    }

    fun stop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private companion object {
        const val TAG = "StaminaEngine"
    }
}
