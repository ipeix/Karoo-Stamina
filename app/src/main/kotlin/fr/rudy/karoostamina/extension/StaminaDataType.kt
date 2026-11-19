package fr.rudy.karoostamina.extension

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class StaminaDataType(
    private val engine: StaminaEngine,
    extension: String,
) : DataTypeImpl(extension, "stamina") {
    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        // Le titre « Stamina » est dessiné dans la vue : on masque l'en-tête standard.
        emitter.onNext(UpdateGraphicConfig(showHeader = false))
        // Case pleine largeur (grille de 60 colonnes) = « ligne », sinon « demi-ligne ».
        val large = config.gridSize.first >= 40
        val job = CoroutineScope(Dispatchers.IO).launch {
            engine.state.collect { live ->
                val snap = if (config.preview) StaminaSnapshot(72, 55, 48, Trend.DOWN) else live
                val result = glance.compose(context, DpSize.Unspecified) {
                    StaminaView(snap, large)
                }
                emitter.updateView(result.remoteViews)
            }
        }
        emitter.setCancellable { job.cancel() }
    }
}
