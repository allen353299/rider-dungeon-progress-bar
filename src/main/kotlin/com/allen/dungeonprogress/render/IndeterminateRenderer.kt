package com.allen.dungeonprogress.render

import com.allen.dungeonprogress.assets.AssetKey
import com.allen.dungeonprogress.assets.SpriteSheet
import java.awt.Graphics2D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Hero in the middle, three skeletons orbiting around it. */
object IndeterminateRenderer {

    private const val ORBIT_PERIOD_MS = 2200.0

    fun draw(g: Graphics2D, width: Int, height: Int) {
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
            val sy = cy + (sin(angle) * orbitR / 2).toInt() - skeletonSize / 2  // squashed orbit (perspective)
            val frame = (SpriteSheet.frameIndex(skeletonKey) + i) % skeletonKey.frameCount
            SpriteSheet.draw(g, skeletonKey, frame, sx, sy, skeletonSize, skeletonSize)
        }
    }
}
