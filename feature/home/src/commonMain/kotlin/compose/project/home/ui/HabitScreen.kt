package compose.project.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.State
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.project.designsystem.HabitIconType
import compose.project.designsystem.theme.HabitsTrackerTheme
import compose.project.domain.model.Habit
import compose.project.domain.model.HabitStatus
import compose.project.home.CalendarUiState
import compose.project.home.CalendarViewMode
import compose.project.home.DayUiModel
import compose.project.home.HabitPanelUiState
import compose.project.home.HabitState
import compose.project.home.HabitTrackerUiState
import compose.project.home.HabitViewModel
import compose.project.home.MonthUiModel
import compose.project.home.WeekUiModel
import compose.project.home.mode
import compose.project.home.page
import habitstracker.feature.home.generated.resources.Choose_a_habit
import habitstracker.feature.home.generated.resources.Res
import habitstracker.feature.home.generated.resources.month_switcher_month
import habitstracker.feature.home.generated.resources.month_switcher_year
import habitstracker.feature.home.generated.resources.no_more_habits
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.nextOrSame
import kotlinx.datetime.plus
import kotlinx.datetime.previousOrSame
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

object CalendarDefaults {
    val CardPadding = 16.dp
    val MonthTextSize = 20.sp
    val SpaceAfterMonth = 16.dp
    val DaysOfWeekTextSize = 14.sp
}

@Composable
fun HabitTrackerScreen(
    habitViewModel: HabitViewModel = koinViewModel()
) {
    val uiState by habitViewModel.uiState.collectAsStateWithLifecycle()
    val panelState = habitViewModel.panelState.collectAsStateWithLifecycle()

    HabitTrackerScreenContent(
        uiState = uiState,
        panelState = panelState,
        onStatusSelected = { date, habitStatus -> habitViewModel.toggleHabitStatus(date, habitStatus) },
        onDayClicked = { day -> habitViewModel.onDayClicked(day) },
        onHabitSelected = { habitViewModel.onHabitSelected(it) },
        onAddHabitClicked = { habitViewModel.onAddHabitClicked() },
        onAddHabitDismiss = { habitViewModel.onAddHabitDismiss() },
        onAddHabit = { habitViewModel.addHabit(it) },
        onDeleteHabit = { habitViewModel.deleteHabit(it) },
        onHideFinished = { habitViewModel.onHideFinished() }
    )
}

@Composable
fun HabitTrackerScreenContent(
    uiState: HabitTrackerUiState,
    panelState: State<HabitPanelUiState>,
    switcherLiquidState: LiquidState = rememberLiquidState(),
    trashLiquidState: LiquidState = rememberLiquidState(),
    panelLiquidState: LiquidState = rememberLiquidState(),
    habitsListLiquidState: LiquidState = rememberLiquidState(),
    onStatusSelected: (DayUiModel, HabitStatus) -> Unit = { _, _ -> },
    onDayClicked: (DayUiModel) -> Unit = { _ -> },
    onHabitSelected: (Long) -> Unit,
    onAddHabitClicked: () -> Unit,
    onAddHabitDismiss: () -> Unit,
    onAddHabit: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onHideFinished: () -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.habits.isEmpty()) {
        EmptyHabitScreen(onAddHabitClicked)
    } else {
        val pagerState = rememberPagerState(pageCount = { 2 })
        val scope = rememberCoroutineScope()

        CalendarTabFrame(
            switcherLiquidState = switcherLiquidState,
            trashLiquidState = trashLiquidState,
            selectedHabitId = uiState.selectedHabitId,
            pagerState = pagerState,
            onModeChanged = { mode ->
                scope.launch {
                    pagerState.animateScrollToPage(mode.page())
                }
            },
            onDeleteHabit = onDeleteHabit,
            content = {
                val selectedHabit = remember(uiState.habits, uiState.selectedHabitId) {
                    uiState.habits.find { it.id == uiState.selectedHabitId }
                }
                val iconType = remember(selectedHabit?.iconResName) {
                    HabitIconType.fromName(selectedHabit?.iconResName ?: "drink_icon_selector")
                }

                CalendarWithPanel(
                    calendarState = uiState.calendarState,
                    panelState = panelState,
                    switcherLiquidState = switcherLiquidState,
                    trashLiquidState = trashLiquidState,
                    panelLiquidState = panelLiquidState,
                    habitsListLiquidState = habitsListLiquidState,
                    selectedHabitId = uiState.selectedHabitId,
                    habits = uiState.habits,
                    onStatusSelected = onStatusSelected,
                    onDayClicked = onDayClicked,
                    pagerState = pagerState,
                    iconType = iconType,
                    onHideFinished = onHideFinished,
                    onHabitSelected = onHabitSelected,
                    onAddHabitClicked = onAddHabitClicked
                )
            }
        )
    }

    if (uiState.showAddHabitSelection) {
        IconSelectionDialog(
            availableHabits = uiState.availableHabits,
            onDismiss = onAddHabitDismiss,
            onHabitSelected = onAddHabit
        )
    }
}

@Composable
fun EmptyHabitScreen(onAddHabitClicked: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(150.dp)
                .clickable { onAddHabitClicked() },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    fontSize = 80.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
fun IconSelectionDialog(
    availableHabits: List<Habit>,
    onDismiss: () -> Unit,
    onHabitSelected: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.Choose_a_habit),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (availableHabits.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.no_more_habits),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        availableHabits.forEach { habit ->
                            val iconType = HabitIconType.fromName(habit.iconResName)
                            Surface(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { onHabitSelected(habit.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    HabitIcon(
                                        iconType = iconType,
                                        modifier = Modifier.size(40.dp),
                                        habitState = HabitState.DEFAULT
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
fun TrashCanIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trashLiquidState: LiquidState
) {
    Surface(
        modifier = modifier
            .size(45.dp)
            .liquid(trashLiquidState) {
                shape = RoundedCornerShape(100)
                refraction = 0.5f
                curve = 0.5f
                edge = 0.1f
                tint = Color.White.copy(alpha = 0.2f)
                saturation = 1.5f
                dispersion = 0.25f
            }
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            HabitIcon(
                iconType = HabitIconType.TRASH,
                modifier = Modifier.size(20.dp),
                habitState = HabitState.DEFAULT
            )
        }
    }
}

@Composable
fun CalendarWithPanel(
    calendarState: CalendarUiState,
    panelState: State<HabitPanelUiState>,
    switcherLiquidState: LiquidState,
    trashLiquidState: LiquidState,
    panelLiquidState: LiquidState,
    onStatusSelected: (DayUiModel, HabitStatus) -> Unit,
    onDayClicked: (DayUiModel) -> Unit = {},
    pagerState: PagerState,
    iconType: HabitIconType,
    onHideFinished: () -> Unit,
    habitsListLiquidState: LiquidState,
    onHabitSelected: (Long) -> Unit,
    onAddHabitClicked: () -> Unit,
    selectedHabitId: Long?,
    habits: List<Habit>
) {
    var panelAnchor by remember { mutableStateOf<PanelAnchor?>(null) }
    val panelBoundsRef = remember { BoundsRef() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquefiable(switcherLiquidState)
            .pointerInput(panelAnchor) {
                if (panelAnchor == null) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val insidePanel = panelBoundsRef.value?.contains(down.position) == true
                    if (!insidePanel) {
                        panelAnchor = null
                    }
                }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .liquefiable(trashLiquidState),
            beyondViewportPageCount = 1
        ) { page ->
            when (page.mode()) {
                CalendarViewMode.MONTH -> {
                    VerticalCalendarList(
                        calendarState = calendarState,
                        panelLiquidState = panelLiquidState,
                        habitsListLiquidState = habitsListLiquidState,
                        iconType = iconType,
                        onDayClick = { day, x, y ->
                            onDayClicked(day)
                            panelAnchor = PanelAnchor(day = day, x = x, y = y)
                        },
                        onHabitSelected = onHabitSelected,
                        onAddHabitClicked = onAddHabitClicked,
                        selectedHabitId = selectedHabitId,
                        habits = habits
                    )
                }

                CalendarViewMode.YEAR -> {
                    YearCalendar(calendarState)
                }
            }
        }

        CalendarPanelOverlay(
            panelAnchor = panelAnchor,
            panelState = panelState,
            panelLiquidState = panelLiquidState,
            onSelect = { day, status ->
                onStatusSelected(day, status)
            },
            onBoundsChanged = { newBounds ->
                panelBoundsRef.value = newBounds
            },
            iconType = iconType,
            onHideFinished = {
                panelAnchor = null
                onHideFinished()
            }
        )
    }
}

@Composable
fun CalendarTabFrame(
    modifier: Modifier = Modifier,
    selectedHabitId: Long?,
    switcherLiquidState: LiquidState,
    trashLiquidState: LiquidState,
    pagerState: PagerState,
    onModeChanged: (CalendarViewMode) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    content: @Composable () -> Unit,
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 0.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 22.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()

                selectedHabitId?.let { id ->
                    TrashCanIcon(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                        onClick = { onDeleteHabit(id) },
                        trashLiquidState = trashLiquidState
                    )
                }

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
private fun HabitListPanel(
    habits: List<Habit>,
    selectedHabitId: Long?,
    onHabitSelected: (Long) -> Unit,
    onAddHabitClicked: () -> Unit,
    modifier: Modifier = Modifier,
    habitsListLiquidState: LiquidState
) {
    val itemSize = 43.dp
    val maxVisibleItems = 4
    val verticalArrangement = 4.dp
    Column(
        modifier = modifier
            .liquid(habitsListLiquidState) {
                shape = RoundedCornerShape(40)
                refraction = 0.5f
                curve = 0.5f
                edge = 0.1f
                tint = Color.White.copy(alpha = 0.2f)
                saturation = 1.5f
                dispersion = 0.25f
                frost = 2.dp
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .width(itemSize)
                .heightIn(max = itemSize * maxVisibleItems + verticalArrangement * (maxVisibleItems - 1)),
            verticalArrangement = Arrangement.spacedBy(verticalArrangement)
        ) {
            items(habits) { habit ->
                val isSelected = habit.id == selectedHabitId
                val iconType = HabitIconType.fromName(habit.iconResName)
                Surface(
                    modifier = Modifier
                        .size(itemSize)
                        .clickable { onHabitSelected(habit.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        HabitIcon(
                            iconType = iconType,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(35.dp),
                            HabitState.DEFAULT
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Кнопка добавления привычки
        Surface(
            modifier = Modifier
                .size(itemSize)
                .clickable { onAddHabitClicked() },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
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
    val monthText = stringResource(Res.string.month_switcher_month)
    val yearText = stringResource(Res.string.month_switcher_year)
    val textMeasurer = rememberTextMeasurer()

    var monthBounds by remember { mutableStateOf<Rect?>(null) }
    var yearBounds by remember { mutableStateOf<Rect?>(null) }

    val indicatorBoundsState = remember(pagerState) {
        derivedStateOf {
            val m = monthBounds
            val y = yearBounds
            val progress = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, 1f)
            if (m == null || y == null) null
            else Rect(
                left = lerp(m.left, y.left, progress),
                top = lerp(m.top, y.top, progress),
                right = lerp(m.right, y.right, progress),
                bottom = lerp(m.bottom, y.bottom, progress)
            )
        }
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
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val rect = indicatorBoundsState.value ?: return@drawBehind
                        val cornerRadius = 20.dp.toPx()
                        drawRoundRect(
                            color = Color(0xCC3C6FB6),
                            topLeft = Offset(rect.left, rect.top),
                            size = Size(rect.width, rect.height),
                            cornerRadius = CornerRadius(cornerRadius)
                        )
                    }
            )

            Row {
                MonthYearButton(
                    text = monthText,
                    onClick = { onSelectionChanged(CalendarViewMode.MONTH) },
                    textMeasurer = textMeasurer,
                    indicatorBoundsState = indicatorBoundsState,
                    onBoundsChanged = { monthBounds = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                MonthYearButton(
                    text = yearText,
                    onClick = { onSelectionChanged(CalendarViewMode.YEAR) },
                    textMeasurer = textMeasurer,
                    indicatorBoundsState = indicatorBoundsState,
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
    panelLiquidState: LiquidState,
    iconType: HabitIconType,
    habitsListLiquidState: LiquidState,
    onHabitSelected: (Long) -> Unit,
    onAddHabitClicked: () -> Unit,
    selectedHabitId: Long?,
    habits: List<Habit>
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = monthsBefore)

    Box(
        modifier = modifier
            .fillMaxSize()
            .liquefiable(panelLiquidState)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .liquefiable(habitsListLiquidState),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(
                items = calendarState.months,
                key = { it.yearMonth.toString() }
            ) { month ->
                MonthBlock(
                    monthUiModel = month,
                    onDayClick = { day, x, y -> onDayClick(day, x, y) },
                    iconType = iconType
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        HabitListPanel(
            habits = habits,
            selectedHabitId = selectedHabitId,
            onHabitSelected = onHabitSelected,
            onAddHabitClicked = onAddHabitClicked,
            habitsListLiquidState = habitsListLiquidState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 22.dp + 150.dp)
        )
    }
}

@Composable
fun MonthBlock(
    monthUiModel: MonthUiModel,
    onDayClick: (DayUiModel, Float, Float) -> Unit,
    iconType: HabitIconType
) {
    val daysOfWeek = remember {
        listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        ).map { it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercaseChar() } }
    }

    val monthYearText = remember(monthUiModel.yearMonth) {
        val ym = monthUiModel.yearMonth
        "${ym.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }} ${ym.year}"
    }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    var monthPosition by remember { mutableStateOf<Offset?>(null) }
    var gridSize by remember { mutableStateOf<IntSize?>(null) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                text = monthYearText,
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
                daysOfWeek.forEach { dayName ->
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        monthPosition = coords.positionInWindow()
                        gridSize = coords.size
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    monthUiModel.weeks.forEachIndexed { weekIndex, week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            week.days.forEachIndexed { dayIndex, dayUiModel ->
                                val isFuture = dayUiModel?.date?.let { it > today } ?: true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            if (isFuture) return@clickable
                                            val gridWidth = gridSize?.width
                                            val tempX = monthPosition?.x
                                            val tempY = monthPosition?.y
                                            if (tempX!= null && tempY != null && gridWidth != null) {
                                                val cellHeight = with(density) { 60.dp.toPx() }
                                                val cellWidth = gridWidth/7f

                                                val x = tempX + dayIndex * cellWidth + cellWidth / 2f
                                                val y = tempY + weekIndex * cellHeight + cellHeight / 2f
                                                onDayClick(dayUiModel, x, y)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayUiModel != null) {
                                        if (isFuture) {
                                            Text(
                                                text = dayUiModel.date.day.toString(),
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            DayCell(
                                                dayUiModel = dayUiModel,
                                                iconType = iconType
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(dayUiModel: DayUiModel, iconType: HabitIconType) {
    Box(
        modifier = Modifier.height(60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayUiModel.date.day.toString(),
                fontSize = 12.sp,
                color = if (dayUiModel.isToday) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            HabitIcon(
                iconType = iconType,
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
    indicatorBoundsState: State<Rect?>,
    onBoundsChanged: (Rect) -> Unit
) {
    val density = LocalDensity.current
    val textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
    val textLayout = remember(text, textStyle) { textMeasurer.measure(text, textStyle) }
    val textWidth = textLayout.size.width.toFloat()

    val hPadPx = with(density) { 16.dp.toPx() }
    val vPadPx = with(density) { 8.dp.toPx() }
    val buttonWidth = textWidth + hPadPx * 2
    val buttonHeight = textLayout.size.height.toFloat() + vPadPx * 2

    var buttonLeft by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .size(
                width = with(density) { buttonWidth.toDp() },
                height = with(density) { buttonHeight.toDp() }
            )
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInParent()
                buttonLeft = bounds.left
                onBoundsChanged(bounds)
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawText(
                textLayoutResult = textLayout,
                color = Color.Black,
                topLeft = Offset(hPadPx, vPadPx)
            )

            val indicator = indicatorBoundsState.value ?: return@Canvas
            val bLeft = buttonLeft
            val clipLeft = maxOf(hPadPx, indicator.left - bLeft)
            val clipRight = minOf(hPadPx + textWidth, indicator.right - bLeft)

            if (clipRight > clipLeft) {
                clipRect(left = clipLeft, top = 0f, right = clipRight, bottom = size.height) {
                    drawText(
                        textLayoutResult = textLayout,
                        color = Color.White,
                        topLeft = Offset(hPadPx, vPadPx)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun HabitTrackerScreenPreview() {
    HabitsTrackerTheme {
        HabitTrackerScreenContent(
            uiState = previewHabitTrackerUiState(),
            panelState = remember { mutableStateOf(HabitPanelUiState.Hidden) },
            onHabitSelected = {},
            onAddHabitClicked = {},
            onAddHabitDismiss = {},
            onAddHabit = {},
            onDeleteHabit = {},
            onHideFinished = {}
        )
    }
}

fun previewHabitTrackerUiState(): HabitTrackerUiState {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentMonth = today.yearMonth

    val months = listOf(
        generateMonthPreview(currentMonth.plus(-1L, DateTimeUnit.MONTH), today),
        generateMonthPreview(currentMonth, today),
        generateMonthPreview(currentMonth.plus(1L, DateTimeUnit.MONTH), today)
    )

    return HabitTrackerUiState(
        isLoading = false,
        habits = listOf(
            Habit(id = 1, name = "Drink", iconResName = "drink_icon_selector"),
            Habit(id = 2, name = "Run", iconResName = "run_icon_selector")
        ),
        selectedHabitId = 1,
        calendarState = CalendarUiState(
            selectedDate = today,
            months = months
        ),
        availableHabits = emptyList(),
        showAddHabitSelection = false
    )
}

fun generateMonthPreview(
    yearMonth: YearMonth,
    selectedDate: LocalDate
): MonthUiModel {
    val firstDay = yearMonth.firstDay
    val start = firstDay.previousOrSame(DayOfWeek.MONDAY)
    val end = yearMonth.lastDay.nextOrSame(DayOfWeek.SUNDAY)

    val dates = generateSequence(start) { it.plus(1, DateTimeUnit.DAY) }
        .takeWhile { it <= end }
        .toList()

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val weeks = dates.chunked(7).map { weekDates ->
        WeekUiModel(
            days = weekDates.map { date ->
                if (date.month == yearMonth.month) {
                    DayUiModel(
                        date = date,
                        epochDay = date.toEpochDays(),
                        habitStatus = when (date.day % 3) {
                            0 -> HabitStatus.COMPLETED
                            1 -> HabitStatus.MISSED
                            else -> HabitStatus.UNMARKED
                        },
                        isToday = date == today,
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

private class BoundsRef(var value: Rect? = null)
