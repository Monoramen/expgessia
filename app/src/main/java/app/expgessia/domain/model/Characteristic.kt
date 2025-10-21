package app.expgessia.domain.model

import androidx.annotation.DrawableRes

data class Characteristic(
    val id: Int,                 // PRIMARY KEY
    val name: String,            // "Strength", "Perception", ...
    val description: String,     // описание навыка
    val iconResName: String      // имя ресурса ("ic_strength")
)


