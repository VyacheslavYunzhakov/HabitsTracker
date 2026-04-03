package compose.project.data.local

import compose.project.data.model.HabitDay

fun HabitDayEntity.toModel(): HabitDay = HabitDay(
    habitId = habitId,
    status = status,
    date = date,
    createdAt = createdAt,
)
