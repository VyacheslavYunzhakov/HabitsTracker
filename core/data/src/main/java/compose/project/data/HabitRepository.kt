package compose.project.data

import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAddedHabits(): Flow<List<HabitEntity>>
    fun getAvailableHabits(): Flow<List<HabitEntity>>
    suspend fun addHabit(id: Long)
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
    suspend fun removeHabit(id: Long)
}
