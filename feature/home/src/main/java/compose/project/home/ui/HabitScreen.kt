package compose.project.home.ui

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.project.data.model.HabitStatus
import compose.project.designsystem.R
import compose.project.designsystem.theme.HabitsTrackerTheme
import compose.project.home.CalendarSwitcherUiState
import compose.project.home.CalendarUiState
import compose.project.home.CalendarViewMode
import compose.project.home.DayUiModel
import compose.project.home.HabitIcon
import compose.project.home.HabitPanelUiState
import compose.project.home.HabitState
import compose.project.home.HabitTrackerUiState
import compose.project.home.HabitViewModel
import compose.project.home.MonthUiModel
import compose.project.home.WeekUiModel
import compose.project.home.mode
import compose.project.home.page
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt


object CalendarDefaults {
    val CardPadding = 16.dp
    val MonthTextSize = 20.sp
    val SpaceAfterMonth = 16.dp
    val DaysOfWeekTextSize = 14.sp
}

@Composable
fun HabitTrackerScreen(
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by habitViewModel.uiState.collectAsStateWithLifecycle()

    HabitTrackerScreenContent(
        uiState = uiState,
        onStatusSelected = { date, habitStatus -> habitViewModel.toggleHabitStatus(date, habitStatus) },
        onDayClicked = { day -> habitViewModel.onDayClicked(day) },
        onPanelDismiss = { habitViewModel.onPanelDismiss() },
        onModeChanged = { habitViewModel.onModeChanged(it) }
    )
}

@Composable
fun HabitTrackerScreenContent(
    uiState: HabitTrackerUiState,
    switcherLiquidState: LiquidState = rememberLiquidState(),
    panelLiquidState: LiquidState = rememberLiquidState(),
    onStatusSelected: (Long, HabitStatus) -> Unit = { _, _ -> },
    onDayClicked: (DayUiModel) -> Unit = { _ -> },
    onPanelDismiss: () -> Unit = {},
    onModeChanged: (CalendarViewMode) -> Unit = { _ -> },
    ) {

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        onModeChanged(pagerState.settledPage.mode())
    }


    CalendarTabFrame(
        switcherLiquidState = switcherLiquidState,
        pagerState = pagerState,
        onModeChanged = { mode ->
            scope.launch {
                pagerState.animateScrollToPage(mode.page())
            }
        }
    ) {
        CalendarWithPanel(
            uiState = uiState,
            switcherLiquidState = switcherLiquidState,
            panelLiquidState = panelLiquidState,
            onStatusSelected = onStatusSelected,
            onDayClicked = onDayClicked,
            onPanelDismiss = onPanelDismiss,
            pagerState = pagerState,
        )
    }
}

@Composable
fun CalendarWithPanel(
    uiState: HabitTrackerUiState,
    switcherLiquidState: LiquidState,
    panelLiquidState: LiquidState,
    onStatusSelected: (Long, HabitStatus) -> Unit,
    onDayClicked: (DayUiModel) -> Unit = {},
    onPanelDismiss: () -> Unit,
    pagerState: PagerState
) {
    var panelAnchor by remember { mutableStateOf<PanelAnchor?>(null) }
    var panelBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquefiable(switcherLiquidState)
            .pointerInput(panelAnchor, panelBounds) {
                if (panelAnchor == null) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val insidePanel = panelBounds?.contains(down.position) == true
                    if (!insidePanel) {
                        panelAnchor = null
                    }
                }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page.mode()) {
                CalendarViewMode.MONTH -> {
                    VerticalCalendarList(
                        calendarState = uiState.calendarState,
                        panelLiquidState = panelLiquidState,
                        onDayClick = { day, x, y ->
                            onDayClicked(day)
                            panelAnchor = PanelAnchor(
                                day = day,
                                x = x,
                                y = y
                            )
                        }
                    )
                }

                CalendarViewMode.YEAR -> {
                    YearCalendar(uiState.calendarState)
                }
            }

            CalendarPanelOverlay(
                panelAnchor = panelAnchor,
                panelState = uiState.panelState,
                panelLiquidState = panelLiquidState,
                onSelect = { day, status ->
                    onStatusSelected(day, status)
                    onPanelDismiss()
                },
                onBoundsChanged = { panelBounds = it }
            )
        }
    }
}

@Composable
fun CalendarTabFrame(
    modifier: Modifier = Modifier,
    switcherLiquidState: LiquidState,
    pagerState: PagerState,
    onModeChanged: (CalendarViewMode) -> Unit,
    content: @Composable () -> Unit,
){

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 0.dp),
    ) {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            HabitIcon(
                selectorRes = R.drawable.drink_icon_selector,
                modifier = Modifier
                    .padding(4.dp)
                    .size(35.dp),
                HabitState.DEFAULT
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
                    switcherLiquidState = switcherLiquidState,
                    pagerState = pagerState,
                    onSelectionChanged = onModeChanged
                )
            }
        }
    }
}

@Composable
fun MonthYearSwitcher(
    modifier: Modifier = Modifier,
    switcherLiquidState: LiquidState,
    pagerState: PagerState,
    onSelectionChanged: (CalendarViewMode) -> Unit
) {
    val monthText = stringResource(compose.project.home.R.string.month_switcher_month)
    val yearText = stringResource(compose.project.home.R.string.month_switcher_year)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    var monthBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var yearBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    val progress by remember(pagerState) {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, 1f)
        }
    }

    val indicatorBounds = remember(progress, monthBounds, yearBounds) {
        val m = monthBounds
        val y = yearBounds
        if (m == null || y == null) null
        else androidx.compose.ui.geometry.Rect(
            left = lerp(m.left, y.left, progress),
            top = lerp(m.top, y.top, progress),
            right = lerp(m.right, y.right, progress),
            bottom = lerp(m.bottom, y.bottom, progress)
        )
    }

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

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .liquid(switcherLiquidState) {
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
            indicatorBounds?.let { rect ->
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                rect.left.roundToInt(),
                                rect.top.roundToInt()
                            )
                        }
                        .size(
                            width = with(density) { rect.width.toDp() },
                            height = with(density) { rect.height.toDp() }
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC3C6FB6))
                )
            }

            Row {
                MonthYearButton(
                    text = monthText,
                    onClick = { onSelectionChanged(CalendarViewMode.MONTH) },
                    textMeasurer = textMeasurer,
                    indicatorBounds = indicatorBounds,
                    onBoundsChanged = { monthBounds = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                MonthYearButton(
                    text = yearText,
                    onClick = { onSelectionChanged(CalendarViewMode.YEAR) },
                    textMeasurer = textMeasurer,
                    indicatorBounds = indicatorBounds,
                    onBoundsChanged = { yearBounds = it }
                )
            }
        }
    }
}


@Composable
fun VerticalCalendarList(
    modifier: Modifier = Modifier,
    calendarState: CalendarUiState,
    onDayClick: (DayUiModel, Float, Float) -> Unit,
    monthsBefore: Int = 12,
    panelLiquidState: LiquidState
) {

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = monthsBefore)

    Box(modifier = modifier
        .fillMaxSize()
        .liquefiable(panelLiquidState)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(
                items = calendarState.months,
                key = { it.yearMonth.toString() }
            ) { month ->
                MonthBlock(
                    monthUiModel=month,
                    onDayClick = { day, x, y -> onDayClick(day, x, y)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MonthBlock(
    monthUiModel: MonthUiModel,
    onDayClick: (DayUiModel, Float, Float) -> Unit
) {

    val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    val today = remember { LocalDate.now() }

    val monthBlockCoords = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val dayCellCoords = remember(monthUiModel.yearMonth) { mutableMapOf<Long, LayoutCoordinates>() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onGloballyPositioned { coords ->
                monthBlockCoords[0] = coords
            }
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(28.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CalendarDefaults.CardPadding)
        ) {
            Text(
                text = monthUiModel.yearMonth.format(monthYearFormatter)
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
                        .format(DayOfWeek.of(if (dayOfWeek == 0) 7 else dayOfWeek))

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

            monthUiModel.weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.days.forEach { dayUiModel ->
                        val isFuture = dayUiModel?.date?.isAfter(today) ?: true

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .onGloballyPositioned { coords ->
                                    dayUiModel?.epochDay?.let { dayCellCoords[it] = coords }
                                }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (isFuture) return@clickable

                                    val coords =
                                        dayCellCoords[dayUiModel.epochDay] ?: return@clickable
                                    val anchor = coords.boundsInWindow()

                                    onDayClick(
                                        dayUiModel,
                                        anchor.center.x,
                                        anchor.top
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayUiModel != null) {
                                if (isFuture) {
                                    Text(
                                        text = dayUiModel.date.dayOfMonth.toString(),
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    DayCell(
                                        dayUiModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(dayUiModel: DayUiModel) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .background(
                if (dayUiModel.isToday)
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayUiModel.date.dayOfMonth.toString(),
                fontSize = 12.sp,
                color = if (dayUiModel.isToday) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            HabitIcon(
                selectorRes = R.drawable.drink_icon_selector,
                habitStatus = dayUiModel.habitStatus,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}


@Composable
private fun MonthYearButton(
    text: String,
    onClick: () -> Unit,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    indicatorBounds: androidx.compose.ui.geometry.Rect?,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit
) {
    val textSizeConst = 18.sp
    val density = LocalDensity.current

    val textStyle = TextStyle(
        fontSize = textSizeConst,
        fontWeight = FontWeight.Normal
    )
    val textLayout = remember(text, textStyle) {
        textMeasurer.measure(text, textStyle)
    }
    val textWidth = textLayout.size.width.toFloat()
    val textHeight = textLayout.size.height.toFloat()

    val horizontalPadding = with(density) { 16.dp.toPx() }
    val verticalPadding = with(density) { 8.dp.toPx() }

    val buttonWidth = textWidth + horizontalPadding * 2
    val buttonHeight = textHeight + verticalPadding * 2

    val paint = remember(textSizeConst, density) {
        Paint().apply {
            textSize = with(density) { textSizeConst.toPx() }
            isAntiAlias = true
        }
    }

    val textBounds = Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)
    val textHeightPx = textBounds.height().toFloat()
    val availableHeight = buttonHeight - verticalPadding * 2
    val baselineOffset = verticalPadding + (availableHeight - textHeightPx) / 2 - textBounds.top

    var buttonBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .size(
                width = with(density) { buttonWidth.toDp() },
                height = with(density) { buttonHeight.toDp() }
            )
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInParent()
                buttonBounds = bounds
                onBoundsChanged(bounds)
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .drawWithContent {
                val bounds = buttonBounds ?: return@drawWithContent

                paint.color = Color.Black.toArgb()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        text,
                        horizontalPadding,
                        baselineOffset,
                        paint
                    )
                }

                val indicator = indicatorBounds ?: return@drawWithContent

                val overlapLeft = maxOf(bounds.left, indicator.left)
                val overlapTop = maxOf(bounds.top, indicator.top)
                val overlapRight = minOf(bounds.right, indicator.right)
                val overlapBottom = minOf(bounds.bottom, indicator.bottom)

                if (overlapRight > overlapLeft && overlapBottom > overlapTop) {
                    val localLeft = overlapLeft - bounds.left
                    val localTop = overlapTop - bounds.top
                    val localRight = overlapRight - bounds.left
                    val localBottom = overlapBottom - bounds.top

                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.clipRect(
                            left = localLeft,
                            top = localTop,
                            right = localRight,
                            bottom = localBottom
                        )

                        paint.color = Color.White.toArgb()
                        canvas.nativeCanvas.drawText(
                            text,
                            horizontalPadding,
                            baselineOffset,
                            paint
                        )

                        canvas.restore()
                    }
                }
            }
    )
}


@Preview(showBackground = true)
@Composable
fun HabitTrackerScreenPreview() {
    HabitsTrackerTheme {
        HabitTrackerScreenContent(previewHabitTrackerUiState())
    }
}

fun previewHabitTrackerUiState(): HabitTrackerUiState {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()

    val months = listOf(
        generateMonthPreview(currentMonth.minusMonths(1), today),
        generateMonthPreview(currentMonth, today),
        generateMonthPreview(currentMonth.plusMonths(1), today)
    )

    val selectedDay = months[1].weeks
        .flatMap { it.days }
        .firstNotNullOf { it }

    return HabitTrackerUiState(
        switcherState = CalendarSwitcherUiState(
            selectedMode = CalendarViewMode.MONTH
        ),
        calendarState = CalendarUiState(
            selectedDate = today,
            months = months
        ),
        panelState = HabitPanelUiState.Visible(
            day = selectedDay
        )
    )
}

fun generateMonthPreview(
    yearMonth: YearMonth,
    selectedDate: LocalDate
): MonthUiModel {

    val firstDay = yearMonth.atDay(1)
    val start = firstDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val end = yearMonth.atEndOfMonth()
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    val dates = generateSequence(start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(end) }
        .toList()

    val weeks = dates.chunked(7).map { weekDates ->
        WeekUiModel(
            days = weekDates.map { date ->
                if (date.month == yearMonth.month) {
                    DayUiModel(
                        date = date,
                        epochDay = date.toEpochDay(),
                        habitStatus = when (date.dayOfMonth % 3) {
                            0 -> HabitStatus.COMPLETED
                            1 -> HabitStatus.MISSED
                            else -> HabitStatus.UNMARKED
                        },
                        isToday = date == LocalDate.now(),
                        isSelected = date == selectedDate,
                        isInCurrentMonth = true
                    )
                } else {
                    null
                }
            }
        )
    }

    return MonthUiModel(
        yearMonth = yearMonth,
        weeks = weeks
    )
}

data class PanelAnchor(
    val day: DayUiModel,
    val x: Float,
    val y: Float
)