package compose.project.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.project.designsystem.theme.HabitsTrackerTheme
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.runtime.State
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

object CalendarDefaults {
    val CardPadding = 16.dp
    val MonthTextSize = 20.sp
    val SpaceAfterMonth = 16.dp
    val DaysOfWeekTextSize = 14.sp
}

@Composable
fun HabitTrackerScreen(
    liquidState: LiquidState = rememberLiquidState(),
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    CalendarTabFrame(liquidState = liquidState) {
        VerticalCalendarList(
            selectedDate = selectedDate,
            onDateSelected = { },
            liquidState = liquidState
        )
    }
}

@Composable
fun CalendarTabFrame(
    modifier: Modifier = Modifier,
    liquidState: LiquidState,
    content: @Composable () -> Unit
) {

    val selectedState = remember { mutableStateOf(MonthYearSelection.MONTH) }

    val onSelectionChanged = remember {
        { selection: MonthYearSelection -> selectedState.value = selection }
    }

    Column (
        modifier = modifier.fillMaxSize()
        .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 0.dp),
    ) {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Image(
                painter = painterResource(id = compose.project.designsystem.R.drawable.winecolor_black),
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(35.dp)
            )
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 22.dp),
            shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()

                MonthYearSwitcher(
                    modifier = Modifier.align(Alignment.TopCenter),
                    liquidState = liquidState,
                    selectedState = selectedState,
                    onSelectionChanged = onSelectionChanged
                )
            }
        }
    }
}

enum class MonthYearSelection {
    MONTH, YEAR
}

@Composable
fun MonthYearSwitcher(
    modifier: Modifier = Modifier,
    liquidState: LiquidState,
    selectedState: State<MonthYearSelection>,
    onSelectionChanged: (MonthYearSelection) -> Unit = {}
) {

    val monthText = stringResource(R.string.month_switcher_month)
    val yearText = stringResource(R.string.month_switcher_year)
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .liquid(liquidState) {
                    shape = RoundedCornerShape(100)
                    refraction = 0.5f
                    curve = 0.5f
                    edge = 0.1f
                    tint = Color.White.copy(alpha = 0.2f)
                    saturation = 1.5f
                    dispersion = 0.25f
                }
                .padding(6.dp)
        ) {
            MonthYearButton(
                text = monthText,
                selection = MonthYearSelection.MONTH,
                selectedState = selectedState,
                onClick = { onSelectionChanged(MonthYearSelection.MONTH) },
                textMeasurer = textMeasurer
            )
            Spacer(modifier = Modifier.width(8.dp))
            MonthYearButton(
                text = yearText,
                selection = MonthYearSelection.YEAR,
                selectedState = selectedState,
                onClick = { onSelectionChanged(MonthYearSelection.YEAR) },
                textMeasurer = textMeasurer
            )
        }
    }
}

@Composable
private fun MonthYearButton(
    text: String,
    selection: MonthYearSelection,
    selectedState: State<MonthYearSelection>,
    onClick: () -> Unit,
    textMeasurer: TextMeasurer = rememberTextMeasurer()
) {
    val textSizeConst = 18.sp
    val density = LocalDensity.current
    val textStyle = TextStyle(
        fontSize = textSizeConst,
        fontWeight = FontWeight.Normal
    )
    val textLayout = textMeasurer.measure(text, textStyle)
    val textWidth = textLayout.size.width.toFloat()
    val textHeight = textLayout.size.height.toFloat()

    val horizontalPadding = with(density) { 16.dp.toPx() }
    val verticalPadding = with(density) { 8.dp.toPx() }

    val buttonWidth = textWidth + horizontalPadding * 2
    val buttonHeight = textHeight + verticalPadding * 2

    val paint = Paint().apply {
        textSize = with(density) { textSizeConst.toPx() }
        isAntiAlias = true
    }
    val textBounds = Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)
    val textHeightPx = textBounds.height().toFloat()

    val availableHeight = buttonHeight - verticalPadding * 2
    val baselineOffset = verticalPadding + (availableHeight - textHeightPx) / 2 - textBounds.top

    Box(
        modifier = Modifier
            .size(
                width = with(density) { buttonWidth.toDp() },
                height = with(density) { buttonHeight.toDp() }
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .drawWithContent {
                val isSelected = selectedState.value == selection

                drawRoundRect(
                    color = if (isSelected) Color(0xCC3C6FB6) else Color.Transparent,
                    cornerRadius = CornerRadius(20.dp.toPx())
                )

                paint.color = (if (isSelected) Color.White else Color.Black).toArgb()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        text,
                        horizontalPadding,
                        baselineOffset,
                        paint
                    )
                }
            }
    )
}

@Composable
fun VerticalCalendarList(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    monthsBefore: Int = 12,
    monthsAfter: Int = 12,
    liquidState: LiquidState
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
        modifier = modifier.fillMaxSize().liquefiable(liquidState),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 16.dp
        )
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
fun MonthBlock(
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
            containerColor = MaterialTheme.colorScheme.tertiary
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
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
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
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
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
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
                                        MaterialTheme.colorScheme.onPrimary
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
    HabitsTrackerTheme {
        HabitTrackerScreen()
    }
}

fun TextUnit.toDp(density: Density): Dp {
    return with(density) {
        this@toDp.toPx().toDp()
    }
}