// presentation/screen/UserScreen.kt
package app.expgessia.presentation.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class Skill(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val progressPercent: Float = 0.84f, // Используем Float от 0.0 до 1.0
)

val skills = listOf(
    Skill(
        title = "Фитнес",
        description = "Тренировки и здоровье",
        icon = Icons.Filled.Info,
        progressPercent = 0.7f
    ),
    Skill(
        title = "Программирование",
        description = "Учёба Kotlin и Android",
        icon = Icons.Filled.Info,
        progressPercent = 0.5f
    ),
    Skill(
        title = "Образование",
        description = "Книги, курсы, практика",
        icon = Icons.Filled.Info,
        progressPercent = 0.9f
    )
)

@Composable
fun SkillScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(skills) { skill ->
            SkillCard(skill)
        }
    }
}


@Composable
fun SkillCard(
    skill: Skill,
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
                imageVector = skill.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Cyan
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = skill.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = skill.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.size(44.dp), // общий контейнер под круг + текст
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { skill.progressPercent },
                    modifier = Modifier.fillMaxSize(), // круг на весь Box
                    color = Color.Green,
                    strokeWidth = 8.dp,
                    trackColor = Color.Green.copy(alpha = 0.3f),
                )

                Text(
                    text = "${(skill.progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        }
    }
}
