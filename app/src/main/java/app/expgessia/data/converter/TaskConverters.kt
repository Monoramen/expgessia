package app.expgessia.data.converter

import androidx.room.TypeConverter
import app.expgessia.domain.model.RepeatMode


class TaskConverters {

    @TypeConverter
    fun fromRepeatMode(mode: RepeatMode): String {
        return mode.name
    }

    @TypeConverter
    fun toRepeatMode(modeString: String): RepeatMode {
        // 1. Поиск по всем доступным значениям
        return RepeatMode.entries.find { it.name == modeString }
        // 2. Если не найдено (или modeString неверно), возвращаем NONE
            ?: RepeatMode.NONE
    }
}
