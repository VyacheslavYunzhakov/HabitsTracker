package compose.project.data.local

import androidx.room.TypeConverter
import compose.project.data.model.HabitStatus
import java.time.Instant
import java.time.LocalDate

class HabitTrackerTypeConverters {

    @TypeConverter
    fun fromHabitStatus(value: HabitStatus): String = value.name

    @TypeConverter
    fun toHabitStatus(value: String): HabitStatus = HabitStatus.valueOf(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate): Long = value.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
