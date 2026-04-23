package compose.project.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
