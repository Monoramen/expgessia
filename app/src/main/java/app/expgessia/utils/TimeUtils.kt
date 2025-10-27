// utils/TimeUtils.kt
package app.expgessia.utils

import app.expgessia.data.entity.TaskEntity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

// Используем современное Time API (java.time) для точных расчетов
object TimeUtils {

    private val userZoneId: ZoneId = ZoneId.systemDefault()
    /**
     * Вычисляет временную метку (Long) начала дня (00:00:00) по указанному Timestamp.
     * Это обеспечивает корректный Primary Key для DailyStatsEntity.
     */
    fun calculateStartOfDay(timestamp: Long): Long {
        return Instant.ofEpochMilli(timestamp)
            .atZone(userZoneId)
            .truncatedTo(ChronoUnit.DAYS)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Вычисляет Long Timestamp следующего запланированного появления задачи.
     * Предполагается, что repeatDetails для Weekly содержит числа 1 (Пн) - 7 (Вс).
     */
    fun calculateNextScheduledDate(task: TaskEntity, completionTimestamp: Long): Long {
        val now = Instant.ofEpochMilli(completionTimestamp).atZone(userZoneId)
        var nextDate: ZonedDateTime = now

        when (task.repeatMode.uppercase(Locale.ROOT)) {
            "DAILY" -> {
                // Завтра в 00:00:00
                nextDate = now.plusDays(1).truncatedTo(ChronoUnit.DAYS)
            }
            "WEEKLY" -> {
                // repeatDetails: "1,3,5" (Пн, Ср, Пт)
                val currentDayOfWeek = now.dayOfWeek.value
                val targetDays = task.repeatDetails
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?.filter { it in 1..7 }
                    ?: listOf(currentDayOfWeek)

                var daysToAdd = targetDays.firstOrNull { it > currentDayOfWeek }

                // Если нет дней позже в эту неделю, берем первый день следующей недели
                if (daysToAdd == null) {
                    val firstDayOfNextWeek = targetDays.minOrNull() ?: (currentDayOfWeek + 7)
                    daysToAdd = 7 - currentDayOfWeek + (firstDayOfNextWeek % 7)
                } else {
                    daysToAdd -= currentDayOfWeek
                }

                nextDate = now.plusDays(daysToAdd.toLong()).truncatedTo(ChronoUnit.DAYS)
            }
            "MONTHLY" -> {
                // repeatDetails: "15" (15-е число месяца)
                val targetDayOfMonth = task.repeatDetails?.toIntOrNull() ?: now.dayOfMonth

                nextDate = now.plusMonths(1)
                    .withDayOfMonth(minOf(targetDayOfMonth, now.plusMonths(1).toLocalDate().lengthOfMonth()))
                    .truncatedTo(ChronoUnit.DAYS)
            }
            else -> return 0L // NONE
        }

        return nextDate.toInstant().toEpochMilli()
    }
}