package compose.project.data.local

import compose.project.domain.model.HabitDay

fun HabitDayEntity.toModel(): HabitDay = HabitDay(
    habitId = habitId,
    status = status,
    date = date,
    createdAt = createdAt,
)

fun HabitDay.toEntity(): HabitDayEntity = HabitDayEntity(
    habitId = habitId,
    status = status,
    date = date,
    createdAt = createdAt,
)
