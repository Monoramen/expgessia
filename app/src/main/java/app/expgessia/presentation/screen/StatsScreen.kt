package app.expgessia.presentation.ui.screens

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.* // ⭐️ ВАЖНО: collectAsState() должен быть здесь
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.expgessia.R
import app.expgessia.domain.model.StatsUiState
import app.expgessia.presentation.ui.components.BlinkingFooter
import app.expgessia.presentation.ui.components.TerminalScanlines
import app.expgessia.presentation.ui.components.formatStatValue
import app.expgessia.presentation.ui.theme.FalloutFontFamilyDigits
import app.expgessia.presentation.ui.theme.FalloutOutline
import app.expgessia.presentation.ui.theme.FalloutPrimary
import app.expgessia.presentation.viewmodel.StatsViewModel
import app.expgessia.utils.TimeUtils
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat // ⭐️ ИМПОРТ: для форматирования даты
import java.util.Date
import java.util.Locale



data class PlayerStat(
    val title: String,
    val value: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatRow(
    stat: PlayerStat,
    digitsFontFamily: FontFamily
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "pressBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )
    val baseColor = MaterialTheme.colorScheme.onBackground
    val glowColor = FalloutPrimary
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) glowColor else baseColor,
        animationSpec = tween(200),
        label = "pressColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            FalloutPrimary.copy(alpha = 0.15f * blinkAlpha)
        else
            Color.Transparent,
        animationSpec = tween(150),
        label = "pressBackground"
    )

    val baseTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal
    )

    val baseSpanStyle = baseTextStyle.toSpanStyle()

    val digitsSpanStyle = SpanStyle(
        fontFamily = digitsFontFamily
    )

    val formattedValue = formatStatValue(
        text = stat.value,
        baseStyle = baseSpanStyle,
        digitStyle = digitsSpanStyle
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(2.dp))
            .background(backgroundColor, shape = RoundedCornerShape(2.dp))
            .combinedClickable(
                onClick = {
                    isPressed = true
                    scope.launch {
                        repeat(3) {
                            isPressed = !isPressed
                            kotlinx.coroutines.delay(80)
                        }
                        isPressed = false
                    }
                },
                onLongClick = {
                    isPressed = true
                    scope.launch {
                        repeat(6) {
                            isPressed = !isPressed
                            kotlinx.coroutines.delay(80)
                        }
                        isPressed = false
                    }
                }
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stat.title,
            color = if (isPressed) glowColor.copy(alpha = blinkAlpha) else animatedColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = formattedValue,
            color = if (isPressed) glowColor.copy(alpha = blinkAlpha) else animatedColor,
            style = baseTextStyle
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FalloutOutline.copy(alpha = 0.6f))
    )
}

// ---------------------------------------------------------------------

@Composable
fun mapStatsUiStateToPlayerStats(uiState: StatsUiState): List<PlayerStat> {
    return listOf(
        PlayerStat(
            title = stringResource(R.string.stat_tasks_completed),
            value = uiState.safeTotalTasksCompleted.toString()
        ),
        PlayerStat(
            title = stringResource(R.string.stat_total_xp_earned),
            value = "${uiState.safeTotalXpEarned}"
        ),
        PlayerStat(
            title = stringResource(R.string.stat_xp_today),
            value = "${uiState.safeXpToday}"
        ),
        PlayerStat(
            title = stringResource(R.string.stat_last_visit),
            value = TimeUtils.formatLastVisit(uiState.safeLastVisit)
        ),
        PlayerStat(
            title = stringResource(R.string.stat_time_in_game),
            value = TimeUtils.formatTime(uiState.safeTimeInGameMs)
        ),
        PlayerStat(
            title = stringResource(R.string.stat_login_streak),
            value = "${uiState.safeCurrentStreak}"
        ),
        PlayerStat(
            title = stringResource(R.string.stat_record_day),
            value = "${uiState.safeRecordXpDay}"
        ),
        PlayerStat(
            title = stringResource(R.string.stat_status),
            value = stringResource(id = uiState.status)
        )
    )
}

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    // ⭐️ ИСПРАВЛЕНО: Теперь collectAsState() работает корректно
    val uiState by viewModel.uiState.collectAsState()

    val stats = mapStatsUiStateToPlayerStats(uiState)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TerminalScanlines()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            stats.forEach { stat ->
                StatRow(stat = stat, digitsFontFamily = FalloutFontFamilyDigits)
            }

            Spacer(modifier = Modifier.height(32.dp))

            BlinkingFooter()
        }
    }
}
