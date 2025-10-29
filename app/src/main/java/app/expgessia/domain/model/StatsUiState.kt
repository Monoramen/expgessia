package app.expgessia.domain.model

data class StatsUiState(
    val totalTasksCompleted: Int = 0,
    val totalXpEarned: Int = 0,
    val recordXpDay: Int? = null, // Int? потому что может быть null, если нет записей
    val currentStreak: Int = 0,

    // Допустим, вы добавите эти данные позже:
    val xpToday: Int = 0,
    val lastVisit: Long = 0L,
    val timeInGameMs: Long = 0L,
    val status: String = "НЕТ ДАННЫХ"
)
