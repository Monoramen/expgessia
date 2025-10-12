// domain/model/User.kt
package app.expgessia.domain.model

import java.time.LocalDateTime
import java.util.Date

data class User(
    val id: Int = 0,
    val name: String,
    val experience: Int,
    val level: Int,
    val score: Int,
    val mana: Int,
    val strength: Int,
    val intelligence: Int,
    val agility: Int,
    val lastLogin: Date?, // Последний вход
    val photoUri: String? = null // Добавим поле для фото
) {
    // Вычисляемые свойства для UI
    val expPercentage: Float
        get() = if (expToNextLevel() > 0) experience.toFloat() / expToNextLevel() else 0f

    fun expToNextLevel(): Int {
        // Формула для расчета EXP до следующего уровня
        return level * 100 // Простая формула для примера
    }
}
