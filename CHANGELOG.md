# Changelog

## 0.1.3 — 2026-04-19

- Add demo GIFs (main run, indeterminate, critical hit) and wire them into the
  README and the Marketplace plugin description (via raw.githubusercontent.com
  URLs).
- Upgrade `org.jetbrains.intellij.platform` 2.1.0 → 2.7.0. 2.1.0 couldn't parse
  Rider 2025+ `product-info.json` and bailed with `Index: 1, Size: 1` when the
  sandbox tried to launch; 2.7.0 handles the newer format.
- Verified end-to-end in both Rider 2024.2 and 2025.1 sandboxes: log shows
  `DungeonProgressBarUI installed via UIManager` + `Dungeon AWT hook installed`
  within ~2 seconds of startup. 2026.1 previously confirmed on a real install.

## 0.1.2 — 2026-04-19

- Fix: on Rider 2026.1 the plugin loaded but the progress bar didn't change.
  Root cause: `ProjectActivity` only fires after a project opens, so on the
  welcome screen the AWT hook was never installed and new JProgressBars stayed
  with the default UI. Now the full install chain (UIManager + AWT hook +
  force-upgrade existing bars) also runs from `LafManagerListener`, which
  fires during initial LAF setup. Verified in a 2024.2 sandbox immediately
  after IDE startup.

## 0.1.1 — 2026-04-19

- Fix: plugin failed to activate on Rider 2026.1 because the
  `ApplicationInitializedListener.execute` signature was tightened. Switched the
  startup hook to `ProjectActivity` + `postStartupActivity`, which is accepted
  unchanged across 2024.1 through 2026.x. Icon now also shows correctly (switched
  from PNG to SVG, which is the only format JetBrains renders for plugin icons).

## 0.1.0 — 2026-04-19

Initial release.

- Replaces every `JProgressBar` in Rider with a pixel-art dungeon-crawler animation.
- Four stages (Cellar / Ruins / Core / Treasury) with a different hero outfit per stage.
- Boss battle from 70% onward; 4-frame death sequence tied to progress.
- Treasure chest opens at 100% with "LOOT FOUND" plate.
- Indeterminate mode shows the hero surrounded by three orbiting skeletons.
- Critical-hit "CRITICAL!" pop-up when progress jumps ≥20% in one update.
- LRU sprite cache so per-frame paint is a pure blit (no scaling cost).
- AWT hook auto-applies the dungeon UI to existing and newly created progress bars.
