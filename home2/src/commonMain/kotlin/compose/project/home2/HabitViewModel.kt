package compose.project.home2

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.project.domain2.HabitInteractor
import compose.project.domain2.model.Habit
import compose.project.domain2.model.HabitDay
import compose.project.domain2.model.HabitStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.*
import kotlin.collections.associate
import kotlin.time.Clock

@Immutable
data class HabitTrackerUiState(
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null,
    val switcherState: CalendarSwitcherUiState = CalendarSwitcherUiState(),
    val calendarState: CalendarUiState = CalendarUiState(),
    val panelState: HabitPanelUiState = HabitPanelUiState.Hidden,
    val showAddHabitSelection: Boolean = false,
    val availableHabits: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

@Immutable
data class CalendarSwitcherUiState(
    val selectedMode: CalendarViewMode = CalendarViewMode.MONTH
)

enum class CalendarViewMode {
    MONTH,
    YEAR
}

@Immutable
data class CalendarUiState(
    val selectedDate: LocalDate? = null,
    val months: List<MonthUiModel> = emptyList()
)

sealed interface HabitPanelUiState {
    data object Hidden : HabitPanelUiState

    data class Visible(
        val day: DayUiModel,
        val closingStatus: HabitStatus? = null
    ) : HabitPanelUiState
}

@Immutable
data class MonthUiModel(
    val yearMonth: YearMonth,
    val weeks: List<WeekUiModel> = emptyList()
)

@Immutable
data class WeekUiModel(
    val days: List<DayUiModel?> = emptyList()
)

@Immutable
data class DayUiModel(
    val date: LocalDate,
    val epochDay: Long,
    val habitStatus: HabitStatus = HabitStatus.UNMARKED,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isInCurrentMonth: Boolean = true,
    val isClickable: Boolean = true
)

class HabitViewModel (
    private val habitInteractor: HabitInteractor
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()
    private val today = Clock.System.todayIn(timeZone)
    private val currentMonth = today.yearMonth
    private val monthsBefore = 12
    private val monthsAfter = 12

    private var habitDaysByEpochDay: Map<Long, HabitStatus> = emptyMap()

    private val initialSelectedDate = today

    private val _uiState = MutableStateFlow(HabitTrackerUiState())
    val uiState: StateFlow<HabitTrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initialMonths = withContext(Dispatchers.Default) {
                buildMonths(selectedDate = initialSelectedDate)
            }
            _uiState.update {
                it.copy(
                    calendarState = CalendarUiState(
                        selectedDate = initialSelectedDate,
                        months = initialMonths
                    )
                )
            }

            launch {
                habitInteractor.getAddedHabits().collect { habits ->
                    if (habits.isEmpty()) {
                        _uiState.update { it.copy(habits = habits, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(habits = habits) }
                        if (_uiState.value.selectedHabitId == null) {
                            onHabitSelected(habits.first().id)
                        }
                    }
                }
            }
            launch {
                habitInteractor.getAvailableHabits().collect { available ->
                    _uiState.update { it.copy(availableHabits = available) }
                }
            }
        }
    }

    fun onHabitSelected(id: Long) {
        if (_uiState.value.selectedHabitId == id) return
        selectHabit(id)
    }

    private fun selectHabit(id: Long) {
        viewModelScope.launch {
            val days = habitInteractor.getHabitDaysByHabitId(id)
            val updatedMonths = withContext(Dispatchers.Default) {
                habitDaysByEpochDay = days.associate { it.date.toEpochDays() to it.status }
                _uiState.value.calendarState.months.applyStatuses(habitDaysByEpochDay)
            }

            _uiState.update { state ->
                state.copy(
                    selectedHabitId = id,
                    calendarState = state.calendarState.copy(
                        months = updatedMonths
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun onAddHabitClicked() {
        _uiState.update { it.copy(showAddHabitSelection = true) }
    }

    fun onAddHabitDismiss() {
        _uiState.update { it.copy(showAddHabitSelection = false) }
    }

    fun addHabit(habitId: Long) {
        viewModelScope.launch {
            habitInteractor.addHabit(habitId)
            _uiState.update { it.copy(showAddHabitSelection = false) }
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            val wasSelected = _uiState.value.selectedHabitId == id
            habitInteractor.removeHabit(id)

            _uiState.update { state ->
                val newHabits = state.habits.filter { it.id != id }
                val nextId = if (wasSelected) newHabits.firstOrNull()?.id else state.selectedHabitId
                state.copy(
                    habits = newHabits,
                    selectedHabitId = nextId
                )
            }

            if (wasSelected) {
                _uiState.value.selectedHabitId?.let { newId ->
                    selectHabit(newId)
                } ?: run {
                    val clearedMonths = withContext(Dispatchers.Default) {
                        habitDaysByEpochDay = emptyMap()
                        buildMonths(_uiState.value.calendarState.selectedDate).applyStatuses(habitDaysByEpochDay)
                    }
                    _uiState.update { state ->
                        state.copy(
                            calendarState = state.calendarState.copy(
                                months = clearedMonths
                            )
                        )
                    }
                }
            }
        }
    }

    fun onDayClicked(day: DayUiModel) {
        _uiState.update { state ->
            state.copy(
                panelState = HabitPanelUiState.Visible(day = day)
            )
        }
    }

    fun toggleHabitStatus(day: DayUiModel, habitStatus: HabitStatus) {
        val date = day.date
        val currentHabitId = _uiState.value.selectedHabitId ?: return

        viewModelScope.launch {
            habitInteractor.updateHabitDay(
                HabitDay(
                    habitId = currentHabitId,
                    status = habitStatus,
                    date = date,
                    createdAt = Clock.System.now()
                )
            )

            habitDaysByEpochDay = habitDaysByEpochDay + (day.epochDay to habitStatus)

            val updatedMonths = withContext(Dispatchers.Default) {
                _uiState.value.calendarState.months.updateDay(day.epochDay, habitStatus)
            }

            _uiState.update { state ->
                state.copy(
                    calendarState = state.calendarState.copy(
                        months = updatedMonths
                    ),
                    panelState = HabitPanelUiState.Visible(day = day, closingStatus = habitStatus)
                )
            }
        }
    }

    fun onModeChanged(mode: CalendarViewMode) {
        _uiState.update { state ->
            state.copy(
                switcherState = state.switcherState.copy(
                    selectedMode = mode
                )
            )
        }
    }

    private fun buildMonths(selectedDate: LocalDate?): List<MonthUiModel> {
        return (-monthsBefore..monthsAfter).map { offset ->
            currentMonth.plus(offset.toLong(), DateTimeUnit.MONTH)
                .toMonthUiModel(selectedDate = selectedDate)
        }
    }

    private fun YearMonth.toMonthUiModel(selectedDate: LocalDate?): MonthUiModel {
        val firstDayOfMonth = firstDay
        val startDate = firstDayOfMonth.previousOrSame(DayOfWeek.MONDAY)

        val endDate = lastDay.nextOrSame(DayOfWeek.SUNDAY)
        val allDates = generateSequence(startDate) { it.plus(1, DateTimeUnit.DAY) }
            .takeWhile { it <= endDate }
            .toList()

        val weeks = allDates.chunked(7).map { weekDates ->
            WeekUiModel(
                days = weekDates.map { date ->
                    if (date.month == this.month) {
                        buildDayUiModel(
                            date = date,
                            selectedDate = selectedDate,
                            currentMonth = this
                        )
                    } else {
                        null
                    }
                }
            )
        }

        return MonthUiModel(
            yearMonth = this,
            weeks = weeks
        )
    }

    private fun buildDayUiModel(
        date: LocalDate,
        selectedDate: LocalDate?,
        currentMonth: YearMonth? = null
    ): DayUiModel {
        val epochDay = date.toEpochDays()

        return DayUiModel(
            date = date,
            epochDay = epochDay,
            habitStatus = habitDaysByEpochDay[epochDay] ?: HabitStatus.UNMARKED,
            isToday = date == today,
            isSelected = selectedDate == date,
            isInCurrentMonth = currentMonth?.let { date.yearMonth == it } ?: true,
            isClickable = true
        )
    }

    private fun List<MonthUiModel>.applyStatuses(
        statuses: Map<Long, HabitStatus>
    ): List<MonthUiModel> {
        return map { month ->
            month.copy(
                weeks = month.weeks.map { week ->
                    week.copy(
                        days = week.days.map { day ->
                            day?.let {
                                val newStatus = statuses[it.epochDay] ?: HabitStatus.UNMARKED
                                if (newStatus == it.habitStatus) it else it.copy(habitStatus = newStatus)
                            }
                        }
                    )
                }
            )
        }
    }

    private fun List<MonthUiModel>.updateDay(
        epochDay: Long,
        habitStatus: HabitStatus
    ): List<MonthUiModel> {
        val targetDate = LocalDate.fromEpochDays(epochDay)
        val targetMonth = targetDate.yearMonth

        return map { month ->
            if (month.yearMonth != targetMonth) {
                month
            } else {
                month.copy(
                    weeks = month.weeks.map { week ->
                        week.copy(
                            days = week.days.map { day ->
                                if (day?.epochDay == epochDay) {
                                    day.copy(habitStatus = habitStatus)
                                } else {
                                    day
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    fun onHideFinished() {
        _uiState.update { state ->
            state.copy(
                panelState = HabitPanelUiState.Hidden
            )
        }
    }
}

fun CalendarViewMode.page(): Int = when (this) {
    CalendarViewMode.MONTH -> 0
    CalendarViewMode.YEAR -> 1
}

fun Int.mode(): CalendarViewMode = when (this) {
    0 -> CalendarViewMode.MONTH
    else -> CalendarViewMode.YEAR
}
