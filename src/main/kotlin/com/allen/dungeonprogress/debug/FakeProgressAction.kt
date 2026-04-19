package com.allen.dungeonprogress.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

class FakeProgressAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val task = object : Task.Backgroundable(project, "Dungeon crawl: 0 to 100", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                for (i in 0..100) {
                    if (indicator.isCanceled) return
                    indicator.fraction = i / 100.0
                    indicator.text = when {
                        i < 26 -> "Cellar: depth ${i}m"
                        i < 61 -> "Ruins: depth ${i}m"
                        i < 70 -> "Core: depth ${i}m"
                        i < 100 -> "BOSS BATTLE: ${i}%"
                        else -> "Looting..."
                    }
                    // Slow down across the boss fight so the 4-frame death sequence is visible.
                    Thread.sleep(if (i in 70..100) 250L else 80L)
                }
                Thread.sleep(3500)  // hold so user can see the chest open fully
            }
        }
        ProgressManager.getInstance().run(task)
    }
}
