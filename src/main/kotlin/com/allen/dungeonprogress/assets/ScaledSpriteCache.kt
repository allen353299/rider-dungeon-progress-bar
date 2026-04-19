package com.allen.dungeonprogress.assets

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.Collections
import java.util.LinkedHashMap

/**
 * Caches sprite sheets pre-scaled to a target frame size. Once cached, painting
 * is a 1:1 blit (no per-frame Graphics2D scaling work).
 *
 * Cache is bounded with simple LRU eviction so multiple bar heights / DPI
 * settings don't grow unbounded.
 */
object ScaledSpriteCache {

    private const val MAX_ENTRIES = 32

    private val cache: MutableMap<Key, BufferedImage> = Collections.synchronizedMap(
        object : LinkedHashMap<Key, BufferedImage>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, BufferedImage>): Boolean =
                size > MAX_ENTRIES
        }
    )

    fun get(key: AssetKey, frameW: Int, frameH: Int): BufferedImage {
        val w = frameW.coerceAtLeast(1)
        val h = frameH.coerceAtLeast(1)
        val cacheKey = Key(key, w, h)
        cache[cacheKey]?.let { return it }
        val scaled = buildScaled(key, w, h)
        cache[cacheKey] = scaled
        return scaled
    }

    fun clear() {
        cache.clear()
    }

    private fun buildScaled(key: AssetKey, frameW: Int, frameH: Int): BufferedImage {
        val source = AssetLoader.image(key)
        val sheetW = frameW * key.cols
        val sheetH = frameH * key.rows
        val out = BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
            g.drawImage(source, 0, 0, sheetW, sheetH, null)
        } finally {
            g.dispose()
        }
        return out
    }

    private data class Key(val asset: AssetKey, val frameW: Int, val frameH: Int)
}
