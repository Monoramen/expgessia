package app.expgessia.presentation.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.expgessia.presentation.ui.theme.SmallTypography
import java.time.LocalDate

@Composable
fun TaskDayCell(
    dateLocal: LocalDate,
    tasks: List<CalendarSimpleTask>,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    onDayClicked: (LocalDate) -> Unit,
    cellHeight: Dp
) {
    val todayHighlightColor = Color(0xFF2B6F07)
    val dayColor = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val taskTextColor = if (isCurrentMonth) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .height(cellHeight)         // <-- жёсткая высота
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 0.4.dp.toPx()
                drawRect(color = Color(0xFF979797), style = Stroke(strokeWidth))
            }
            .background(Color(0xFF2E2F2D))
            .clickable { onDayClicked(dateLocal) }
            .let {
                if (isToday) it.border(2.dp, todayHighlightColor) else it
            }
    ) {


        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = dateLocal.dayOfMonth.toString(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleSmall,
            color = dayColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        tasks.take(7).forEach { task ->
            Column {
                Text(
                    text = task.name,
                    style = SmallTypography.bodySmall,
                    color = taskTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF6B6C69).copy(alpha = if (isCurrentMonth) 1f else 0.5f))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        if (tasks.size > 7) {
            Text(
                text = "+${tasks.size - 7} more",
                style = SmallTypography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = if (isCurrentMonth) 1f else 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}