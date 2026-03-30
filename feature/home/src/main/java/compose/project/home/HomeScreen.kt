package compose.project.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitTrackerScreen() {
    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }
    val daysInMonth = remember { today.lengthOfMonth() }

    var completedDays by remember { mutableStateOf(setOf<LocalDate>()) }

    val days = remember {
        (1..daysInMonth).map { firstDayOfMonth.withDayOfMonth(it) }
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    VerticalCalendarList(selectedDate, {})
}

@Composable
fun VerticalCalendarList(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    monthsBefore: Int = 12,    // количество месяцев до текущего
    monthsAfter: Int = 12,     // количество месяцев после текущего
    modifier: Modifier = Modifier
) {
    val currentMonth = YearMonth.from(selectedDate)
    // Генерируем список месяцев для отображения
    val months = remember(currentMonth, monthsBefore, monthsAfter) {
        (-monthsBefore..monthsAfter).map { currentMonth.plusMonths(it.toLong()) }
    }

    // Находим индекс текущего месяца в списке
    val initialIndex = months.indexOfFirst { it == currentMonth }.coerceAtLeast(0)

    val listState = rememberLazyListState()

    // Прокручиваем к текущему месяцу после первой композиции
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(months) { yearMonth ->
            MonthBlock(
                yearMonth = yearMonth,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun MonthBlock(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    // Смещение для первого дня (пн=0, вс=6)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7

    // Форматирование названия месяца и года
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Заголовок месяца – центрируем
        Text(
            text = yearMonth.format(monthYearFormatter)
                .replaceFirstChar { it.uppercase() },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center  // центрирование
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Строка с названиями дней недели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val dayOfWeek = (i + 1) % 7
                val dayName = dayOfWeekFormatter.withLocale(Locale.getDefault())
                    .format(dayOfWeek.let { java.time.DayOfWeek.of(if (it == 0) 7 else it) })
                Text(
                    text = dayName.take(3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        val weeks = mutableListOf<MutableList<LocalDate?>>()
        var currentDay = 1
        while (currentDay <= daysInMonth) {
            val week = MutableList(7) { null as LocalDate? }
            for (col in 0..6) {
                if (weeks.isEmpty() && col < startOffset) continue
                if (currentDay > daysInMonth) break
                week[col] = yearMonth.atDay(currentDay)
                currentDay++
            }
            weeks.add(week)
        }

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (day != null && day == selectedDate)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .then(
                                if (day != null) Modifier.clickable { onDateSelected(day) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontSize = 16.sp,
                                color = if (day == selectedDate)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (day == selectedDate) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitTrackerScreenPreview() {
    HabitTrackerScreen()
}