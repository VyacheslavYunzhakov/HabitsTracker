package compose.project.domain2

import compose.project.domain2.model.Habit
import compose.project.domain2.model.HabitDay
import kotlinx.coroutines.flow.Flow

interface HabitInteractor {
    fun getAddedHabits(): Flow<List<Habit>>
    fun getAvailableHabits(): Flow<List<Habit>>
    suspend fun addHabit(id: Long)
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
    suspend fun removeHabit(id: Long)
}
