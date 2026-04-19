package com.allen.dungeonprogress.render

import com.allen.dungeonprogress.assets.AssetKey
import com.allen.dungeonprogress.assets.SpriteSheet
import com.allen.dungeonprogress.model.DungeonStage
import java.awt.Graphics2D

/** Picks the right tica sheet for the stage and draws the current animation frame. */
object HeroRenderer {

    fun draw(g: Graphics2D, stage: DungeonStage, filledX: Int, height: Int, barWidth: Int, reservedRight: Int = 0) {
        val key = when (stage) {
            DungeonStage.CELLAR -> AssetKey.HERO_LEVEL1
            DungeonStage.RUINS -> AssetKey.HERO_LEVEL2
            DungeonStage.CORE, DungeonStage.TREASURY -> AssetKey.HERO_LEVEL3
        }
        val heroH = (height * 0.95).toInt().coerceAtLeast(1)
        val heroW = heroH
        val frame = SpriteSheet.frameIndex(key)
        val maxX = (barWidth - reservedRight - heroW).coerceAtLeast(0)
        val x = (filledX - heroW / 2).coerceIn(0, maxX)
        val y = (height - heroH) / 2
        SpriteSheet.draw(g, key, frame, x, y, heroW, heroH)
    }
}
