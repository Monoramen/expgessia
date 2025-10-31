package app.expgessia.presentation.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import java.time.LocalDate
import kotlin.math.absoluteValue



@Composable
fun DateCarousel(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {

    val totalCount = Int.MAX_VALUE
    val initialCenterIndex = totalCount / 2
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val itemWidthDp = 50.dp

    val dayOffsetFromNow = selectedDate.toEpochDay() - LocalDate.now().toEpochDay()
    val initialIndex = initialCenterIndex + dayOffsetFromNow.toInt()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val maxOffsetDp = configuration.screenWidthDp.dp / 2f // Максимальное смещение от центра

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = maxOffsetDp - itemWidthDp / 2f),
    ) {
        items(totalCount) { index ->
            val offsetFromCenter = index - initialCenterIndex
            val date = remember(index) {
                LocalDate.now().plusDays(offsetFromCenter.toLong())
            }


            DateItem(
                date = date,
                isSelected = date == selectedDate,
                onDateClicked = onDateSelected,
            )
        }
    }
}