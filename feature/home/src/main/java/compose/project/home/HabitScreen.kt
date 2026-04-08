package compose.project.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.project.data.model.HabitStatus
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

object CalendarDefaults {
    val CardPadding = 16.dp
    val MonthTextSize = 20.sp
    val SpaceAfterMonth = 16.dp
    val DaysOfWeekTextSize = 14.sp
}

@Composable
fun HabitTrackerScreen(
    habitId: Long = 1L,
    liquidState: LiquidState = rememberLiquidState(),
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by habitViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(habitId) {
        habitViewModel.getHabitDaysByHabitId(habitId)
    }

    HabitTrackerScreenContent(
        uiState = uiState,
        liquidState = liquidState,
        onStatusSelected = { date, habitStatus -> habitViewModel.toggleHabitStatus(date, habitStatus) },
        onDayClicked = { day -> habitViewModel.onDayClicked(day) },
        onPanelDismiss = { habitViewModel.onPanelDismiss() }
    )
}

@Composable
fun HabitTrackerScreenContent(
    uiState: HabitTrackerUiState,
    liquidState: LiquidState = rememberLiquidState(),
    onStatusSelected: (Long, HabitStatus) -> Unit = { _, _ -> },
    onDayClicked: (DayUiModel) -> Unit = {_ ->},
    onPanelDismiss: () -> Unit = {}
) {
    CalendarTabFrame(liquidState = liquidState) {
        CalendarWithPanel(
            uiState = uiState,
            liquidState = liquidState,
            onStatusSelected = onStatusSelected,
            onDayClicked = onDayClicked,
            onPanelDismiss = onPanelDismiss
        )
    }
}

@Composable
fun CalendarWithPanel(
    uiState: HabitTrackerUiState,
    liquidState: LiquidState,
    onStatusSelected: (Long, HabitStatus) -> Unit,
    onDayClicked: (DayUiModel) -> Unit = {},
    onPanelDismiss: () -> Unit
) {
    var panelAnchor by remember { mutableStateOf<PanelAnchor?>(null) }
    var panelBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        VerticalCalendarList(
            calendarState = uiState.calendarState,
            liquidState = liquidState,
            onDayClick = { day, x, y ->
                onDayClicked(day)
                panelAnchor = PanelAnchor(
                    day = day,
                    x = x,
                    y = y
                )
            }
        )

        CalendarPanelOverlay(
            panelAnchor = panelAnchor,
            panelState = uiState.panelState,
            onSelect = { day, status ->
                onStatusSelected(day, status)
                onPanelDismiss()
            },
            onBoundsChanged = { panelBounds = it }
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
            HabitIcon(
                selectorRes = compose.project.designsystem.R.drawable.drink_icon_selector,
                modifier = Modifier.padding(4.dp).size(35.dp),
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
fun VerticalCalendarList(
    modifier: Modifier = Modifier,
    calendarState: CalendarUiState,
    onDayClick: (DayUiModel, Float, Float) -> Unit,
    monthsBefore: Int = 12,
    liquidState: LiquidState,
) {

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = monthsBefore)

    Box(modifier = modifier.fillMaxSize().liquefiable(liquidState)) {
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

                                    val coords = dayCellCoords[dayUiModel.epochDay] ?: return@clickable
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
            .height(60.dp),
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
                selectorRes = compose.project.designsystem.R.drawable.drink_icon_selector,
                habitStatus = dayUiModel.habitStatus,
                modifier = Modifier.size(35.dp)
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
fun CalendarPanelOverlay(
    panelAnchor: PanelAnchor?,
    panelState: HabitPanelUiState,
    onSelect: (Long, HabitStatus) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit

) {
    panelAnchor?.let { anchor ->
        val density = LocalDensity.current
        val panelWidth = 176.dp
        val panelHeight = 62.dp

        val x = with(density) { (anchor.x - panelWidth.toPx() / 2f).toInt() }
        val y = with(density) { (anchor.y - panelHeight.toPx() - 6.dp.toPx()).toInt() }
        if (panelState is HabitPanelUiState.Visible) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .zIndex(100f)
                    .onGloballyPositioned { coords ->
                        onBoundsChanged(coords.boundsInParent())
                    }
                    .clickable {

                    }
            ) {
                HabitStatePanel(
                    selectedState = when (panelState.day.habitStatus) {
                        HabitStatus.COMPLETED -> HabitState.COMPLETED
                        HabitStatus.MISSED -> HabitState.MISSED
                        HabitStatus.UNMARKED -> HabitState.UNMARKED
                    },
                    onSelect = { state ->
                        val status = when (state) {
                            HabitState.COMPLETED -> HabitStatus.COMPLETED
                            HabitState.MISSED -> HabitStatus.MISSED
                            HabitState.UNMARKED -> HabitStatus.UNMARKED
                            HabitState.DEFAULT -> return@HabitStatePanel
                        }
                        onSelect(anchor.day.epochDay, status)
                    }
                )
            }
        }
    }
}

@Composable
private fun HabitStatePanel(
    selectedState: HabitState,
    onSelect: (HabitState) -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(min = 150.dp)
            .clip(PentagonBubbleShape())
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                PentagonBubbleShape()
            )
            .padding(top = 18.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                HabitState.COMPLETED,
                HabitState.MISSED,
                HabitState.UNMARKED
            ).forEach { state ->
                val isSelected = selectedState == state

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(state) },
                    contentAlignment = Alignment.Center
                ) {
                    HabitIcon(
                        selectorRes = compose.project.designsystem.R.drawable.drink_icon_selector,
                        habitState = state,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

class PentagonBubbleShape(
    private val arrowWidth: Float = 26f,
    private val arrowHeight: Float = 12f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val midX = size.width / 2f

        path.moveTo(0f, arrowHeight)
        path.lineTo(midX - arrowWidth / 2f, arrowHeight)
        path.lineTo(midX, 0f)
        path.lineTo(midX + arrowWidth / 2f, arrowHeight)
        path.lineTo(size.width, arrowHeight)
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()

        return Outline.Generic(path)
    }
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

fun TextUnit.toDp(density: Density): Dp {
    return with(density) {
        this@toDp.toPx().toDp()
    }
}

data class PanelAnchor(
    val day: DayUiModel,
    val x: Float,
    val y: Float
)