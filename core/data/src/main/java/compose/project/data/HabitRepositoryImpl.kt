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

    override fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAllHabits()
    }

    override suspend fun insertHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitDayDao.getByHabitId(habitId).map { it.toModel() }
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitDayDao.insert(habitDay.toEntity())
    }
}
