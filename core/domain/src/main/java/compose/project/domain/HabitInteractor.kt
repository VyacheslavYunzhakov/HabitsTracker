package compose.project.domain

import compose.project.data.model.HabitDay

interface HabitInteractor {
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
}
