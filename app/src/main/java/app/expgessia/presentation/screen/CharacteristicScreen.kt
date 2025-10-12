// presentation/screen/UserScreen.kt
package app.expgessia.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class Characteristic(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val level: Int = 1
)
val characteristics = listOf(
    Characteristic(
        title = "Agility",
        description = "Гибкость тела и ума, адаптивность. Новые маршруты, лёгкое планирование, смена рутин.\n\n**What it is:** Adapting quickly + staying sharp.\n- **How to boost it:** Speed drills (e.g., typing, cooking), trying new hobbies.\n- **Perk:** \"Fast Reflexes\" – React better under pressure.",
        icon = Icons.Default.Favorite,
        level = 1
    ),
    Characteristic(
        title = "Charisma",
        description = "Связь с другими, эмпатия, принятие. Мелкие добрые дела, слушание, честное общение.\n\n**What it is:** Likeability + communication skills.\n- **How to boost it:** Practice listening, telling stories, being kind.\n- **Perk:** \"Smooth Talker\" – Win people over easier.",
        icon = Icons.Default.Favorite,
        level = 1
    ),
    Characteristic(
        title = "Endurance",
        description = "Устойчивость, энергия, забота о рутине. Теплое утро, сон, режим, отдых, план без стресса.\n\n**What it is:** Stamina for long-term goals.\n- **How to boost it:** Good sleep, breaking big tasks into small steps.\n- **Perk:** \"Energizer Bunny\" – Work longer without burning out",
        icon = Icons.Default.Favorite,
        level = 1
    ),
    Characteristic(
        title = "Intellect",
        description = "Любопытство, мышление, обучение. Новый факт в день, интересное видео, вопросы к себе.\n\n**What it is:** Learning + problem-solving.\n- **How to boost it:** Reading, asking questions, trying new things.\n- **Perk:** \"Quick Learner\" – Pick up skills fast.",
        icon = Icons.Default.Favorite,
        level = 1
    )
)

@Composable
fun CharacteristicScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(characteristics) { char ->
            CharacteristicCard(char)
        }
    }
}


@Composable
fun CharacteristicCard(
    characteristic: Characteristic,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = characteristic.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Cyan
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = characteristic.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = characteristic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = characteristic.level.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}