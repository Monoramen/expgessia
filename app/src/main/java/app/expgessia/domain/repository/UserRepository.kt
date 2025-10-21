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


    // Операции со статами
    suspend fun increaseStrength(amount: Int = 1)
    suspend fun increaseIntelligence(amount: Int = 1)
    suspend fun increaseAgility(amount: Int = 1)

    suspend fun levelUpIfPossible()

    // Получение данных без Flow
    suspend fun getCurrentUserOnce(): User?
    suspend fun updateUserName(newName: String)
}