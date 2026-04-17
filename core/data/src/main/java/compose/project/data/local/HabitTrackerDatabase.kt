package compose.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitDayEntity::class, HabitEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(HabitTrackerTypeConverters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun habitDayDao(): HabitDayDao
    abstract fun habitDao(): HabitDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `habits` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `iconResName` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
