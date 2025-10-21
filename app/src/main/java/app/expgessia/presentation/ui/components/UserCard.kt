package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.expgessia.R
import app.expgessia.domain.model.User
import app.expgessia.presentation.ui.theme.DigitMediumStyle


// ⭐️ ЦВЕТА для характеристик
private val ColorSTR = Color(0xFFE57373) // Красный (Сила)
private val ColorPER = Color(0xFF81C784) // Светло-зеленый (Восприятие)
private val ColorEND = Color(0xFF64B5F6) // Голубой (Выносливость)
private val ColorCHA = Color(0xFFFFB74D) // Оранжевый (Харизма)
private val ColorINT = Color(0xFFBA68C8) // Фиолетовый (Интеллект)
private val ColorAGI = Color(0xFFFF8A65) // Персиковый (Ловкость)
private val ColorLCK = Color(0xFFFFF176) // Желтый (Удача)
// private val ColorWhite = Color(0xFFFFFFFF) // Не используется после замены


@Composable
fun UserCard(
    user: User?,
    onNameEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    val isNameValid = editedName.isNotBlank()

    val onCardColor = MaterialTheme.colorScheme.onPrimaryContainer
    val heroBackground = Color(0xFFACA452)
    val primaryColor = MaterialTheme.colorScheme.primary


    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
    } else {
        Column(modifier = Modifier.padding(0.dp)) {

            // ⭐️ БЛОК 1: ИМЯ
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Имя
                if (isEditingName) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        label = {
                            Text(
                                stringResource(R.string.label_character_name),
                                color = onCardColor.copy(alpha = 0.6f)
                            )
                        },
                        isError = !isNameValid,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = {
                                onNameEdit(editedName); isEditingName = false; editedName = ""
                            }, enabled = isNameValid) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = onCardColor
                                )
                            }
                        }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isEditingName = true; editedName = user.name
                        }
                    ) {
                        Text(
                            text = user.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = onCardColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = onCardColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ⭐️ БЛОК 2: ИКОНКА ПЕРСОНАЖА + LVL/MONEY/EXP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // ✅ Заставить все дочерние элементы соответствовать максимальной высоте
                    .height(IntrinsicSize.Max),
                verticalAlignment = Alignment.Top
            ) {

                RetroFrame(
                    modifier = Modifier
                        .width(100.dp)
                        // ✅ Добавляем fillMaxHeight(), чтобы RetroFrame занял IntrinsicSize.Max
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            // ✅ ПЕРЕКРЫВАЕМ ЧЕРНЫЙ ФОН
                            .background(heroBackground)

                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.hero),
                            contentDescription = stringResource(R.string.nav_hero),
                            tint = Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))


                // 2. БЛОК: LVL / MONEY / EXP (Без изменений, он сам задает высоту)
                RetroFrame(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = ContentPaddingHorizontal)
                            .padding(vertical = ContentPaddingTextVertical),
                    ) {
                        Text(
                            text = stringResource(R.string.text_level, user.level),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = stringResource(R.string.text_experience, user.experience),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = stringResource(R.string.text_money, user.money),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- Блок характеристик ----------

            RetroFrame() {
                CharacteristicsGrid(user)
            }

        }
    }
}


@Composable
private fun StatItem(labelResId: Int, value: Int) {

    val label = stringResource(id = labelResId)

    // ⭐️ Используем Row для горизонтального размещения
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Отступы между строками
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Метка (выровнена по левому краю)
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            // Дополнительный модификатор, если нужно ограничить ширину
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value.toString(),
            style = DigitMediumStyle,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Composable
private fun CharacteristicsGrid(user: User) {
    // Внутренний отступ, чтобы текст не сливался с рамкой
    val innerPadding = 6.dp

    // ⭐️ Родительская Row для горизонтального размещения колонок
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 💡 ВАЖНО: IntrinsicSize.Max гарантирует, что Divider будет иметь высоту самой высокой колонки
            .height(IntrinsicSize.Max)
            .padding(horizontal = innerPadding, vertical = innerPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. КОЛОНКА 1 (4 стата)
        Column(
            modifier = Modifier.weight(1f).padding(end = 8.dp) // Занимает половину ширины
        ) {
            // ✅ Используем strings
            StatItem(R.string.stat_strength, user.strength)
            StatItem(R.string.stat_perception, user.perception)
            StatItem(R.string.stat_endurance, user.endurance)
            StatItem(R.string.stat_charisma, user.charisma)
        }

        // ⭐️ ВЕРТИКАЛЬНЫЙ РАЗДЕЛИТЕЛЬ (Опционально, но как на скриншоте)
        VerticalDivider(
            color = MaterialTheme.colorScheme.primary, // Ваш зеленый цвет
            modifier = Modifier
                .fillMaxHeight() // Растягиваем до высоты родительской Row
                .width(1.dp)     // Толщина линии
        )

        // 2. КОЛОНКА 2 (3 стата)
        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp) // Отступ от разделителя
        ) {
            // ✅ Используем новые ID строк
            StatItem(R.string.stat_intelligence, user.intelligence)
            StatItem(R.string.stat_agility, user.agility)
            StatItem(R.string.stat_luck, user.luck)
        }
    }
}