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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import compose.project.data.model.HabitStatus

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
    LaunchedEffect(habitId) {
        habitViewModel.getHabitDaysByHabitId(habitId)
    }

    HabitTrackerScreenContent(
        habitDays = habitViewModel.habitDays,
        liquidState = liquidState,
        onDateClick = { date, habitStatus -> habitViewModel.toggleHabitStatus(habitId, date, habitStatus) }
    )
}

@Composable
fun HabitTrackerScreenContent(
    habitDays: SnapshotStateMap<Long, HabitStatus> = mutableStateMapOf(),
    liquidState: LiquidState = rememberLiquidState(),
    onDateClick: (Long, HabitStatus) -> Unit = { _, _ -> }
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
        CalendarTabFrame(liquidState = liquidState) {
            VerticalCalendarList(
                habitDays = habitDays,
                liquidState = liquidState,
                onDayClick = { day, x, y, yearMonth ->
                    panelAnchor = PanelAnchor(day, x, y, yearMonth)
                }
            )
        }

        CalendarPanelOverlay(
            panelAnchor = panelAnchor,
            habitDays = habitDays,
            onSelect = { day, status ->
                onDateClick(day, status)
                panelAnchor = null
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
    habitDays: SnapshotStateMap<Long, HabitStatus>,
    onDayClick: (Long, Float, Float, YearMonth) -> Unit,
    monthsBefore: Int = 12,
    monthsAfter: Int = 12,
    liquidState: LiquidState
) {
    val currentMonth = remember { YearMonth.from(LocalDate.now()) }
    val months = remember(currentMonth, monthsBefore, monthsAfter) {
        (-monthsBefore..monthsAfter).map { currentMonth.plusMonths(it.toLong()) }
    }

    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize().liquefiable(liquidState)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(
                items = months,
                key = { it }   // важно
            ) { yearMonth ->
                MonthBlock(
                    yearMonth = yearMonth,
                    habitDays = habitDays,
                    onDayClick = { day, x, y ->
                        onDayClick(day, x, y, yearMonth)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CalendarPanelOverlay(
    panelAnchor: PanelAnchor?,
    habitDays: SnapshotStateMap<Long, HabitStatus>,
    onSelect: (Long, HabitStatus) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit

) {
    panelAnchor?.let { anchor ->
        val density = LocalDensity.current
        val panelWidth = 176.dp
        val panelHeight = 62.dp

        val x = with(density) { (anchor.x - panelWidth.toPx() / 2f).toInt() }
        val y = with(density) { (anchor.y - panelHeight.toPx() - 6.dp.toPx()).toInt() }

        Box(
            modifier = Modifier
                .offset { IntOffset(x, y) }
                .zIndex(100f)
                .onGloballyPositioned { coords ->
                    onBoundsChanged(coords.boundsInParent())
                }
        ) {
            HabitStatePanel(
                selectedState = when (habitDays[anchor.day]) {
                    HabitStatus.COMPLETED -> HabitState.COMPLETED
                    HabitStatus.MISSED -> HabitState.MISSED
                    HabitStatus.UNMARKED -> HabitState.UNMARKED
                    else -> HabitState.DEFAULT
                },
                onSelect = { state ->
                    val status = when (state) {
                        HabitState.COMPLETED -> HabitStatus.COMPLETED
                        HabitState.MISSED -> HabitStatus.MISSED
                        HabitState.UNMARKED -> HabitStatus.UNMARKED
                        HabitState.DEFAULT -> return@HabitStatePanel
                    }
                    onSelect(anchor.day, status)
                }
            )
        }
    }
}

@Composable
fun MonthBlock(
    yearMonth: YearMonth,
    habitDays: SnapshotStateMap<Long, HabitStatus>,
    onDayClick: (day: Long, x: Float, y: Float) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7

    val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val today = remember { LocalDate.now() }

    val monthBlockCoords = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val dayCellCoords = remember(yearMonth) { mutableMapOf<Long, LayoutCoordinates>() }

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
                    week.forEach { localDate ->
                        val day = localDate?.toEpochDay()
                        val isFuture = localDate?.isAfter(today) == true

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .onGloballyPositioned { coords ->
                                    if (day != null) {
                                        dayCellCoords[day] = coords
                                    }
                                }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (day == null || isFuture) return@clickable

                                    val coords = dayCellCoords[day] ?: return@clickable
                                    val anchor = coords.boundsInWindow()

                                    onDayClick(
                                        day,
                                        anchor.center.x,
                                        anchor.top
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                if (isFuture) {
                                    Text(
                                        text = localDate.dayOfMonth.toString(),
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    DayCell(
                                        epochDay = day,
                                        dayOfMonth = localDate.dayOfMonth,
                                        isToday = day == todayEpoch,
                                        habitDays = habitDays
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
private fun DayCell(
    epochDay: Long,
    dayOfMonth: Int,
    isToday: Boolean,
    habitDays: SnapshotStateMap<Long, HabitStatus>
) {
    Box(
        modifier = Modifier
            .height(60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfMonth.toString(),
                fontSize = 12.sp,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            HabitIcon(
                selectorRes = compose.project.designsystem.R.drawable.drink_icon_selector,
                epochDay = epochDay,
                habitDays = habitDays,
                modifier = Modifier.size(35.dp)
            )
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
        HabitTrackerScreenContent()
    }
}

fun TextUnit.toDp(density: Density): Dp {
    return with(density) {
        this@toDp.toPx().toDp()
    }
}

data class PanelAnchor(
    val day: Long,
    val x: Float,
    val y: Float,
    val yearMonth: YearMonth
)