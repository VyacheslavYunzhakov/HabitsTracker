package compose.project.data

import compose.project.data.local.HabitDayDao
import compose.project.data.local.HabitDao
import compose.project.data.local.HabitEntity
import compose.project.data.local.toModel
import compose.project.data.local.toEntity
import compose.project.domain.HabitRepository
import compose.project.domain.model.Habit
import compose.project.domain.model.HabitDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HabitRepositoryImpl (
    private val habitDayDao: HabitDayDao,
    private val habitDao: HabitDao
) : HabitRepository {

    override fun getAddedHabits(): Flow<List<Habit>> {
        return habitDao.getAddedHabits()
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    override fun getAvailableHabits(): Flow<List<Habit>> {
        return habitDao.getAvailableHabits().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun addHabit(id: Long) {
        habitDao.updateHabitStatus(id, true)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> = withContext(Dispatchers.Default) {
        habitDayDao.getByHabitId(habitId).map { it.toModel() }
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) = withContext(Dispatchers.Default) {
        val entity = habitDay.toEntity()
        habitDayDao.insert(entity)
    }

    override suspend fun removeHabit(id: Long) {
        habitDao.removeHabit(id)
    }
}
