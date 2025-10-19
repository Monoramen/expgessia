package app.expgessia.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.expgessia.data.converter.DateConverter
import java.util.Date


@Entity(tableName = "users")
@TypeConverters(DateConverter::class)
data class UserEntity(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    @PrimaryKey val id: Int = 0,
    val name: String,
    val experience: Int,
    val level: Int,
    val score: Int,
    val mana: Int,
    val strength: Int,
    val intelligence: Int,
    val agility: Int,
    val lastLogin: Date?, // Последний вход
    val photoUri: String? = null // Добавим поле для фото
)