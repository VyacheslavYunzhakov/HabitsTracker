package compose.project.home

import androidx.lifecycle.ViewModel
import compose.project.domain.HabitInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import compose.project.data.model.HabitDay
import compose.project.data.model.HabitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitInteractor: HabitInteractor
) : ViewModel() {

    private val _habitDays = MutableStateFlow<Map<LocalDate, HabitStatus>>(emptyMap())
    val habitDays: StateFlow<Map<LocalDate, HabitStatus>> = _habitDays.asStateFlow()

    fun getHabitDaysByHabitId(habitId: Long) {
        viewModelScope.launch {
            val days = habitInteractor.getHabitDaysByHabitId(habitId)
            _habitDays.value = days.associate { it.date to it.status }
        }
    }

    fun toggleHabitStatus(habitId: Long, date: LocalDate) {
        viewModelScope.launch {
            val currentStatus = _habitDays.value[date] ?: HabitStatus.UNMARKED
            val nextStatus = when (currentStatus) {
                HabitStatus.UNMARKED -> HabitStatus.MISSED
                HabitStatus.MISSED -> HabitStatus.COMPLETED
                HabitStatus.COMPLETED -> HabitStatus.UNMARKED
            }

            val habitDay = HabitDay(
                habitId = habitId,
                status = nextStatus,
                date = date,
                createdAt = Instant.now()
            )
            habitInteractor.updateHabitDay(habitDay)
            getHabitDaysByHabitId(habitId)
        }
    }
}


