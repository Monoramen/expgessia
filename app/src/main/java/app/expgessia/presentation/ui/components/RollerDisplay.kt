package app.expgessia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.presentation.ui.theme.DigitLargeStyle

@Composable
fun RollerDisplay(
    value: Any,
    width: Int,
    digitColor: Color,
    boxColor: Color,
    isExp: Boolean = false,
    modifier: Modifier = Modifier // ⭐️ Добавляем параметр modifier
) {
    val valueFloat = if (value is Int) value.toFloat() else value as Float

    val formattedString = if (isExp) {
        val intPart = (valueFloat * 100).toInt()
        String.format("%0${width}d", intPart)
    } else {
        String.format("%0${width}d", valueFloat.toInt())
    }

    Row(
        modifier = modifier, // ⭐️ Применяем modifier к Row
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        formattedString.forEach { digit ->
            RollerDigit(
                digit = digit,
                style = DigitLargeStyle,
                digitColor = digitColor,
                boxColor = boxColor
            )
        }

        if (isExp) {
            Text(
                text = "%",
                style = DigitLargeStyle.copy(fontSize = 16.sp),
                color = digitColor,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}