package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey val id: Int = 1,
    val name: String,
    val experience: Int,
    val level: Int,
    val money: Int,
    @ColumnInfo(name = "last_login")
    val lastLogin: Long?,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null, // Добавим поле для фото
)