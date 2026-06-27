package compose.project.data2.local

import compose.project.domain2.model.HabitDay

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
