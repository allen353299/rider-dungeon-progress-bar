package com.allen.dungeonprogress

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.awt.AWTEvent
import java.awt.Component
import java.awt.Container
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ContainerEvent
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Installs the dungeon ProgressBarUI, forces existing progress bars to upgrade,
 * and registers an AWT hook that upgrades future bars as they are created.
 *
 * Fires on first project open (ProjectActivity), which is available from
 * 2024.1+ all the way through 2026.x — unlike `ApplicationInitializedListener`
 * whose `execute` signature was tightened in 2026.1 and started rejecting our override.
 */
class DungeonStartupListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        SwingUtilities.invokeLater {
            installDungeonUi()
            forceUpgradeExistingBars()
            installAwtHook()
        }
    }

    companion object {
        @Volatile private var awtHookInstalled = false
        @Volatile private var uiInstalled = false

        fun installDungeonUi() {
            val name = DungeonProgressBarUI::class.java.name
            UIManager.put("ProgressBarUI", name)
            UIManager.getDefaults()[name] = DungeonProgressBarUI::class.java
            if (!uiInstalled) {
                uiInstalled = true
                thisLogger().info("DungeonProgressBarUI installed via UIManager")
            }
        }

        fun forceUpgradeExistingBars() {
            var count = 0
            for (window in Window.getWindows()) count += upgrade(window)
            if (count > 0) thisLogger().info("Force-upgraded $count existing JProgressBar(s) to Dungeon")
        }

        fun installAwtHook() {
            if (awtHookInstalled) return
            awtHookInstalled = true
            Toolkit.getDefaultToolkit().addAWTEventListener({ ev ->
                if (ev is ContainerEvent && ev.id == ContainerEvent.COMPONENT_ADDED) {
                    upgrade(ev.child)
                }
            }, AWTEvent.CONTAINER_EVENT_MASK)
            thisLogger().info("Dungeon AWT hook installed")
        }

        private fun upgrade(component: Component): Int {
            var upgraded = 0
            if (component is JProgressBar) {
                val current = component.ui
                if (current !is DungeonProgressBarUI) {
                    component.setUI(DungeonProgressBarUI())
                    upgraded++
                }
            }
            if (component is Container) {
                for (child in component.components) upgraded += upgrade(child)
            }
            return upgraded
        }
    }
}
