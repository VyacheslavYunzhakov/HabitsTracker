package compose.project.data

import compose.project.data.model.HabitDay

interface HabitRepository {
    fun getHabitDays() : List<HabitDay>
}