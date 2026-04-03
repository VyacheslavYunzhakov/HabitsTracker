package compose.project.home

import androidx.lifecycle.ViewModel
import compose.project.data.model.HabitDay
import compose.project.domain.HabitInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    habitInteractor: HabitInteractor
) : ViewModel() {

    fun getHabitDaysByHabitId(habitId: Long) {

    }
}


