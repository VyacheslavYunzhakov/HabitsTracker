package compose.project.data.model

import java.time.Instant
import java.time.LocalDate

data class HabitDay(
    val habitId: Long,
    val status: HabitStatus,
    val date: LocalDate,
    val createdAt: Instant,
)

enum class HabitStatus {
    COMPLETED,
    MISSED,
    UNMARKED
}
