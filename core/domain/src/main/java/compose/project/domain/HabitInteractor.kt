package compose.project.domain

import compose.project.data.model.HabitDay

interface HabitInteractor {
    fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
}
