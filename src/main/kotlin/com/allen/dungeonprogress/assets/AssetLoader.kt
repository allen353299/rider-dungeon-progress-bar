package com.allen.dungeonprogress.assets

import com.intellij.openapi.diagnostic.thisLogger
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

object AssetLoader {
    private val cache = ConcurrentHashMap<AssetKey, BufferedImage>()
    private val warned = ConcurrentHashMap.newKeySet<AssetKey>()

    fun image(key: AssetKey): BufferedImage = cache.computeIfAbsent(key) { load(it) ?: placeholder() }

    private fun load(key: AssetKey): BufferedImage? {
        val path = "/assets/${key.filename}"
        val stream = AssetLoader::class.java.getResourceAsStream(path)
        if (stream == null) {
            if (warned.add(key)) thisLogger().warn("Missing dungeon asset: $path")
            return null
        }
        return stream.use { ImageIO.read(it) }
    }

    private fun placeholder(): BufferedImage {
        val path = "/assets/${AssetKey.PLACEHOLDER.filename}"
        val stream = AssetLoader::class.java.getResourceAsStream(path)
        return if (stream != null) {
            stream.use { ImageIO.read(it) }
        } else {
            val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
            val g = img.createGraphics()
            g.color = java.awt.Color.MAGENTA
            g.fillRect(0, 0, 16, 16)
            g.dispose()
            img
        }
    }
}
