package compose.project.domain

import compose.project.data.HabitRepository
import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HabitInteractorImpl @Inject constructor(private val habitRepository: HabitRepository): HabitInteractor {
    override fun getAddedHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAddedHabits().flowOn(Dispatchers.Default)
    }

    override fun getAvailableHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAvailableHabits().flowOn(Dispatchers.Default)
    }

    override suspend fun addHabit(id: Long) = withContext(Dispatchers.Default) {
        habitRepository.addHabit(id)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> = withContext(Dispatchers.Default) {
        habitRepository.getHabitDaysByHabitId(habitId)
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) = withContext(Dispatchers.Default) {
        habitRepository.updateHabitDay(habitDay)
    }

    override suspend fun removeHabit(id: Long) = withContext(Dispatchers.Default) {
        habitRepository.removeHabit(id)
    }
}
