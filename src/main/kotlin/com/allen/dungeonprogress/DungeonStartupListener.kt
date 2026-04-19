package com.allen.dungeonprogress

import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.CoroutineScope
import java.awt.AWTEvent
import java.awt.Component
import java.awt.Container
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ContainerEvent
import javax.swing.JComponent
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.UIManager

/** Installs the dungeon ProgressBarUI and force-applies it to every existing + future JProgressBar. */
class DungeonStartupListener : ApplicationInitializedListener {
    override suspend fun execute(asyncScope: CoroutineScope) {
        SwingUtilities.invokeLater {
            installDungeonUi()
            forceUpgradeExistingBars()
            installAwtHook()
        }
    }

    companion object {
        private var awtHookInstalled = false

        fun installDungeonUi() {
            val name = DungeonProgressBarUI::class.java.name
            UIManager.put("ProgressBarUI", name)
            UIManager.getDefaults()[name] = DungeonProgressBarUI::class.java
            thisLogger().info("DungeonProgressBarUI installed via UIManager")
        }

        fun forceUpgradeExistingBars() {
            for (window in Window.getWindows()) upgrade(window)
        }

        private fun installAwtHook() {
            if (awtHookInstalled) return
            awtHookInstalled = true
            Toolkit.getDefaultToolkit().addAWTEventListener({ ev ->
                if (ev is ContainerEvent && ev.id == ContainerEvent.COMPONENT_ADDED) {
                    upgrade(ev.child)
                }
            }, AWTEvent.CONTAINER_EVENT_MASK)
            thisLogger().info("Dungeon AWT hook installed")
        }

        private fun upgrade(component: Component) {
            if (component is JProgressBar) {
                val current = component.ui
                if (current !is DungeonProgressBarUI) {
                    component.setUI(DungeonProgressBarUI())
                    thisLogger().info("Upgraded JProgressBar UI -> Dungeon (was ${current?.javaClass?.simpleName})")
                }
            }
            if (component is Container) {
                for (child in component.components) upgrade(child)
            }
        }
    }
}
