package compose.project.data.model

import java.sql.Date

class HabitDay(
    val status: HabitStatus,
    val date: Date
)

enum class HabitStatus {
    Good, Bad, NotChecked
}