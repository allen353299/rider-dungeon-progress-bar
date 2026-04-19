package com.allen.dungeonprogress

import com.allen.dungeonprogress.model.DungeonStage

object StageResolver {
    fun resolve(percent: Double): DungeonStage = when {
        percent >= 96.0 -> DungeonStage.TREASURY
        percent >= 61.0 -> DungeonStage.CORE
        percent >= 26.0 -> DungeonStage.RUINS
        else -> DungeonStage.CELLAR
    }
}
