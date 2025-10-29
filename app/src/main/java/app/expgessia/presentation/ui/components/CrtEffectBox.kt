// Импорты для RuntimeShader (нужны для API >= 31)
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun loadCrtShaderCode(assetPath: String): String {
    val context = LocalContext.current
    return remember(assetPath) {
        // Чтение файла из assets
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }
}


/**
 * Fallback-функция для рисования статических строк развертки (scanlines) на API < 31.
 */

fun Modifier.staticCrtFallback() = this.drawBehind {
    val lineHeight = 2.dp.toPx() // Высота черной линии
    val lineSpacing = 3.dp.toPx() // Расстояние между линиями
    val alpha = 0.2f // Прозрачность линий

    val linesPaint = Paint().apply {
        color = Color.Black.copy(alpha = alpha)
    }

    // Рисуем линии, начиная с верхней границы (y = 0)
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = lineHeight
        )
        y += lineHeight + lineSpacing
    }
}

@Composable
fun CrtEffectBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val crtModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // ✅ ВАРИАНТ 1: API >= 31. Используем мощный RuntimeShader.
        // Здесь мы можем использовать полную анимацию и сложный AGSL-код.
        val infiniteTransition = rememberInfiniteTransition(label = "crt_animation")
        val time by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000000f,
            animationSpec = infiniteRepeatable(tween(1000000, easing = LinearEasing)),
            label = "time_uniform"
        )
        modifier.crtEffect(time = time) // Вызов @RequiresApi(S) функции внутри if(S)
    } else {
        // ❌ ВАРИАНТ 2: API < 31 (включая ваш minSdk 30).
        // Используем Fallback с Canvas/DrawBehind.
        modifier
            .staticCrtFallback() // Статические строки развертки
            .graphicsLayer {
                // Можно добавить небольшую виньетку и затемнение
                shadowElevation = 0.dp.toPx()
                alpha = 0.95f // Немного затемняем все содержимое
            }
    }

    Box(modifier = crtModifier) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Suppress("NewApi")
fun Modifier.crtEffect(time: Float) = composed {
    // 1. Загружаем AGSL-код из assets
    // Путь должен соответствовать структуре: assets/shaders/crt_effect.agsl
    val crtShaderCode = loadCrtShaderCode("shaders/crt_effect.agsl")

    // 2. Создаем и запоминаем шейдер с AGSL-кодом
    val crtShader = remember { RuntimeShader(crtShaderCode) }

    this.graphicsLayer {
        // Pass the actual dimensions of the Composable to the shader.
        crtShader.setFloatUniform("resolution", size.width, size.height)

        // Pass the animated time for line movement
        crtShader.setFloatUniform("time", time)

        // 3. Apply the shader as RenderEffect
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(crtShader, "composable")
            .asComposeRenderEffect()
    }
}