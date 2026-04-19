package com.allen.dungeonprogress.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

class FakeIndeterminateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val task = object : Task.Backgroundable(project, "Dungeon crawl: indeterminate", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Surrounded by skeletons..."
                val end = System.currentTimeMillis() + 10_000
                while (System.currentTimeMillis() < end) {
                    if (indicator.isCanceled) return
                    Thread.sleep(100)
                }
            }
        }
        ProgressManager.getInstance().run(task)
    }
}
