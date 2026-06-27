package compose.project.domain2.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val iconResName: String,
    val isAdded: Boolean = false
)