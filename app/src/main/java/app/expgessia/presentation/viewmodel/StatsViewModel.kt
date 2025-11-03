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

    val uiState: StateFlow<StatsUiState> = combine(
        dailyStatsRepository.getTotalTasksCompleted(),
        dailyStatsRepository.getTotalXpEarned(),
        dailyStatsRepository.getRecordXpDay(),
        dailyStatsRepository.getCurrentStreak(),
        dailyStatsRepository.getTodayXp(),
        dailyStatsRepository.getTimeInApp(),
        userRepository.getLastLogin()
    ) { results ->
        // results - это массив из 7 элементов
        val tasksCompleted = results[0] as Int
        val totalXp = results[1] as Int
        val recordXp = results[2] as Int
        val streak = results[3] as Int
        val xpToday = results[4] as Int
        val timeInApp = results[5] as Long
        val lastVisit = results[6] as Long?

        StatsUiState(
            totalTasksCompleted = tasksCompleted,
            totalXpEarned = totalXp,
            recordXpDay = recordXp,
            currentStreak = streak,
            xpToday = xpToday,
            timeInGameMs = timeInApp,
            lastVisit = lastVisit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}