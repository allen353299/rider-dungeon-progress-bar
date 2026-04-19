package com.allen.dungeonprogress.assets

enum class AssetKey(val filename: String, val cols: Int, val rows: Int) {
    BG("bg2.png", 1, 1),
    HERO_LEVEL1("tica_level1.png", 2, 2),
    HERO_LEVEL2("tica_level2.png", 2, 2),
    HERO_LEVEL3("tica_level3.png", 2, 2),
    BOSS("boss.png", 2, 2),
    SKELETON("skeleton.png", 4, 1),
    CHEST("treasure_chest.png", 4, 1),
    PLACEHOLDER("placeholder.png", 1, 1);

    val frameCount: Int get() = cols * rows
}
