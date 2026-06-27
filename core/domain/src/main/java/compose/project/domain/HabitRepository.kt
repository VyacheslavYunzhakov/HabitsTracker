package compose.project.domain

import compose.project.domain.model.Habit
import compose.project.domain.model.HabitDay
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAddedHabits(): Flow<List<Habit>>
    fun getAvailableHabits(): Flow<List<Habit>>
    suspend fun addHabit(id: Long)
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
    suspend fun removeHabit(id: Long)
}