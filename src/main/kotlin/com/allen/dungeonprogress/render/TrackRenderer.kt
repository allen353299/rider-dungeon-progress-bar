package com.allen.dungeonprogress.render

import com.allen.dungeonprogress.assets.AssetKey
import com.allen.dungeonprogress.assets.AssetLoader
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D

/** Draws the dungeon background tiled / fitted to the bar, then draws fog of war on the unvisited part. */
object TrackRenderer {

    fun drawBackground(g: Graphics2D, width: Int, height: Int) {
        val bg = AssetLoader.image(AssetKey.BG)
        val aspect = bg.width.toDouble() / bg.height
        val tileW = (height * aspect).toInt().coerceAtLeast(1)
        var x = 0
        while (x < width) {
            g.drawImage(bg, x, 0, tileW, height, null)
            x += tileW
        }
    }

    fun drawFogOfWar(g: Graphics2D, filledX: Int, width: Int, height: Int) {
        if (filledX >= width) return
        val prev = g.composite
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f)
        g.color = Color(0, 0, 0)
        g.fillRect(filledX, 0, width - filledX, height)
        g.composite = prev
    }
}
