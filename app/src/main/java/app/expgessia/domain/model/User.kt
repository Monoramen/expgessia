package app.expgessia.domain.model

data class User(
    val id: Int = 1,
    val name: String,
    val experience: Int,
    val level: Int,
    val money: Int,
    val lastLogin: Long?,
    val photoUri: String? = null,
    val characteristics: Map<Int, Int> = emptyMap() // characteristicId to value
) {


    val strength: Int get() = characteristics[1] ?: 0
    val perception: Int get() = characteristics[2] ?: 0
    val endurance: Int get() = characteristics[3] ?: 0
    val charisma: Int get() = characteristics[4] ?: 0
    val intelligence: Int get() = characteristics[5] ?: 0
    val agility: Int get() = characteristics[6] ?: 0
    val luck: Int get() = characteristics[7] ?: 0

    val expPercentage: Float
        get() = if (expToNextLevel() > 0) experience.toFloat() / expToNextLevel() else 0f

    fun expToNextLevel(): Int {
        return level * 100 // Простая формула для примера
    }
}
