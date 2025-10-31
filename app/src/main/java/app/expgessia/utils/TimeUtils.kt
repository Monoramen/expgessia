// utils/TimeUtils.kt
package app.expgessia.utils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.expgessia.R
import app.expgessia.data.entity.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Используем современное Time API (java.time) для точных расчетов
object TimeUtils {

    private val userZoneId: ZoneId = ZoneId.systemDefault()

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) // Формат для даты
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

    fun formatLastVisit(timestamp: Long): String {
        if (timestamp == 0L) return "Нет данных"
        return Instant.ofEpochMilli(timestamp)
            .atZone(userZoneId)
            .format(dateFormatter)
    }


    data class TimePart(
        @PluralsRes val resourceId: Int, // R.plurals.time_hours, R.plurals.time_minutes и т.д.
        val count: Long // Количество (1, 2, 5 и т.д.)
    )

    /**
     * Возвращает список TimePart вместо готовой строки.
     * Эту функцию можно вызывать из ViewModel или другого не-Compose кода.
     */
    fun formatTimeData(milliseconds: Long): List<TimePart> {
        if (milliseconds <= 0) {
            return listOf(TimePart(R.plurals.time_seconds, 0L))
        }

        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        val parts = mutableListOf<TimePart>()

        // 1. Часы
        if (hours > 0) {
            parts.add(TimePart(R.plurals.time_hours, hours))
        }

        // 2. Минуты
        if (minutes > 0) {
            parts.add(TimePart(R.plurals.time_minutes, minutes))
        }

        // 3. Секунды
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(TimePart(R.plurals.time_seconds, seconds))
        }

        // Обработка 0 < milliseconds < 1000
        if (parts.isEmpty() && milliseconds > 0) {
            parts.add(TimePart(R.plurals.time_seconds, 0L))
        }

        return parts
    }


    @Composable
    fun formatTime(milliseconds: Long): String {
        // 1. Получаем данные (этот вызов не требует Context)
        val timeParts = formatTimeData(milliseconds)

        // 2. Преобразуем данные в локализованные строки внутри Composable
        val formattedParts = timeParts.map { part ->
            // Здесь используется pluralStringResource:
            // - part.count.toInt() выбирает правильную форму (one, few, many, other)
            // - part.count вставляется вместо %d в строку
            pluralStringResource(id = part.resourceId, count = part.count.toInt(), formatArgs = arrayOf(part.count))
        }

        return formattedParts.joinToString(" ")
    }




    /**
     * Проверяет, запланирована ли повторяющаяся задача на указанную дату.
     * Использует текущую системную ZoneId.
     */
    fun isTaskScheduledOnDate(task: TaskEntity, date: LocalDate): Boolean {
        // 1. NON-Repeating Tasks:
        if (task.repeatMode.uppercase(Locale.ROOT) == "NONE") {
            // Проверяем, совпадает ли дата запланированного выполнения (если есть) с date
            return task.scheduledFor?.let { timestamp ->
                val scheduledDate = Instant.ofEpochMilli(timestamp).atZone(userZoneId).toLocalDate()
                scheduledDate.isEqual(date)
            } ?: false
        }

        // 2. Daily Tasks:
        if (task.repeatMode.uppercase(Locale.ROOT) == "DAILY") {
            // Ежедневные задачи запланированы на любой день
            return true
        }

        // 3. Weekly Tasks:
        if (task.repeatMode.uppercase(Locale.ROOT) == "WEEKLY") {
            // repeatDetails: "1,3,5" (Пн, Ср, Пт) -> DayOfWeek.value (1-7)
            val targetDays = task.repeatDetails
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.filter { it in 1..7 }
                ?: return false // Если нет деталей, то не запланировано

            return targetDays.contains(date.dayOfWeek.value)
        }

        // 4. Monthly Tasks:
        if (task.repeatMode.uppercase(Locale.ROOT) == "MONTHLY") {
            // repeatDetails: "15" (15-е число месяца)
            val targetDayOfMonth = task.repeatDetails?.toIntOrNull() ?: return false

            // ВАЖНО: Учитываем, что, если указано "31", а в месяце 30 дней,
            // то задача должна быть запланирована на последний день месяца.
            val maxDayInMonth = date.lengthOfMonth()
            val scheduledDay = minOf(targetDayOfMonth, maxDayInMonth)

            return date.dayOfMonth == scheduledDay
        }

        // По умолчанию: NONE или неизвестный режим
        return false
    }

}