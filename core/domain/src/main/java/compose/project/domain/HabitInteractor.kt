package compose.project.domain

import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.flow.Flow

interface HabitInteractor {
    fun getAllHabits(): Flow<List<HabitEntity>>
    suspend fun insertHabit(habit: HabitEntity)
    suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay>
    suspend fun updateHabitDay(habitDay: HabitDay)
}
