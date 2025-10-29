package app.expgessia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.model.StatsUiState
import app.expgessia.domain.repository.DailyStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel // ⭐️ ДОБАВИТЬ: Указывает Hilt, что нужно создать провайдер для этого ViewModel
class StatsViewModel @Inject constructor( // ⭐️ ДОБАВИТЬ: Указывает Hilt, как создавать экземпляр
    private val dailyStatsRepository: DailyStatsRepository,
) : ViewModel() {

    // ⭐️ ИСПРАВЛЕНО: Теперь uiState является прямым членом класса StatsViewModel
    val uiState: StateFlow<StatsUiState> = combine(
        dailyStatsRepository.getTotalTasksCompleted(),
        dailyStatsRepository.getTotalXpEarned(),
        dailyStatsRepository.getRecordXpDay(),
        dailyStatsRepository.getCurrentStreak(),
        dailyStatsRepository.getTodayXp()
        // 💡 Здесь можно добавить Flow для lastVisit и timeInGameMs, если они будут в репозитории
    ) { tasksCompleted, totalXp, recordXp, streak, xpToday ->
        // Компонуем все в единый объект StatsUiState
        StatsUiState(
            totalTasksCompleted = tasksCompleted,
            totalXpEarned = totalXp,
            recordXpDay = recordXp,
            currentStreak = streak,
            xpToday = xpToday
            // status, lastVisit, timeInGameMs остаются дефолтными (0L, "НЕТ ДАННЫХ" или 0L)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState() // Начальное пустое состояние
    )
}