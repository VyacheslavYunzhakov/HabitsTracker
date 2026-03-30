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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalendarDefaults {
    val CardPadding = 16.dp
    val MonthTextSize = 20.sp
    val SpaceAfterMonth = 16.dp
    val DaysOfWeekTextSize = 14.sp
}

@Composable
fun HabitTrackerScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    VerticalCalendarList(
        selectedDate = selectedDate,
        onDateSelected = { }
    )
}

@Composable
fun VerticalCalendarList(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    monthsBefore: Int = 12,
    monthsAfter: Int = 12
) {
    val currentMonth = remember { YearMonth.from(LocalDate.now()) }
    val months = remember(currentMonth, monthsBefore, monthsAfter) {
        (-monthsBefore..monthsAfter).map { currentMonth.plusMonths(it.toLong()) }
    }

    val initialIndex = remember(months) {
        months.indexOfFirst { it == currentMonth }.coerceAtLeast(0)
    }

    val listState = rememberLazyListState()

    val density = LocalDensity.current
    val containerWidthDp = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }

    val horizontalPadding = CalendarDefaults.CardPadding * 2
    val cellSize = (containerWidthDp - horizontalPadding) / 7

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    val weeksCount = (startOffset + daysInMonth + 6) / 7

    val estimatedItemHeightDp =
        CalendarDefaults.CardPadding +
                CalendarDefaults.MonthTextSize.toDp(density) +
                CalendarDefaults.SpaceAfterMonth +
                CalendarDefaults.DaysOfWeekTextSize.toDp(density) +
                (cellSize * weeksCount) +
                CalendarDefaults.CardPadding

    val containerHeightPx = LocalWindowInfo.current.containerSize.height

    LaunchedEffect(Unit) {

        val itemHeightPx = with(density) {
            estimatedItemHeightDp.toPx()
        }
        val offset = (containerHeightPx / 2f - itemHeightPx / 2f).toInt()

        listState.scrollToItem(
            index = initialIndex,
            scrollOffset = -offset
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(months) { yearMonth ->
            MonthBlock(
                yearMonth = yearMonth,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7

    val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CalendarDefaults.CardPadding)
        ) {
            Text(
                text = yearMonth.format(monthYearFormatter)
                    .replaceFirstChar { it.uppercase() },
                fontSize = CalendarDefaults.MonthTextSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(CalendarDefaults.SpaceAfterMonth))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..6) {
                    val dayOfWeek = (i + 1) % 7
                    val dayName = dayOfWeekFormatter.withLocale(Locale.getDefault())
                        .format(java.time.DayOfWeek.of(if (dayOfWeek == 0) 7 else dayOfWeek))
                    Text(
                        text = dayName.take(3).replaceFirstChar { it.uppercase() },
                        fontSize = CalendarDefaults.DaysOfWeekTextSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
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
}

@Preview(showBackground = true)
@Composable
fun HabitTrackerScreenPreview() {
    MaterialTheme {
        HabitTrackerScreen()
    }
}

fun TextUnit.toDp(density: Density): Dp {
    return with(density) {
        this@toDp.toPx().toDp()
    }
}