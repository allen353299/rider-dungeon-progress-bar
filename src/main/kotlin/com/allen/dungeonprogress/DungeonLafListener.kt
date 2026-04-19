package com.allen.dungeonprogress

import com.allen.dungeonprogress.assets.ScaledSpriteCache
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener

/** Re-installs the dungeon UI after a LAF change resets UIManager defaults. */
class DungeonLafListener : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        // LafManagerListener fires during initial LAF setup on app startup (before any
        // project opens / welcome screen), so we run the full install chain here too.
        // All three helpers are idempotent, so it's safe if ProjectActivity fires later.
        DungeonStartupListener.installDungeonUi()
        DungeonStartupListener.installAwtHook()
        DungeonStartupListener.forceUpgradeExistingBars()
        // DPI / theme change can imply different bar dimensions; drop the scaled cache
        // so it re-fills on demand at the new sizes.
        ScaledSpriteCache.clear()
    }
}
