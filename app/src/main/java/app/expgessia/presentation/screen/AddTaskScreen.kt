package app.expgessia.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.expgessia.R
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.RepeatMode
import app.expgessia.domain.model.Task
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.CharacteristicBadge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. СОБИРАЕМ СОСТОЯНИЕ: Список характеристик
    val characteristics by viewModel.characteristicsUiState.collectAsState()

    // 2. Локальные состояния формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var xpRewardText by remember { mutableStateOf("10") } // XP как String для TextField
    var selectedCharacteristic by remember { mutableStateOf<Characteristic?>(null) }
    var repeatMode by remember { mutableStateOf(RepeatMode.NONE) }

    // Состояния для выпадающих меню
    var charExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(characteristics) {
        if (selectedCharacteristic == null && characteristics.isNotEmpty()) {
            selectedCharacteristic = characteristics.first()
        }
    }

    val isFormValid =
        title.isNotBlank() && selectedCharacteristic != null && xpRewardText.toIntOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_add_new_task)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        // Используем иконку "назад" или "закрыть"
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // 1. Заголовок
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.label_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Описание
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.label_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Выбор Характеристики (Characteristic)
            ExposedDropdownMenuBox(
                expanded = charExpanded,
                onExpandedChange = { charExpanded = !charExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                // UI-элемент, который показывает выбранное значение
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { charExpanded = true }
                        .padding(vertical = 8.dp)
                        .menuAnchor(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_characeristic), style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))

                    selectedCharacteristic?.let { characteristic ->
                        // ✅ ИСПОЛЬЗУЕМ ПЕРЕИСПОЛЬЗУЕМЫЙ КОМПОНЕНТ
                        CharacteristicBadge(
                            iconName = characteristic.iconResName, // <--- Сюда нужно вставить новый компонент
                            name = stringResource(characteristic.getLocalizedNameResId()).uppercase(),
                            modifier = Modifier.weight(1f)
                        )
                    } ?: Text(
                        text = "Select...",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error
                    )

                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }

                // Выпадающее меню с опциями
                ExposedDropdownMenu(
                    expanded = charExpanded,
                    onDismissRequest = { charExpanded = false }
                ) {
                    characteristics.forEach { characteristic ->
                        DropdownMenuItem(
                            // 💡 Используем CharacteristicBadge внутри DropdownMenuItem
                            text = {
                                CharacteristicBadge(
                                    iconName = characteristic.iconResName,
                                    name = stringResource(characteristic.getLocalizedNameResId()).uppercase(),
                                )
                            },
                            onClick = {
                                selectedCharacteristic = characteristic
                                charExpanded = false
                            },
                            // Закрываем меню после выбора
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Награда XP
            OutlinedTextField(
                value = xpRewardText,
                onValueChange = { newValue ->
                    // Ограничиваем ввод только цифрами
                    xpRewardText = newValue.filter { it.isDigit() }
                },
                label = { Text(stringResource(R.string.label_reward)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = "XP") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Выбор режима повторения (Repeat Mode)
            ExposedDropdownMenuBox(
                expanded = repeatExpanded,
                onExpandedChange = { repeatExpanded = !repeatExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { repeatExpanded = true }
                        .padding(vertical = 12.dp)
                        .menuAnchor(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_repeat), style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(repeatMode.stringResId).uppercase(), style = MaterialTheme.typography.labelLarge)
                    }

                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }

                ExposedDropdownMenu(
                    expanded = repeatExpanded,
                    onDismissRequest = { repeatExpanded = false }
                ) {
                    RepeatMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(stringResource(mode.stringResId).uppercase()) },
                            onClick = {
                                repeatMode = mode
                                repeatExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 6. Кнопка Сохранения
            Button(
                onClick = {
                    if (isFormValid) {
                        // ✅ Используем доменную модель Task
                        val taskToSave = Task(
                            id = 0, // Room сгенерирует ID
                            title = title,
                            description = description,
                            characteristicId = selectedCharacteristic!!.id,
                            repeatMode = repeatMode,
                            repeatDetails = null,
                            xpReward = xpRewardText.toInt(),
                            isCompleted = false,
                            scheduledFor = null
                        )
                        viewModel.onAddTask(taskToSave) // ✅ ПЕРЕДАЕМ Task
                        onBackClicked() // закрываем экран после сохранения
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ADD TASK")
            }
        }
    }
}