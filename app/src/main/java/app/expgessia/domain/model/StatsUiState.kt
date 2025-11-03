package app.expgessia.domain.model

import app.expgessia.R



data class StatsUiState(
    val totalTasksCompleted: Int? = 0,
    val totalXpEarned: Int? = 0,
    val recordXpDay: Int? = 0,
    val currentStreak: Int? = 0,
    val xpToday: Int? = 0,
    val timeInGameMs: Long? = 0L,
    val lastVisit: Long? = null,
    val status: Int = R.string.value_status
) {
    // Вычисляемые свойства для безопасного доступа
    val safeTotalTasksCompleted: Int get() = totalTasksCompleted ?: 0
    val safeTotalXpEarned: Int get() = totalXpEarned ?: 0
    val safeRecordXpDay: Int get() = recordXpDay ?: 0
    val safeCurrentStreak: Int get() = currentStreak ?: 0
    val safeXpToday: Int get() = xpToday ?: 0
    val safeTimeInGameMs: Long get() = timeInGameMs ?: 0L
    val safeLastVisit: Long get() = lastVisit ?: 0L
}