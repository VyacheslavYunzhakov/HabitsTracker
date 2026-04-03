package compose.project.data

import compose.project.data.model.HabitDay

interface HabitRepository {
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
}
