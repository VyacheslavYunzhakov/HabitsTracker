package compose.project.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
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

    private val _habitDays = mutableStateMapOf<Long, HabitStatus>()
    val habitDays: SnapshotStateMap<Long, HabitStatus> = _habitDays

    fun getHabitDaysByHabitId(habitId: Long) {
        viewModelScope.launch {
            val days = habitInteractor.getHabitDaysByHabitId(habitId)
            _habitDays.clear()
            _habitDays.putAll(days.associate { it.date.toEpochDay() to it.status })
        }
    }

    fun toggleHabitStatus(habitId: Long, epochDay: Long, habitStatus: HabitStatus) {
        viewModelScope.launch {
            val date = LocalDate.ofEpochDay(epochDay)

            habitInteractor.updateHabitDay(
                HabitDay(
                    habitId = habitId,
                    status = habitStatus,
                    date = date,
                    createdAt = Instant.now()
                )
            )

            _habitDays[epochDay] = habitStatus
        }
    }
}


