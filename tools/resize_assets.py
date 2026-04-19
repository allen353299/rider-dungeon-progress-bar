"""Downsample source assets to plugin-friendly sizes using nearest-neighbour
resampling (preserves pixel-art crispness, no aspect distortion), then remove
the white background by flood-filling from the 4 corners.

Corner-connected white regions become transparent; interior whites (tica belly,
skeleton bones, sparkles) are preserved.
"""
from __future__ import annotations

import os
import numpy as np
from PIL import Image
from scipy.ndimage import label

_REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = os.path.join(_REPO, "assets-source")
TARGET = os.path.join(_REPO, "src", "main", "resources", "assets")

PLAN: dict[str, tuple[int, int]] = {
    "bg2.png":            (800, 246),
    "tica_level1.png":    (512, 512),
    "tica_level2.png":    (512, 512),
    "tica_level3.png":    (512, 512),
    "boss.png":           (512, 512),
    "skeleton.png":       (768, 432),
    "treature_chest.png": (768, 432),
}
RENAME = {"treature_chest.png": "treasure_chest.png"}

# Files that should keep their opaque background (none for now — bg2 also gets
# transparent corners which is harmless because the bar always fills the frame).
KEEP_OPAQUE: set[str] = set()

# Near-white tolerance. Treat pixels with all channels >= 255 - TOL as "white".
TOL = 8


def remove_corner_connected_white(img: Image.Image) -> Image.Image:
    """Flood-fill near-white regions that touch any of the 4 corners, replacing
    them with fully transparent pixels."""
    img = img.convert("RGBA")
    arr = np.array(img)
    h, w = arr.shape[:2]
    r, g, b, _ = arr[..., 0], arr[..., 1], arr[..., 2], arr[..., 3]
    white = (r >= 255 - TOL) & (g >= 255 - TOL) & (b >= 255 - TOL)
    if not white.any():
        return img

    labeled, _ = label(white)  # 4-connectivity by default
    corner_labels = {
        labeled[0, 0],
        labeled[0, w - 1],
        labeled[h - 1, 0],
        labeled[h - 1, w - 1],
    }
    corner_labels.discard(0)
    if not corner_labels:
        return img

    bg_mask = np.isin(labeled, list(corner_labels))
    arr[bg_mask] = [0, 0, 0, 0]
    return Image.fromarray(arr, "RGBA")


def main() -> None:
    os.makedirs(TARGET, exist_ok=True)
    total_before = 0
    total_after = 0
    for src_name, (w, h) in PLAN.items():
        src_path = os.path.join(SOURCE, src_name)
        if not os.path.exists(src_path):
            print(f"SKIP missing: {src_path}")
            continue
        total_before += os.path.getsize(src_path)
        img = Image.open(src_path)
        out = img.resize((w, h), Image.NEAREST)
        if src_name not in KEEP_OPAQUE:
            out = remove_corner_connected_white(out)
        out_name = RENAME.get(src_name, src_name)
        out_path = os.path.join(TARGET, out_name)
        out.save(out_path, optimize=True)
        total_after += os.path.getsize(out_path)
        print(f"{src_name} {img.size} -> {out_name} ({w}x{h})")
    print(f"\ntotal: {total_before/1024/1024:.1f} MB -> {total_after/1024:.0f} KB")


if __name__ == "__main__":
    main()
