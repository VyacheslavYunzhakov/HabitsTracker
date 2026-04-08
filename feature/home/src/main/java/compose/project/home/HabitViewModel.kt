package compose.project.home

import androidx.lifecycle.ViewModel
import compose.project.domain.HabitInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import compose.project.data.model.HabitDay
import compose.project.data.model.HabitStatus
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.YearMonth

@Immutable
data class HabitTrackerUiState(
    val switcherState: CalendarSwitcherUiState = CalendarSwitcherUiState(),
    val calendarState: CalendarUiState = CalendarUiState(),
    val panelState: HabitPanelUiState = HabitPanelUiState.Hidden
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
        val day: DayUiModel
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

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitInteractor: HabitInteractor
) : ViewModel() {

    private val currentMonth = YearMonth.now()
    private val monthsBefore = 12
    private val monthsAfter = 12

    private var currentHabitId: Long? = null
    private var habitDaysByEpochDay: Map<Long, HabitStatus> = emptyMap()

    private val _uiState = MutableStateFlow(HabitTrackerUiState())
    val uiState: StateFlow<HabitTrackerUiState> = _uiState.asStateFlow()

    fun getHabitDaysByHabitId(habitId: Long) {
        currentHabitId = habitId

        viewModelScope.launch {
            val days = habitInteractor.getHabitDaysByHabitId(habitId)
            habitDaysByEpochDay = days.associate { it.date.toEpochDay() to it.status }
            rebuildCalendar()
        }
    }

    fun onDayClicked(day: DayUiModel) {

        _uiState.update { state ->
            state.copy(
                panelState = HabitPanelUiState.Visible(
                    day = day
                )
            )
        }
    }

    fun onPanelDismiss() {
        _uiState.update { state ->
            state.copy(panelState = HabitPanelUiState.Hidden)
        }
    }

    fun toggleHabitStatus(epochDay: Long, habitStatus: HabitStatus) {
        val habitId = currentHabitId ?: return
        val date = LocalDate.ofEpochDay(epochDay)

        viewModelScope.launch {
            habitInteractor.updateHabitDay(
                HabitDay(
                    habitId = habitId,
                    status = habitStatus,
                    date = date,
                    createdAt = Instant.now()
                )
            )

            habitDaysByEpochDay = habitDaysByEpochDay + (epochDay to habitStatus)

            _uiState.update { state ->
                val selectedDate = state.calendarState.selectedDate ?: date
                state.copy(
                    calendarState = state.calendarState.copy(
                        selectedDate = selectedDate,
                        months = buildMonths(selectedDate = selectedDate)
                    ),
                    panelState = HabitPanelUiState.Hidden
                )
            }
        }
    }

    private fun rebuildCalendar() {
        val selectedDate = _uiState.value.calendarState.selectedDate
        _uiState.update { state ->
            state.copy(
                calendarState = state.calendarState.copy(
                    months = buildMonths(selectedDate = selectedDate)
                ),
                panelState = when (val panel = state.panelState) {
                    HabitPanelUiState.Hidden -> panel
                    is HabitPanelUiState.Visible -> {
                        HabitPanelUiState.Visible(
                            day = buildDayUiModel(
                                date = panel.day.date,
                                selectedDate = selectedDate ?: panel.day.date
                            )
                        )
                    }
                }
            )
        }
    }

    private fun buildMonths(selectedDate: LocalDate?): List<MonthUiModel> {
        return (-monthsBefore..monthsAfter).map { offset ->
            currentMonth.plusMonths(offset.toLong())
                .toMonthUiModel(selectedDate = selectedDate)
        }
    }

    private fun YearMonth.toMonthUiModel(selectedDate: LocalDate?): MonthUiModel {
        val firstDayOfMonth = atDay(1)
        val startDate = firstDayOfMonth.with(
            java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )

        val endDate = atEndOfMonth().with(
            java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
        )

        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
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
        val epochDay = date.toEpochDay()

        return DayUiModel(
            date = date,
            epochDay = epochDay,
            habitStatus = habitDaysByEpochDay[epochDay] ?: HabitStatus.UNMARKED,
            isToday = date == LocalDate.now(),
            isSelected = selectedDate == date,
            isInCurrentMonth = currentMonth?.let { date.yearMonth == it } ?: true,
            isClickable = true
        )
    }

    private val LocalDate.yearMonth: YearMonth
        get() = YearMonth.from(this)
}
