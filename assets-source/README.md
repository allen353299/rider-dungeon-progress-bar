# Source artwork

High-resolution PNGs that the build pipeline downsamples + de-backgrounds into
`src/main/resources/assets/`. Don't edit the resized output by hand — change
these and re-run `py tools/resize_assets.py`.

| File | Size | Purpose |
|---|---|---|
| `bg2.png` | 2816×868 | Dungeon corridor background (1×1) |
| `tica_level1.png` | 2048×2048 | Cellar hero, 4 frames in 2×2 grid |
| `tica_level2.png` | 2048×2048 | Ruins hero, 4 frames in 2×2 grid |
| `tica_level3.png` | 2048×2048 | Core/Treasury hero, 4 frames in 2×2 grid |
| `boss.png` | 2048×2048 | Giant red bug, 4 death frames in 2×2 grid |
| `skeleton.png` | 2816×1536 | Skeleton, 4 frames in 1×4 horizontal strip |
| `treature_chest.png` | 2816×1536 | Chest open sequence, 4 frames in 1×4 strip |
| `bg.png`, `bg3.png` | varies | Unused alternate backgrounds (kept for reference) |

Frame ordering in 2×2 grids is **row-major**:
`0=top-left, 1=top-right, 2=bottom-left, 3=bottom-right`.

## Re-running the pipeline

```
py tools/resize_assets.py
```

Output goes to `src/main/resources/assets/`; commit both source and output.
