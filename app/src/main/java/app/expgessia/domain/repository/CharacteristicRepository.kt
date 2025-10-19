package app.expgessia.domain.repository

import app.expgessia.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Основные CRUD операции
    fun getCurrentUser(): Flow<User?>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser()

    // Операции с характеристиками
    suspend fun addExperience(amount: Int)
    suspend fun addScore(amount: Int)
    suspend fun updateMana(value: Int)
    suspend fun updateHealth(value: Int) // Добавим здоровье позже

    // Операции со статами
    suspend fun increaseStrength(amount: Int = 1)
    suspend fun increaseIntelligence(amount: Int = 1)
    suspend fun increaseAgility(amount: Int = 1)

    // Восстановление ресурсов
    suspend fun restoreMana(amount: Int = 10)

    // Проверки и утилиты
    suspend fun hasEnoughMana(required: Int): Boolean
    suspend fun levelUpIfPossible()

    // Получение данных без Flow
    suspend fun getCurrentUserOnce(): User?
    suspend fun updateUserName(newName: String)
}