package app.expgessia.domain.repository

import app.expgessia.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser()
    suspend fun addExperience(amount: Int)
    suspend fun increaseStrength(amount: Int = 1)
    suspend fun increaseIntelligence(amount: Int = 1)
    suspend fun increaseAgility(amount: Int = 1)
    suspend fun levelUpIfPossible()
    suspend fun getCurrentUserOnce(): User?
    suspend fun updateUserName(newName: String)
}