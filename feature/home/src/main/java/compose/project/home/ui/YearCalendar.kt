package compose.project.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.project.data.model.HabitStatus
import compose.project.designsystem.R
import compose.project.home.CalendarUiState
import compose.project.home.DayUiModel
import compose.project.home.MonthUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
enum class StreakPart {
    SINGLE, START, MIDDLE, END, NONE
}

@Composable
fun YearCalendar(
    calendarState: CalendarUiState,
    modifier: Modifier = Modifier
) {
    val selectedYear = LocalDate.now().year

    val yearMonths = remember(calendarState.months, selectedYear) {
        calendarState.months
            .filter { it.yearMonth.year == selectedYear }
            .sortedBy { it.yearMonth.monthValue }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp),
        contentPadding = PaddingValues(top = 72.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = yearMonths,
            key = { it.yearMonth.toString() }
        ) { month ->
            YearMonthCard(monthUiModel = month)
        }
    }
}

@Composable
fun YearMonthCard(
    monthUiModel: MonthUiModel,
    modifier: Modifier = Modifier
) {
    val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(18.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = monthUiModel.yearMonth
                .format(monthYearFormatter)
                .replaceFirstChar { it.uppercase() },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        monthUiModel.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.days.forEachIndexed { index, dayUiModel ->
                    val streakPart = week.days.streakPartAt(index)

                    YearDayCell(
                        dayUiModel = dayUiModel,
                        streakPart = streakPart,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearDayCell(
    dayUiModel: DayUiModel?,
    streakPart: StreakPart,
    modifier: Modifier = Modifier
) {
    val status = dayUiModel?.habitStatus

    val baseColor = when (status) {
        HabitStatus.COMPLETED -> colorResource(R.color.habit_completed)
        HabitStatus.MISSED -> colorResource(R.color.habit_missed)
        HabitStatus.UNMARKED -> colorResource(R.color.habit_unmarked)
        null -> Color.Transparent
    }


    val shape = when (streakPart) {
        StreakPart.SINGLE -> CircleShape
        StreakPart.START -> RoundedCornerShape(
            topStart = 4.dp,
            bottomStart = 4.dp
        )
        StreakPart.MIDDLE -> RectangleShape
        StreakPart.END -> RoundedCornerShape(
            topEnd = 4.dp,
            bottomEnd = 4.dp,
        )
        StreakPart.NONE -> RectangleShape
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        if (dayUiModel != null) {
            val backgroundModifier = when (streakPart) {
                StreakPart.SINGLE -> Modifier
                    .fillMaxSize()
                StreakPart.START,
                StreakPart.MIDDLE,
                StreakPart.END -> Modifier
                    .fillMaxWidth()
                    .height(14.dp)

                StreakPart.NONE -> Modifier
            }

            Box(
                modifier = backgroundModifier
                    .clip(shape)
                    .background(baseColor)
            )
        }
    }
}

private fun List<DayUiModel?>.streakPartAt(index: Int): StreakPart {
    val current = getOrNull(index) ?: return StreakPart.NONE

    val prev = getOrNull(index - 1)
    val next = getOrNull(index + 1)

    val hasPrev = prev?.habitStatus == current.habitStatus
    val hasNext = next?.habitStatus == current.habitStatus

    return when {
        !hasPrev && !hasNext -> StreakPart.SINGLE
        !hasPrev && hasNext -> StreakPart.START
        hasPrev && !hasNext -> StreakPart.END
        else -> StreakPart.MIDDLE
    }
}
