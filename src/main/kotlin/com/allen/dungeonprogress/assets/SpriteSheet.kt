package com.allen.dungeonprogress.assets

import java.awt.Graphics2D

/**
 * Row-major sprite sheet renderer.
 *
 * Frame index mapping: idx -> row = idx / cols, col = idx % cols
 * For 2x2 layout: 0=top-left, 1=top-right, 2=bottom-left, 3=bottom-right.
 *
 * Drawing always pulls from ScaledSpriteCache so the source/destination rects
 * are equal size — a pure blit, no scaling work in the paint path.
 */
object SpriteSheet {

    fun frameIndex(key: AssetKey, frameDurationMs: Long = 125L): Int {
        val total = key.frameCount.coerceAtLeast(1)
        return ((System.currentTimeMillis() / frameDurationMs) % total).toInt()
    }

    fun frameIndexFromStart(startMs: Long, frameDurationMs: Long, frameCount: Int, hold: Boolean): Int {
        val elapsed = System.currentTimeMillis() - startMs
        val raw = (elapsed / frameDurationMs).toInt()
        return if (hold) raw.coerceIn(0, frameCount - 1) else (raw % frameCount.coerceAtLeast(1))
    }

    fun draw(g: Graphics2D, key: AssetKey, frameIdx: Int, dstX: Int, dstY: Int, dstW: Int, dstH: Int) {
        val frame = frameIdx.coerceIn(0, key.frameCount - 1)
        val row = frame / key.cols
        val col = frame % key.cols
        val sheet = ScaledSpriteCache.get(key, dstW, dstH)
        val sx = col * dstW
        val sy = row * dstH
        // src/dst same size = pure blit, no scaling
        g.drawImage(sheet, dstX, dstY, dstX + dstW, dstY + dstH, sx, sy, sx + dstW, sy + dstH, null)
    }
}
