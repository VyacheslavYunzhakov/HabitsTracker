package compose.project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import compose.project.domain.model.Habit

@Entity(
    tableName = "habits",
    indices = [Index(value = ["iconResName"], unique = true)]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconResName: String,
    val isAdded: Boolean = false
)

fun HabitEntity.toModel(): Habit {
    return Habit(
        id = id,
        name = name,
        iconResName = iconResName,
        isAdded = isAdded
    )
}
