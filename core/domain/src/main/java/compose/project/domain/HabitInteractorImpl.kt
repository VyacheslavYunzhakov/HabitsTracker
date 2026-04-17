package compose.project.domain

import compose.project.data.HabitRepository
import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitInteractorImpl @Inject constructor(private val habitRepository: HabitRepository): HabitInteractor {
    override fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAllHabits()
    }

    override suspend fun insertHabit(habit: HabitEntity) {
        habitRepository.insertHabit(habit)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitRepository.getHabitDaysByHabitId(habitId)
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitRepository.updateHabitDay(habitDay)
    }
}
