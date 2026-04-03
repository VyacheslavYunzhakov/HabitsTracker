package compose.project.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import compose.project.data.model.HabitStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "habit_day_entries",
    indices = [Index(value = ["habit_id"])],
)
data class HabitDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "habit_id")
    val habitId: Long,
    val status: HabitStatus,
    val date: LocalDate,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
)
