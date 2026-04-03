package compose.project.data

import compose.project.data.local.HabitDayDao
import compose.project.data.local.toModel
import compose.project.data.local.toEntity
import compose.project.data.model.HabitDay
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDayDao: HabitDayDao,
) : HabitRepository {

    override suspend fun getHabitDaysByHabitId(habitId: Long): List<HabitDay> {
        return habitDayDao.getByHabitId(habitId).map { it.toModel() }
    }

    override suspend fun updateHabitDay(habitDay: HabitDay) {
        habitDayDao.insert(habitDay.toEntity())
    }
}
