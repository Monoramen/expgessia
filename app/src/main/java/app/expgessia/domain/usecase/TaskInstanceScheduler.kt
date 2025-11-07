package app.expgessia.domain.usecase

import android.util.Log
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.utils.TimeUtils
import jakarta.inject.Inject
import java.time.LocalDate


class TaskScheduler @Inject constructor(
    private val taskCompletionRepository: TaskCompletionRepository,
) {
    private var lastProcessedDate: LocalDate? = null

    suspend fun ensureInstancesForDate(date: LocalDate) {
        Log.d("TaskScheduler", "🎯 Creating instances for single date: $date")
        if (lastProcessedDate == date) {
            return
        }


        val dateMillis = TimeUtils.localDateToStartOfDayMillis(date)
        taskCompletionRepository.ensureTaskInstancesForDate(dateMillis)
        lastProcessedDate = date
    }

    suspend fun ensureInstancesForDateRange(startDate: LocalDate, endDate: LocalDate) {
        Log.d("TaskScheduler", "📅 Creating instances for range: $startDate - $endDate")

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            ensureInstancesForDate(currentDate)
            currentDate = currentDate.plusDays(1)
        }
    }
}