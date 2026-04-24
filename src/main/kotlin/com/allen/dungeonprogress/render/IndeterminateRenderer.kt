package com.allen.dungeonprogress.render

import com.allen.dungeonprogress.StageResolver
import com.allen.dungeonprogress.assets.AssetKey
import com.allen.dungeonprogress.assets.SpriteSheet
import com.allen.dungeonprogress.model.CriticalHitEvent
import com.allen.dungeonprogress.model.DungeonStage
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Indeterminate mode plays the full dungeon fight as a ~16.5s loop, mirroring
 * the FakeFullDemo sequence: orbit skeletons -> climb 0-70% -> boss battle 70-100%
 * -> chest opens -> indeterminate bridge -> 4 critical jumps -> repeat.
 *
 * Phase timings match FakeFullDemoAction so the visual stays identical to the
 * one users see when they run the determinate demo.
 */
object IndeterminateRenderer {

    private const val PHASE_INTRO_MS = 2000L      // orbit skeletons
    private const val PHASE_WALK_MS = 3195L       // normal 0 -> 70  (71 * 45ms)
    private const val PHASE_BOSS_MS = 3900L       // boss 70 -> 100  (30 * 130ms)
    private const val PHASE_TREASURY_MS = 2000L   // chest hold
    private const val PHASE_BRIDGE_MS = 1000L     // back to orbit
    private const val PHASE_CRITICAL_MS = 4500L   // 5 * 800ms + 500ms hold
    private const val CYCLE_MS =
        PHASE_INTRO_MS + PHASE_WALK_MS + PHASE_BOSS_MS +
            PHASE_TREASURY_MS + PHASE_BRIDGE_MS + PHASE_CRITICAL_MS

    private const val BATTLE_START_PERCENT = 70.0
    private const val CRITICAL_STEP_MS = 800L
    private const val ORBIT_PERIOD_MS = 2200.0

    fun draw(g: Graphics2D, width: Int, height: Int) {
        val t = System.currentTimeMillis() % CYCLE_MS
        val tIntroEnd = PHASE_INTRO_MS
        val tWalkEnd = tIntroEnd + PHASE_WALK_MS
        val tBossEnd = tWalkEnd + PHASE_BOSS_MS
        val tTreasuryEnd = tBossEnd + PHASE_TREASURY_MS
        val tBridgeEnd = tTreasuryEnd + PHASE_BRIDGE_MS

        when {
            t < tIntroEnd -> drawOrbit(g, width, height)
            t < tWalkEnd -> {
                val local = (t - tIntroEnd).toDouble() / PHASE_WALK_MS
                drawDungeon(g, width, height, percent = local * BATTLE_START_PERCENT, critical = null)
            }
            t < tBossEnd -> {
                val local = (t - tWalkEnd).toDouble() / PHASE_BOSS_MS
                val p = BATTLE_START_PERCENT + local * (100.0 - BATTLE_START_PERCENT)
                drawDungeon(g, width, height, percent = p.coerceAtMost(99.9), critical = null)
            }
            t < tTreasuryEnd -> {
                drawDungeon(
                    g, width, height,
                    percent = 100.0,
                    critical = null,
                    treasuryElapsedMs = t - tBossEnd,
                )
            }
            t < tBridgeEnd -> drawOrbit(g, width, height)
            else -> drawCriticalPhase(g, width, height, phaseMs = t - tBridgeEnd)
        }
    }

    private fun drawOrbit(g: Graphics2D, width: Int, height: Int) {
        TrackRenderer.drawBackground(g, width, height)
        val prev = g.composite
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f)
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
        g.composite = prev

        val cx = width / 2
        val cy = height / 2
        val heroKey = AssetKey.HERO_LEVEL1
        val heroSize = (height * 0.95).toInt().coerceAtLeast(1)
        val heroFrame = SpriteSheet.frameIndex(heroKey)
        SpriteSheet.draw(g, heroKey, heroFrame, cx - heroSize / 2, cy - heroSize / 2, heroSize, heroSize)

        val skeletonKey = AssetKey.SKELETON
        val skeletonSize = (height * 0.7).toInt().coerceAtLeast(1)
        val orbitR = (height * 0.9).toInt()
        val baseAngle = (System.currentTimeMillis() % ORBIT_PERIOD_MS.toLong()) / ORBIT_PERIOD_MS * 2 * PI
        for (i in 0 until 3) {
            val angle = baseAngle + i * (2 * PI / 3)
            val sx = cx + (cos(angle) * orbitR).toInt() - skeletonSize / 2
            val sy = cy + (sin(angle) * orbitR / 2).toInt() - skeletonSize / 2
            val frame = (SpriteSheet.frameIndex(skeletonKey) + i) % skeletonKey.frameCount
            SpriteSheet.draw(g, skeletonKey, frame, sx, sy, skeletonSize, skeletonSize)
        }
    }

    private fun drawCriticalPhase(g: Graphics2D, width: Int, height: Int, phaseMs: Long) {
        // 5 jumps at 0/25/50/75/100, 800ms apart, clamp last so we hold on 100
        val jumpIdx = (phaseMs / CRITICAL_STEP_MS).toInt().coerceIn(0, 4)
        val percent = (jumpIdx * 25).toDouble()
        val timeSinceJump = phaseMs - jumpIdx * CRITICAL_STEP_MS
        // Skip the pop-up on the first jump (percent=0) to match the real
        // detectCritical which needs a previous sample; jumps 1..4 all fire.
        val critical = if (jumpIdx >= 1) {
            CriticalHitEvent(System.currentTimeMillis() - timeSinceJump)
        } else null
        // Treasury enters at jumpIdx=4 (percent=100); anchor chest animation to that jump.
        val treasuryElapsed = if (percent >= 96.0) timeSinceJump else -1L
        drawDungeon(g, width, height, percent = percent, critical = critical, treasuryElapsedMs = treasuryElapsed)
    }

    /** Shared dungeon render used by walk / boss / treasury / critical phases. */
    private fun drawDungeon(
        g: Graphics2D,
        width: Int,
        height: Int,
        percent: Double,
        critical: CriticalHitEvent?,
        treasuryElapsedMs: Long = -1L,
    ) {
        val filledX = (percent / 100.0 * width).toInt()
        val stage = StageResolver.resolve(percent)

        TrackRenderer.drawBackground(g, width, height)
        TrackRenderer.drawFogOfWar(g, filledX, width, height)
        drawFill(g, filledX, width, height)

        val bossVisible = percent >= BATTLE_START_PERCENT
        if (bossVisible && stage != DungeonStage.TREASURY) {
            drawBossSprite(g, width, height, percent)
        } else if (stage == DungeonStage.TREASURY && treasuryElapsedMs >= 0) {
            drawChest(g, width, height, treasuryElapsedMs)
        }

        val reservedRight = if (bossVisible) (height * 1.4).toInt() else 0
        HeroRenderer.draw(g, stage, filledX, height, width, reservedRight)

        critical?.let { OverlayRenderer.drawCritical(g, it, filledX, height) }
    }

    private fun drawFill(g: Graphics2D, filledX: Int, width: Int, height: Int) {
        if (filledX <= 0) return
        val prevClip = g.clip
        g.clipRect(0, 0, filledX, height)
        val prevComp = g.composite
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f)
        g.paint = LinearGradientPaint(
            0f, 0f, width.toFloat(), 0f,
            floatArrayOf(0f, 1f),
            arrayOf(Color(0x3C, 0xB3, 0x71), Color(0xFF, 0xD7, 0x00)),
        )
        g.fillRect(0, 0, width, height)
        g.composite = prevComp
        g.clip = prevClip
    }

    private fun drawBossSprite(g: Graphics2D, w: Int, h: Int, percent: Double) {
        val bossFrames = AssetKey.BOSS.frameCount
        val span = 100.0 - BATTLE_START_PERCENT
        val local = ((percent - BATTLE_START_PERCENT) / span).coerceIn(0.0, 0.999)
        val bossFrame = (local * bossFrames).toInt().coerceIn(0, bossFrames - 1)
        val sprite = (h * 1.4).toInt().coerceAtLeast(1)
        val rightX = w - sprite
        SpriteSheet.draw(g, AssetKey.BOSS, bossFrame, rightX, (h - sprite) / 2, sprite, sprite)
    }

    private fun drawChest(g: Graphics2D, w: Int, h: Int, elapsedMs: Long) {
        val frameDur = 250L
        val chestFrames = AssetKey.CHEST.frameCount
        val chestFrame = (elapsedMs / frameDur).toInt().coerceIn(0, chestFrames - 1)
        val sprite = (h * 1.4).toInt().coerceAtLeast(1)
        val rightX = w - sprite
        SpriteSheet.draw(g, AssetKey.CHEST, chestFrame, rightX, (h - sprite) / 2, sprite, sprite)
    }
}
