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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.project.domain.model.HabitStatus
import compose.project.home.CalendarUiState
import compose.project.home.DayUiModel
import compose.project.home.MonthUiModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun YearCalendar(
    calendarState: CalendarUiState,
    modifier: Modifier = Modifier
) {
    val selectedYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    val yearMonths = remember(calendarState.months, selectedYear) {
        calendarState.months
            .filter { it.yearMonth.year == selectedYear }
            .sortedBy { it.yearMonth.month }
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
    val monthName = formatMonth(
        monthUiModel.yearMonth.month.number
    ).replaceFirstChar { it.uppercase() }

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
            text = monthName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        monthUiModel.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.days.forEach { dayUiModel ->
                    YearDayCell(
                        dayUiModel = dayUiModel,
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
    modifier: Modifier = Modifier
) {
    val completedColor = Color(0xFF4CAF50)
    val missedColor = MaterialTheme.colorScheme.error
    val unmarkedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    val backgroundColor = when (dayUiModel?.habitStatus) {
        HabitStatus.COMPLETED -> completedColor
        HabitStatus.MISSED -> missedColor
        HabitStatus.UNMARKED -> unmarkedColor
        null -> Color.Transparent
    }

    val textColor = when (dayUiModel?.habitStatus) {
        HabitStatus.COMPLETED, HabitStatus.MISSED -> Color.White
        HabitStatus.UNMARKED -> MaterialTheme.colorScheme.onSurface
        null -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (dayUiModel != null) {
            Text(
                text = dayUiModel.date.day.toString(),
                fontSize = 7.sp,
                lineHeight = 7.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

expect fun formatMonth(monthNumber: Int): String
