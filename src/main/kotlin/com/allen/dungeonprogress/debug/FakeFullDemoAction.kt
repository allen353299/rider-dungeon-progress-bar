package com.allen.dungeonprogress.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

/**
 * Full demo sequence designed for ~15 second marketing GIFs:
 * 1. Indeterminate (2.0s) — hero + 3 skeletons orbit
 * 2. Normal 0 -> 70% (3.0s) — Cellar / Ruins / Core hero outfits
 * 3. Boss battle 70 -> 100% (4.0s) — the 4-frame boss death is tied to progress
 * 4. Treasury hold at 100% (2.0s) — chest opens with "LOOT FOUND"
 * 5. Indeterminate bridge (1.0s) — smooth transition, no blank frame
 * 6. Critical (3.5s) — 4 jumps of +25% triggering the "CRITICAL!" pop-up
 */
class FakeFullDemoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val task = object : Task.Backgroundable(project, "Tica Dungeon: full demo", true) {
            override fun run(indicator: ProgressIndicator) {
                // 1. Indeterminate intro
                indicator.isIndeterminate = true
                indicator.text = "Surrounded by skeletons..."
                sleepOrCancel(indicator, 2000)
                if (indicator.isCanceled) return

                // 2. Normal 0 -> 70% (45ms / step, 70 steps = 3.15s)
                indicator.isIndeterminate = false
                for (i in 0..70) {
                    if (indicator.isCanceled) return
                    indicator.fraction = i / 100.0
                    indicator.text = stageLabel(i)
                    Thread.sleep(45)
                }

                // 3. Boss battle 70 -> 100% (130ms / step, 30 steps = 3.9s)
                for (i in 71..100) {
                    if (indicator.isCanceled) return
                    indicator.fraction = i / 100.0
                    indicator.text = "BOSS BATTLE: ${i}%"
                    Thread.sleep(130)
                }

                // 4. Treasury hold
                indicator.text = "Looting..."
                sleepOrCancel(indicator, 2000)
                if (indicator.isCanceled) return

                // 5. Indeterminate bridge
                indicator.isIndeterminate = true
                indicator.text = "Summoning CRITICAL!..."
                sleepOrCancel(indicator, 1000)
                if (indicator.isCanceled) return

                // 6. Critical: jump 0 -> 25 -> 50 -> 75 -> 100, each step triggers CRITICAL!
                indicator.isIndeterminate = false
                indicator.fraction = 0.0
                var f = 0
                while (f <= 100) {
                    if (indicator.isCanceled) return
                    indicator.fraction = f / 100.0
                    indicator.text = "CRITICAL! ${f}%"
                    Thread.sleep(800)
                    f += 25
                }
                // tiny hold so the last CRITICAL! has time to bounce
                sleepOrCancel(indicator, 500)
            }
        }
        ProgressManager.getInstance().run(task)
    }

    private fun sleepOrCancel(indicator: ProgressIndicator, ms: Long) {
        val step = 50L
        var remaining = ms
        while (remaining > 0 && !indicator.isCanceled) {
            val slice = if (remaining < step) remaining else step
            Thread.sleep(slice)
            remaining -= slice
        }
    }

    private fun stageLabel(percent: Int): String = when {
        percent < 26 -> "Cellar: depth ${percent}m"
        percent < 61 -> "Ruins: depth ${percent}m"
        else -> "Core: depth ${percent}m"
    }
}
