package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.expgessia.data.converter.DateConverter
import java.util.Date


@Entity(tableName = "users")
@TypeConverters(DateConverter::class)
data class UserEntity(

    @PrimaryKey val id: Int = 1,
    val name: String,
    val experience: Int,
    val level: Int,
    val money: Int,
    val strength: Int,
    val perception: Int, // add Perception
    val endurance: Int,
    val charisma: Int,
    val intelligence: Int,
    val agility: Int,
    val luck: Int,
    @ColumnInfo(name = "last_login")
    val lastLogin: Date?, // Последний вход
    @ColumnInfo(name = "photo_uri")
        val photoUri: String? = null // Добавим поле для фото
)