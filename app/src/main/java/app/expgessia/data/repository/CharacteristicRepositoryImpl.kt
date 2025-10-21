package app.expgessia.data.repository

import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.mapper.toDomain
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.repository.CharacteristicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Реализация репозитория характеристик, отвечающая за взаимодействие с Room.
 * Использует мапперы для преобразования Entity в Domain Model.
 */
class CharacteristicRepositoryImpl @Inject constructor(
    private val characteristicDao: CharacteristicDao
) : CharacteristicRepository {

    override fun getAllCharacteristics(): Flow<List<Characteristic>> {

        return characteristicDao.getAllCharacteristics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCharacteristicById(id: Int): Characteristic? {
        // Получаем Entity, если она существует, и преобразуем в доменную модель.
        return characteristicDao.getCharacteristicById(id)?.toDomain()
    }
}
