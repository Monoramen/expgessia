package app.expgessia.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.R
import app.expgessia.presentation.ui.theme.FalloutOutline
import app.expgessia.presentation.ui.theme.FalloutPrimary


@Composable
fun TerminalScanlines() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val lineHeight = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color(0xFF00FF00).copy(alpha = 0.06f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineHeight
        }
    }
}

fun formatStatValue(
    text: String,
    baseStyle: SpanStyle,
    digitStyle: SpanStyle
) = buildAnnotatedString {
    text.forEach { char ->
        if (char.isDigit()) {
            withStyle(digitStyle) {
                append(char)
            }
        } else {
            withStyle(baseStyle) {
                append(char)
            }
        }
    }
}

// ---------------------------------------------------------------------
@Composable
fun BlinkingFooter() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // ⭐️ ИСПРАВЛЕНО: Используем строковый ресурс
        Text(
            text = stringResource(R.string.footer_end_of_file),
            color = FalloutOutline,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(10.dp, 14.dp)
                .alpha(alpha)
                .background(FalloutPrimary)
        )
    }
}