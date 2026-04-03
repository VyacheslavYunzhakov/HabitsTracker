package compose.project.domain

import compose.project.data.HabitRepository
import compose.project.data.model.HabitDay
import javax.inject.Inject

class HabitInteractorImpl @Inject constructor(private val habitRepository: HabitRepository): HabitInteractor {
    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitRepository.getHabitDaysByHabitId(habitId)
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitRepository.updateHabitDay(habitDay)
    }
}
