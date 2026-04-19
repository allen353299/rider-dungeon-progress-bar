package com.allen.dungeonprogress.model

data class CriticalHitEvent(val startMs: Long, val durationMs: Long = 900) {
    fun isActive(nowMs: Long): Boolean = nowMs - startMs in 0..durationMs
    fun progress(nowMs: Long): Double = ((nowMs - startMs).toDouble() / durationMs).coerceIn(0.0, 1.0)
}
