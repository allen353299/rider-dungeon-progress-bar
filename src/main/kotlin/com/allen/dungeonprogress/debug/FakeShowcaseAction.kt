package com.allen.dungeonprogress.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

/**
 * Opens a standalone showcase window with a 1000x100 progress bar and loops
 * through the full Tica Dungeon demo sequence forever. Designed for clean
 * screen recordings: the bigger bar means more pixel detail per source frame.
 *
 * Close the window to stop. Use any screen-capture tool to record the bar
 * region; the fixed window size makes cropping easy.
 */
class FakeShowcaseAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SwingUtilities.invokeLater {
            val bar = JProgressBar(0, 100).apply {
                preferredSize = Dimension(1000, 100)
                isStringPainted = false
            }
            val phaseLabel = JLabel(" ", SwingConstants.CENTER).apply {
                font = Font(Font.DIALOG, Font.PLAIN, 18)
                foreground = Color(170, 170, 170)
                border = BorderFactory.createEmptyBorder(24, 0, 0, 0)
            }

            val frame = JFrame("Tica Dungeon — Showcase (loop)")
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.contentPane = buildContent(bar, phaseLabel)
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true

            val driver = Thread({
                try {
                    while (frame.isDisplayable) {
                        runOneCycle(bar, phaseLabel)
                    }
                } catch (_: InterruptedException) {
                    // window closed
                }
            }, "TicaShowcaseDriver")
            driver.isDaemon = true
            driver.start()
        }
    }

    private fun buildContent(bar: JProgressBar, phase: JLabel): JPanel {
        val root = JPanel(BorderLayout())
        root.background = Color(43, 43, 43)
        root.border = BorderFactory.createEmptyBorder(40, 40, 40, 40)
        root.preferredSize = Dimension(1100, 320)

        val title = JLabel("Tica Dungeon Progress Bar", SwingConstants.CENTER)
        title.font = Font(Font.DIALOG, Font.BOLD, 28)
        title.foreground = Color(230, 230, 230)
        title.border = BorderFactory.createEmptyBorder(0, 0, 24, 0)
        root.add(title, BorderLayout.NORTH)

        val barWrap = JPanel()
        barWrap.background = Color(43, 43, 43)
        barWrap.add(bar)
        root.add(barWrap, BorderLayout.CENTER)

        root.add(phase, BorderLayout.SOUTH)
        return root
    }

    /** One full 16.5s sequence; same timings as FakeFullDemoAction. */
    private fun runOneCycle(bar: JProgressBar, phaseLabel: JLabel) {
        // 1. Indeterminate intro (2s)
        setOnEdt(bar, isIndeterminate = true, value = 0, phaseLabel, "Indeterminate: surrounded by skeletons")
        Thread.sleep(2000)

        // 2. Normal 0 -> 70% (3.15s)
        setOnEdt(bar, isIndeterminate = false, value = 0, phaseLabel, "Determinate: climbing 0 -> 70%")
        for (i in 0..70) {
            val v = i
            SwingUtilities.invokeLater { bar.value = v }
            Thread.sleep(45)
        }

        // 3. Boss battle 71 -> 100% (3.9s)
        SwingUtilities.invokeLater { phaseLabel.text = "Boss battle: 70 -> 100%" }
        for (i in 71..100) {
            val v = i
            SwingUtilities.invokeLater { bar.value = v }
            Thread.sleep(130)
        }

        // 4. Treasury hold (2s)
        SwingUtilities.invokeLater { phaseLabel.text = "Treasury: looting..." }
        Thread.sleep(2000)

        // 5. Indeterminate bridge (1s)
        setOnEdt(bar, isIndeterminate = true, value = 100, phaseLabel, "Bridge: summoning critical...")
        Thread.sleep(1000)

        // 6. Critical jumps (4.5s)
        setOnEdt(bar, isIndeterminate = false, value = 0, phaseLabel, "Critical jumps: 0 -> 25 -> 50 -> 75 -> 100")
        for (v in listOf(0, 25, 50, 75, 100)) {
            val pct = v
            SwingUtilities.invokeLater { bar.value = pct }
            Thread.sleep(800)
        }
        Thread.sleep(500)
    }

    private fun setOnEdt(
        bar: JProgressBar,
        isIndeterminate: Boolean,
        value: Int,
        phaseLabel: JLabel,
        phase: String,
    ) {
        SwingUtilities.invokeLater {
            bar.isIndeterminate = isIndeterminate
            bar.value = value
            phaseLabel.text = phase
        }
    }

}
