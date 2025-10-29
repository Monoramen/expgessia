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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import app.expgessia.presentation.ui.components.CustomTopAppBar
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.CharacteristicBadge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onBackClicked: () -> Unit,
    taskIdToEdit: Long?,
    modifier: Modifier = Modifier,
) {
    // 1. СОБИРАЕМ СОСТОЯНИЕ: Список характеристик
    val characteristics by viewModel.characteristicsUiState.collectAsState()

    val isEditMode = taskIdToEdit != null && taskIdToEdit != 0L
    // 2. Локальные состояния формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var xpRewardText by remember { mutableStateOf("10") } // XP как String для TextField
    var selectedCharacteristic by remember { mutableStateOf<Characteristic?>(null) }
    var repeatMode by remember { mutableStateOf(RepeatMode.NONE) }
// Состояние для режима WEEKLY (1=Пн, 7=Вс)
    var selectedDaysOfWeek by remember { mutableStateOf(emptySet<Int>()) }

// Состояние для режима MONTHLY
    var selectedDayOfMonth by remember { mutableStateOf<Int?>(null) }

    // Состояния для выпадающих меню
    var charExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(taskIdToEdit, characteristics) {
        if (characteristics.isEmpty()) return@LaunchedEffect

        // 1. Инициализация выбранной характеристики по умолчанию (только если ничего не выбрано)
        if (selectedCharacteristic == null) {
            selectedCharacteristic = characteristics.first()
        }

        // 2. Если режим редактирования, загружаем и заполняем данные
        if (isEditMode) {
            val task = viewModel.getTaskById(taskIdToEdit!!) // Загружаем задачу
            task?.let { loadedTask ->
                title = loadedTask.title
                description = loadedTask.description
                xpRewardText = loadedTask.xpReward.toString()
                repeatMode = loadedTask.repeatMode
                selectedCharacteristic =
                    characteristics.find { it.id == loadedTask.characteristicId }
                        ?: characteristics.first()

                // Обработка repeatDetails
                when (loadedTask.repeatMode) {
                    RepeatMode.WEEKLY -> {
                        selectedDaysOfWeek = loadedTask.repeatDetails
                            ?.split(",")
                            ?.mapNotNull { it.toIntOrNull() }
                            ?.toSet() ?: emptySet()
                    }

                    RepeatMode.MONTHLY -> {
                        selectedDayOfMonth = loadedTask.repeatDetails?.toIntOrNull()
                    }

                    else -> Unit
                }
            }
        }
    }

    val isFormValid =
        title.isNotBlank() && selectedCharacteristic != null && xpRewardText.toIntOrNull() != null


        Column(
            modifier = Modifier
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
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.label_characeristic),
                        style = MaterialTheme.typography.labelLarge
                    )
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
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.label_repeat),
                        style = MaterialTheme.typography.labelLarge
                    )
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
                        Text(
                            stringResource(repeatMode.stringResId).uppercase(),
                            style = MaterialTheme.typography.labelLarge
                        )
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
            Spacer(modifier = Modifier.height(16.dp))

// --- УСЛОВНЫЙ КОНТЕНТ: ДЕТАЛИ ПОВТОРЕНИЯ ---
            when (repeatMode) {
                RepeatMode.WEEKLY -> WeeklyRepeatDetails(
                    selectedDays = selectedDaysOfWeek,
                    onDaySelected = { day ->
                        selectedDaysOfWeek = if (selectedDaysOfWeek.contains(day)) {
                            selectedDaysOfWeek - day
                        } else {
                            selectedDaysOfWeek + day
                        }
                    }
                )

                // Todo: Реализовать MonthlyRepeatDetails (например, с помощью OutlinedTextField)
                RepeatMode.MONTHLY -> MonthlyRepeatDetails(
                    selectedDay = selectedDayOfMonth,
                    onDaySelected = { day -> selectedDayOfMonth = day }
                )

                else -> Unit // Для NONE и DAILY ничего не нужно
            }

            Spacer(modifier = Modifier.height(24.dp)) // Перед кнопкой сохранения
            // 6. Кнопка Сохранения
            Button(
                onClick = {
                    // 1. Формируем строку repeatDetails
                    val details: String? = when (repeatMode) {
                        RepeatMode.WEEKLY -> {
                            selectedDaysOfWeek.sorted().joinToString(",").takeIf { it.isNotEmpty() }
                        }

                        RepeatMode.MONTHLY -> selectedDayOfMonth?.toString()
                        else -> null
                    }

                    // 2. Дополнительная проверка возможности сохранения
                    val canSaveDetails = when (repeatMode) {
                        RepeatMode.WEEKLY -> details != null
                        RepeatMode.MONTHLY -> details != null
                        else -> true
                    }

                    if (isFormValid && canSaveDetails) {
                        // 💡 ИЗМЕНЕНИЕ 4: Используем taskIdToEdit в режиме редактирования
                        val taskId = if (isEditMode) taskIdToEdit!! else 0L

                        val taskToSave = Task(
                            id = taskId, // <--- ИСПОЛЬЗУЕМ ID
                            title = title,
                            description = description,
                            characteristicId = selectedCharacteristic!!.id,
                            repeatMode = repeatMode,
                            repeatDetails = details,
                            xpReward = xpRewardText.toInt(),
                            isCompleted = false, // Сохраняем как false, если редактируем
                            scheduledFor = null
                        )

                        // 💡 ИЗМЕНЕНИЕ 5: Добавляем логику для обновления
                        if (isEditMode) {
                            viewModel.onUpdateTask(taskToSave)
                        } else {
                            viewModel.onAddTask(taskToSave)
                        }

                        onBackClicked() // закрываем экран после сохранения/обновления
                    }
                },
                enabled = isFormValid && when (repeatMode) {
                    RepeatMode.WEEKLY -> selectedDaysOfWeek.isNotEmpty()
                    RepeatMode.MONTHLY -> selectedDayOfMonth != null
                    else -> true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // 💡 ИЗМЕНЕНИЕ 6: Текст кнопки
                Text(if (isEditMode) stringResource(R.string.button_save).uppercase() else stringResource(R.string.button_add_task).uppercase())
            }
        }
    }



// Файл: AddTaskScreen.kt (Обновленная версия)

@Composable
fun WeeklyRepeatDetails(
    selectedDays: Set<Int>, // 1 (Пн) - 7 (Вс)
    onDaySelected: (Int) -> Unit,
) {
    // Используем Map для сопоставления номера дня (1-7) и строкового ресурса
    val dayResources = mapOf(
        1 to R.string.day_short_mon,
        2 to R.string.day_short_tue,
        3 to R.string.day_short_wed,
        4 to R.string.day_short_thu,
        5 to R.string.day_short_fri,
        6 to R.string.day_short_sat,
        7 to R.string.day_short_sun
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            // Используем локализованный текст
            stringResource(R.string.label_repeat_days),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..7).forEach { dayNumber -> // Итерируемся по номерам дней от 1 до 7
                val isSelected = selectedDays.contains(dayNumber)
                val dayNameResId = dayResources[dayNumber]
                    ?: R.string.day_short_mon // Fallback, хотя не должен случиться

                Button(
                    onClick = { onDaySelected(dayNumber) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(stringResource(dayNameResId), style = MaterialTheme.typography.labelSmall)
                }
                if (dayNumber < 7) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}


// Файл: AddTaskScreen.kt

@Composable
fun MonthlyRepeatDetails(
    selectedDay: Int?,
    onDaySelected: (Int?) -> Unit,
) {
    var textValue by remember { mutableStateOf(selectedDay?.toString() ?: "") }

    // Эффект для синхронизации локального текста с внешним состоянием (при смене режима)
    LaunchedEffect(selectedDay) {
        if (selectedDay != textValue.toIntOrNull()) {
            textValue = selectedDay?.toString() ?: ""
        }
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // 1. Фильтруем, оставляя только цифры
            val filteredValue = newValue.filter { it.isDigit() }

            // 2. Преобразуем в число и проверяем ограничения (1-31)
            val dayOfMonth = filteredValue.toIntOrNull()

            // 3. Обновляем локальный текст
            textValue = filteredValue

            // 4. Обновляем внешнее состояние, только если число в пределах 1-31
            if (dayOfMonth != null && dayOfMonth in 1..31) {
                onDaySelected(dayOfMonth)
            } else if (dayOfMonth == null && filteredValue.isEmpty()) {
                // Если поле очищено
                onDaySelected(null)
            } else if (dayOfMonth != null && dayOfMonth > 31) {
                // Если введено число > 31, сбрасываем состояние
                onDaySelected(null)
            }
        },
        label = { Text(stringResource(R.string.label_day_of_month)) },
        placeholder = { Text("Например, 15") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = selectedDay == null && textValue.isNotEmpty(),
        supportingText = {
            if (selectedDay == null && textValue.isNotEmpty()) {
                Text(stringResource(R.string.error_day_of_month_invalid))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}