package compose.project.domain2.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

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
