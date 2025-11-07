package app.expgessia.data.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.expgessia.data.entity.UserCharacteristicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCharacteristicDao {

    @Query("SELECT * FROM user_characteristics WHERE user_id = :userId")
    suspend fun getUserCharacteristics(userId: Int): List<UserCharacteristicEntity>

    @Query("SELECT * FROM user_characteristics WHERE user_id = :userId")
    fun getUserCharacteristicsStream(userId: Int): Flow<List<UserCharacteristicEntity>>

    @Query("SELECT * FROM user_characteristics WHERE user_id = :userId AND characteristic_id = :characteristicId")
    suspend fun getUserCharacteristic(userId: Int, characteristicId: Int): UserCharacteristicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCharacteristic(userCharacteristic: UserCharacteristicEntity)

    @Update
    suspend fun updateUserCharacteristic(userCharacteristic: UserCharacteristicEntity)

    @Query("DELETE FROM user_characteristics WHERE user_id = :userId")
    suspend fun deleteUserCharacteristics(userId: Int)

    @Query("DELETE FROM user_characteristics WHERE user_id = :userId AND characteristic_id = :characteristicId")
    suspend fun deleteUserCharacteristic(userId: Int, characteristicId: Int)
}