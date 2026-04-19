# Changelog

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
