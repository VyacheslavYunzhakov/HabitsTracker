package compose.project.data

import compose.project.data.model.HabitDay

class HabitRepositoryImpl: HabitRepository {
    override fun getHabitDays(): List<HabitDay> {
        return listOf()
    }
}