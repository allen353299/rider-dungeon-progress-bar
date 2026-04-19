package com.allen.dungeonprogress

import com.allen.dungeonprogress.assets.AssetKey
import com.allen.dungeonprogress.assets.SpriteSheet
import com.allen.dungeonprogress.model.CriticalHitEvent
import com.allen.dungeonprogress.model.DungeonStage
import com.allen.dungeonprogress.render.HeroRenderer
import com.allen.dungeonprogress.render.IndeterminateRenderer
import com.allen.dungeonprogress.render.OverlayRenderer
import com.allen.dungeonprogress.render.TrackRenderer
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import java.awt.RenderingHints
import javax.swing.JComponent
import javax.swing.JProgressBar
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicProgressBarUI

class DungeonProgressBarUI : BasicProgressBarUI() {

    private var lastPercent: Double = -1.0
    private var lastCritical: CriticalHitEvent? = null
    private var treasuryEnteredMs: Long = -1L

    override fun installUI(c: JComponent) {
        super.installUI(c)
        if (c is JProgressBar) AnimationTicker.register(c)
    }

    override fun uninstallUI(c: JComponent) {
        if (c is JProgressBar) AnimationTicker.unregister(c)
        super.uninstallUI(c)
    }

    override fun paintDeterminate(g: Graphics, c: JComponent) {
        val bar = c as? JProgressBar ?: return super.paintDeterminate(g, c)
        val w = bar.width
        val h = bar.height
        if (w <= 0 || h <= 0) return

        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

            val span = (bar.maximum - bar.minimum).coerceAtLeast(1)
            val percent = ((bar.value - bar.minimum).toDouble() / span * 100.0).coerceIn(0.0, 100.0)
            val stage = StageResolver.resolve(percent)
            val filledX = (percent / 100.0 * w).toInt()

            detectCritical(percent)

            // 1. Background corridor
            TrackRenderer.drawBackground(g2, w, h)

            // 2. Fog of war on the unvisited part
            TrackRenderer.drawFogOfWar(g2, filledX, w, h)

            // 3. Green->gold progress fill
            if (filledX > 0) {
                val prevClip = g2.clip
                g2.clipRect(0, 0, filledX, h)
                val prevComp = g2.composite
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f)
                g2.paint = LinearGradientPaint(
                    0f, 0f, w.toFloat(), 0f,
                    floatArrayOf(0f, 1f),
                    arrayOf(Color(0x3C, 0xB3, 0x71), Color(0xFF, 0xD7, 0x00)),
                )
                g2.fillRect(0, 0, w, h)
                g2.composite = prevComp
                g2.clip = prevClip
            }

            // 4. Boss battle: boss becomes visible from BATTLE_START_PERCENT onward.
            // Frame advances with progress (you fight through it), then in TREASURY the chest plays.
            val bossVisible = percent >= BATTLE_START_PERCENT
            if (bossVisible && stage != DungeonStage.TREASURY) {
                paintBossApproach(g2, w, h, percent)
            } else if (stage == DungeonStage.TREASURY) {
                if (treasuryEnteredMs < 0) treasuryEnteredMs = System.currentTimeMillis()
                paintTreasury(g2, w, h)
            } else {
                treasuryEnteredMs = -1
            }

            // 5. Hero (reserve the right slot for boss/chest whenever boss is visible)
            val reservedRight = if (bossVisible) (h * 1.4).toInt() else 0
            HeroRenderer.draw(g2, stage, filledX, h, w, reservedRight)

            // 6. Critical-hit overlay
            lastCritical?.let { OverlayRenderer.drawCritical(g2, it, filledX, h) }

            lastPercent = percent
        } finally {
            g2.dispose()
        }
    }

    override fun paintIndeterminate(g: Graphics, c: JComponent) {
        val bar = c as? JProgressBar ?: return super.paintIndeterminate(g, c)
        val w = bar.width
        val h = bar.height
        if (w <= 0 || h <= 0) return

        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            TrackRenderer.drawBackground(g2, w, h)
            // dim the whole bar so the orbiting hero pops out
            val prev = g2.composite
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f)
            g2.color = Color.BLACK
            g2.fillRect(0, 0, w, h)
            g2.composite = prev
            IndeterminateRenderer.draw(g2, w, h)
        } finally {
            g2.dispose()
        }
    }

    private fun detectCritical(percent: Double) {
        if (lastPercent < 0) return
        val delta = percent - lastPercent
        if (delta >= 20.0) lastCritical = CriticalHitEvent(System.currentTimeMillis())
    }

    /** Boss is on screen while progress is between BATTLE_START_PERCENT and 100.
     *  Its 4-frame death sequence is mapped to that progress range so the boss visibly
     *  collapses as hero closes the distance. */
    private fun paintBossApproach(g: Graphics2D, w: Int, h: Int, percent: Double) {
        val bossFrames = AssetKey.BOSS.frameCount
        val span = 100.0 - BATTLE_START_PERCENT
        val local = ((percent - BATTLE_START_PERCENT) / span).coerceIn(0.0, 0.999)
        val bossFrame = (local * bossFrames).toInt().coerceIn(0, bossFrames - 1)
        val sprite = (h * 1.4).toInt().coerceAtLeast(1)
        val rightX = w - sprite
        SpriteSheet.draw(g, AssetKey.BOSS, bossFrame, rightX, (h - sprite) / 2, sprite, sprite)
    }

    /** At 100% the boss has already fallen; play the chest opening sequence (time-driven, holds). */
    private fun paintTreasury(g: Graphics2D, w: Int, h: Int) {
        val frameDur = 250L
        val chestFrames = AssetKey.CHEST.frameCount
        val chestFrame = SpriteSheet.frameIndexFromStart(treasuryEnteredMs, frameDur, chestFrames, hold = true)
        val sprite = (h * 1.4).toInt().coerceAtLeast(1)
        val rightX = w - sprite
        SpriteSheet.draw(g, AssetKey.CHEST, chestFrame, rightX, (h - sprite) / 2, sprite, sprite)
    }

    companion object {
        /** Percent at which the boss becomes visible; the fight plays out across the remaining bar. */
        private const val BATTLE_START_PERCENT = 70.0

        @JvmStatic
        @Suppress("UNUSED_PARAMETER", "ACCIDENTAL_OVERRIDE")
        fun createUI(c: JComponent): ComponentUI = DungeonProgressBarUI()
    }
}
