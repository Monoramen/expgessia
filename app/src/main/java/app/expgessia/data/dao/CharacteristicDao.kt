package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Query
import app.expgessia.data.entity.CharacteristicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacteristicDao {

    /**
     * Возвращает все характеристики из базы данных.
     * Использует Flow для получения обновлений в реальном времени.
     */
    @Query("SELECT * FROM characteristics ORDER BY id ASC")
    fun getAllCharacteristics(): Flow<List<CharacteristicEntity>>

    /**
     * Возвращает конкретную характеристику по ее ID.
     */
    @Query("SELECT * FROM characteristics WHERE id = :characteristicId LIMIT 1")
    suspend fun getCharacteristicById(characteristicId: Int): CharacteristicEntity?


    @Query("SELECT icon_res_name FROM characteristics WHERE id = :id")
    suspend fun getIconResNameById(id: Int): String?
}
