package app.expgessia.presentation.viewmodel

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.R
import app.expgessia.domain.model.StatsUiState
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dailyStatsRepository: DailyStatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // ✅ ИСПРАВЛЕНО: Для более чем 6 Flow нужно использовать перегрузку, которая принимает Iterable<Flow>
    // и возвращает Flow<Array<Any?>>, где лямбда принимает один аргумент (Array).
    val uiState: StateFlow<StatsUiState> = combine(
        listOf(
            dailyStatsRepository.getTotalTasksCompleted(), // 0: Int?
            dailyStatsRepository.getTotalXpEarned(),      // 1: Int?
            dailyStatsRepository.getRecordXpDay(),        // 2: Int?
            dailyStatsRepository.getCurrentStreak(),      // 3: Int?
            dailyStatsRepository.getTodayXp(),            // 4: Int?
            dailyStatsRepository.getTimeInApp(),          // 5: Long?
            userRepository.getLastLogin()                 // 6: Long?
        )
    ) { values -> // values - это Array<Any?>, содержащий все результаты

        // Безопасное приведение типов (Cast) и использование оператора Элвиса для дефолтных значений
        val tasksCompleted = values[0] as Int? ?: 0
        val totalXp = values[1] as Int? ?: 0
        val recordXp = values[2] as Int? ?: 0
        val streak = values[3] as Int? ?: 0
        val xpToday = values[4] as Int? ?: 0
        val timeInGameMs = values[5] as Long? ?: 0L
        val lastVisit = values[6] as Long? ?: 0L

        // Компонуем все в единый объект StatsUiState
        StatsUiState(
            totalTasksCompleted = tasksCompleted,
            totalXpEarned = totalXp,
            recordXpDay = recordXp,
            currentStreak = streak,
            xpToday = xpToday,

            // Безопасное разворачивание Long? к Long с дефолтом 0L
            timeInGameMs = timeInGameMs,
            lastVisit = lastVisit,

            // Статус устанавливается вручную, как и раньше
            status = R.string.value_status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
