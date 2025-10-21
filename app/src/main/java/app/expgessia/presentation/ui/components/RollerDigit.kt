package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun RollerDigit(
    digit: Char,
    style: TextStyle,
    digitColor: Color,
    boxColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(16.dp) // Ширина одной цифры
            .height(24.dp) // Высота окошка
            .padding(horizontal = 0.5.dp) // Уменьшим отступ для более плотного ряда
            .background(boxColor, shape = MaterialTheme.shapes.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            style = style,
            color = digitColor,
            modifier = Modifier.padding(top = 1.dp) // Небольшой сдвиг для центрирования шрифта
        )
    }
}