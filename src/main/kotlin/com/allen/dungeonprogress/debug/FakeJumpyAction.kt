package com.allen.dungeonprogress.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

class FakeJumpyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val task = object : Task.Backgroundable(project, "Dungeon crawl: jumpy (CRITICAL!)", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                var f = 0
                while (f <= 100) {
                    if (indicator.isCanceled) return
                    indicator.fraction = f / 100.0
                    indicator.text = "Critical step $f%"
                    Thread.sleep(1500)
                    f += 25
                }
                Thread.sleep(2500)
            }
        }
        ProgressManager.getInstance().run(task)
    }
}
