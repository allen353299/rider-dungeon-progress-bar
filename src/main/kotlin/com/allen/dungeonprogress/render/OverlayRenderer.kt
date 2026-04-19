package com.allen.dungeonprogress.render

import com.allen.dungeonprogress.model.CriticalHitEvent
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import kotlin.math.PI
import kotlin.math.sin

/** "CRITICAL!" pop-up text that bounces over the hero when a big jump happens. */
object OverlayRenderer {

    fun drawCritical(g: Graphics2D, event: CriticalHitEvent, anchorX: Int, height: Int) {
        val now = System.currentTimeMillis()
        if (!event.isActive(now)) return
        val progress = event.progress(now)
        val fontSize = (height * 0.55f).coerceAtLeast(8f)
        g.font = Font(Font.DIALOG, Font.BOLD, fontSize.toInt())
        val text = "CRITICAL!"
        val fm = g.fontMetrics
        val tw = fm.stringWidth(text)
        val baseY = (height * 0.6).toInt()
        val bounce = (sin(progress * PI) * height * 0.4).toInt()
        val x = anchorX - tw / 2
        val y = baseY - bounce
        val alpha = (255 * (1.0 - progress * progress)).toInt().coerceIn(0, 255)
        // outline
        g.color = Color(0, 0, 0, alpha)
        for (dx in -1..1) for (dy in -1..1) if (dx != 0 || dy != 0) g.drawString(text, x + dx, y + dy)
        g.color = Color(255, 215, 0, alpha)
        g.drawString(text, x, y)
    }
}
