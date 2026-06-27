package compose.project.data2.local

import androidx.room.TypeConverter
import compose.project.domain2.model.HabitStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class HabitTrackerTypeConverters {

    @TypeConverter
    fun fromHabitStatus(value: HabitStatus): String = value.name

    @TypeConverter
    fun toHabitStatus(value: String): HabitStatus = HabitStatus.valueOf(value)
    @TypeConverter
    fun fromLocalDate(value: LocalDate): Long =
        value.toEpochDays()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate =
        LocalDate.fromEpochDays(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long =
        value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant =
        Instant.fromEpochMilliseconds(value)
}
