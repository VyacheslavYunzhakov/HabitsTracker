package compose.project.data2.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import compose.project.domain2.model.HabitStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

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
