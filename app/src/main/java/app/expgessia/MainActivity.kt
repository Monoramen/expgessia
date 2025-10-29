// MainActivity.kt
package app.expgessia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect // ⬅️ ИМПОРТ
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb // ⬅️ ИМПОРТ
import androidx.compose.ui.platform.LocalView // ⬅️ ИМПОРТ
import androidx.core.view.WindowCompat // ⬅️ ИМПОРТ
import app.expgessia.presentation.ui.theme.expgessiaTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 💡 КЛЮЧ: Разрешаем содержимому растягиваться под статус-бар и навигационный бар
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            expgessiaTheme {

                // 💡 БЛОК НАСТРОЙКИ СТАТУС-БАРА - УДАЛЕН

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}