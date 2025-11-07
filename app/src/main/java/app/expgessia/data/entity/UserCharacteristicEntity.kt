// data/entity/UserCharacteristicEntity.kt
package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_characteristics",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CharacteristicEntity::class,
            parentColumns = ["id"],
            childColumns = ["characteristic_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserCharacteristicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "characteristic_id") val characteristicId: Int,
    val value: Int
)