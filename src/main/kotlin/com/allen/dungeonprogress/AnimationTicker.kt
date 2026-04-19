package com.allen.dungeonprogress

import java.util.Collections
import java.util.WeakHashMap
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * Repaints all registered progress bars at ~30fps.
 * Auto-stops when no bars are registered to keep idle cost at zero.
 */
object AnimationTicker {
    private val bars: MutableSet<JProgressBar> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

    private val timer: Timer = Timer(33) {
        SwingUtilities.invokeLater {
            val snapshot: List<JProgressBar>
            synchronized(bars) { snapshot = bars.toList() }
            if (snapshot.isEmpty()) {
                stop()
                return@invokeLater
            }
            for (bar in snapshot) if (bar.isShowing) bar.repaint()
        }
    }.apply { isRepeats = true }

    fun register(bar: JProgressBar) {
        bars.add(bar)
        if (!timer.isRunning) timer.start()
    }

    fun unregister(bar: JProgressBar) {
        bars.remove(bar)
    }

    private fun stop() {
        if (timer.isRunning) timer.stop()
    }
}
