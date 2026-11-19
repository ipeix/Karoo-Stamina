package fr.rudy.karoostamina.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private val GREEN = 0xFF639922.toInt()
private val AMBER = 0xFFBA7517.toInt()
private val RED = 0xFFE24B4A.toInt()
private val GREY = Color(0xFF888780)

fun staminaColor(stamina: Int): Int = when {
    stamina >= 60 -> GREEN
    stamina >= 30 -> AMBER
    else -> RED
}

/** Jauge à deux niveaux : plein jusqu'au potentiel, clair jusqu'au niveau actuel, trait au potentiel. */
fun drawBar(stamina: Int, potential: Int, color: Int, w: Int, h: Int): Bitmap {
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = h / 2.5f
    p.color = AColor.argb(60, 136, 135, 128)
    c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, p)
    val xNow = w * stamina.coerceIn(0, 100) / 100f
    val xPot = w * potential.coerceIn(0, 100) / 100f
    p.color = AColor.argb(90, AColor.red(color), AColor.green(color), AColor.blue(color))
    c.drawRoundRect(RectF(0f, 0f, xNow, h.toFloat()), r, r, p)
    p.color = color
    c.drawRoundRect(RectF(0f, 0f, xPot, h.toFloat()), r, r, p)
    p.color = AColor.rgb(120, 120, 120)
    p.strokeWidth = (h / 6f).coerceAtLeast(2f)
    c.drawLine(xPot, 0f, xPot, h.toFloat(), p)
    return bmp
}

fun drawArrow(trend: Trend, color: Int, size: Int = 64): Bitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = size / 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val s = size / 64f
    val path = Path()
    when (trend) {
        Trend.DOWN -> {
            path.moveTo(12 * s, 12 * s); path.lineTo(50 * s, 50 * s)
            path.moveTo(22 * s, 50 * s); path.lineTo(50 * s, 50 * s); path.lineTo(50 * s, 22 * s)
        }
        Trend.STABLE -> {
            path.moveTo(8 * s, 32 * s); path.lineTo(54 * s, 32 * s)
            path.moveTo(38 * s, 16 * s); path.lineTo(54 * s, 32 * s); path.lineTo(38 * s, 48 * s)
        }
        Trend.UP -> {
            path.moveTo(12 * s, 52 * s); path.lineTo(50 * s, 14 * s)
            path.moveTo(22 * s, 14 * s); path.lineTo(50 * s, 14 * s); path.lineTo(50 * s, 42 * s)
        }
    }
    c.drawPath(path, p)
    return bmp
}

private fun formatMinutes(m: Int): String =
    if (m >= 60) "${m / 60} h ${(m % 60).toString().padStart(2, '0')}" else "$m min"

/** large = case "ligne" (pleine largeur), sinon "demi-ligne". */
@Composable
fun StaminaView(snap: StaminaSnapshot, large: Boolean) {
    val color = staminaColor(snap.stamina)
    val composeColor = Color(color)
    val numberSize = if (large) 56.sp else 40.sp
    val labelSize = if (large) 13.sp else 12.sp
    val footSize = if (large) 12.sp else 11.sp
    val barH = if (large) 20 else 16
    val barBmp = drawBar(snap.stamina, snap.potential, color, if (large) 480 else 240, barH)
    val arrowBmp = drawArrow(snap.trend, color)
    val arrowDp = if (large) 28.dp else 22.dp

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text("Stamina", style = TextStyle(color = ColorProvider(GREY), fontSize = labelSize))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = GlanceModifier.defaultWeight()) {}
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    snap.stamina.toString(),
                    style = TextStyle(
                        color = ColorProvider(composeColor),
                        fontSize = numberSize,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Text(
                    " %",
                    style = TextStyle(color = ColorProvider(GREY), fontSize = if (large) 20.sp else 16.sp),
                )
            }
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Image(
                    provider = ImageProvider(arrowBmp),
                    contentDescription = "Tendance",
                    modifier = GlanceModifier.size(arrowDp),
                )
            }
        }
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(barBmp),
            contentDescription = "Jauge stamina",
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxWidth().height(if (large) 10.dp else 8.dp),
        )
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)) {
            if (large) {
                val txt = snap.minutesToZero?.let { "0 % dans ${formatMinutes(it)}" } ?: "0 % : plus de 4 h"
                Text(
                    txt,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(color = ColorProvider(GREY), fontSize = footSize),
                )
            } else {
                Box(modifier = GlanceModifier.defaultWeight()) {}
            }
            Text(
                "Pot. ${snap.potential} %",
                style = TextStyle(color = ColorProvider(GREY), fontSize = footSize),
            )
        }
    }
}
