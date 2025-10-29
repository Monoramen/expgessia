package app.expgessia.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppTimeTracker @Inject constructor(
    private val dailyStatsRepository: DailyStatsRepository,
    private val useRepository: UserRepository,
) : DefaultLifecycleObserver {
    private var sessionStartTime: Long = 0

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        sessionStartTime = System.currentTimeMillis()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        val sessionEndTime = System.currentTimeMillis()
        val timeInAppMs = sessionEndTime - sessionStartTime

        if (timeInAppMs > 1000) {
            GlobalScope.launch(Dispatchers.IO) {
                dailyStatsRepository.recordUserLogin(
                    System.currentTimeMillis(),
                    timeInAppMs = timeInAppMs
                )
                useRepository.updateLastLogin(System.currentTimeMillis())
            }
        }

    }
}