package compose.project.data

import compose.project.data.local.HabitDayDao
import compose.project.data.local.HabitDao
import compose.project.data.local.HabitEntity
import compose.project.data.local.toModel
import compose.project.data.local.toEntity
import compose.project.data.model.HabitDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDayDao: HabitDayDao,
    private val habitDao: HabitDao
) : HabitRepository {

    override fun getAddedHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAddedHabits()
    }

    override fun getAvailableHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAvailableHabits()
    }

    override suspend fun addHabit(id: Long) {
        habitDao.updateHabitStatus(id, true)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitDayDao.getByHabitId(habitId).map { it.toModel() }
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitDayDao.insert(habitDay.toEntity())
    }

    override suspend fun removeHabit(id: Long) {
        habitDao.removeHabit(id)
    }
}
