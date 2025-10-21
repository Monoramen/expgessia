package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.expgessia.data.converter.DateConverter
import java.util.Date


@Entity(tableName = "characteristics")
data class CharacteristicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    @ColumnInfo(name = "icon_res_name")
    val iconResName: String
)