package compose.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HabitDayEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(HabitTrackerTypeConverters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun habitDayDao(): HabitDayDao
}
