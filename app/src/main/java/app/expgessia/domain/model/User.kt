// domain/model/User.kt
package app.expgessia.domain.model

data class User(
    val id: Int = 1,
    val name: String,
    //val sex: String,
    val experience: Int,
    val level: Int,
    val money: Int,
    val strength: Int,
    val perception: Int,
    val endurance: Int,
    val charisma: Int,
    val intelligence: Int,
    val agility: Int,
    val luck: Int,
    val lastLogin: Long?, // Последний вход
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
