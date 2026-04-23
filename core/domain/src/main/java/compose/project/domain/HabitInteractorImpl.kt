package compose.project.domain

import compose.project.data.HabitRepository
import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitInteractorImpl @Inject constructor(private val habitRepository: HabitRepository): HabitInteractor {
    override fun getAddedHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAddedHabits()
    }

    override fun getAvailableHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAvailableHabits()
    }

    override suspend fun addHabit(id: Long) {
        habitRepository.addHabit(id)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitRepository.getHabitDaysByHabitId(habitId)
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitRepository.updateHabitDay(habitDay)
    }

    override suspend fun removeHabit(id: Long) {
        habitRepository.removeHabit(id)
    }
}
