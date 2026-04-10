package compose.project.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.project.data.model.HabitStatus
import compose.project.home.CalendarUiState
import compose.project.home.MonthUiModel

@Composable
fun YearCalendar(
    calendarState: CalendarUiState
) {
    val months = calendarState.months
        .groupBy { it.yearMonth.year }
        .values
        .first()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(months) { month ->
            YearMonthItem(month)
        }
    }
}

@Composable
fun YearMonthItem(month: MonthUiModel) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Text(
            text = month.yearMonth.month.name.take(3),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(4.dp))

        MonthStatusBars(month)
    }
}

@Composable
fun MonthStatusBars(month: MonthUiModel) {
    val days = month.weeks.flatMap { it.days }.filterNotNull()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        days.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(getColor(day.habitStatus))
            )
        }
    }
}

fun getColor(status: HabitStatus): Color {
    return when (status) {
        HabitStatus.COMPLETED -> Color.Green
        HabitStatus.MISSED -> Color.Red
        HabitStatus.UNMARKED -> Color.Gray
    }
}