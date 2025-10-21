package app.expgessia.domain.repository

import app.expgessia.domain.model.Characteristic
import kotlinx.coroutines.flow.Flow

/**
 * Определяет методы для работы с характеристиками.
 * Использует доменные модели (Characteristic).
 */
interface CharacteristicRepository {

    /**
     * Получает поток всех характеристик для отображения в реальном времени.
     */
    fun getAllCharacteristics(): Flow<List<Characteristic>>

    /**
     * Получает конкретную характеристику по ее ID.
     */
    suspend fun getCharacteristicById(id: Int): Characteristic?

    // Здесь можно было бы добавить методы для обновления (update) или удаления (delete),
    // но для статичных характеристик пока достаточно чтения.
}
