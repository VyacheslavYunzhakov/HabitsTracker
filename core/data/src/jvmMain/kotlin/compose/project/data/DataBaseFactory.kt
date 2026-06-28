package compose.project.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import compose.project.data.local.HabitTrackerDatabase
import java.io.File

actual class DatabaseFactory {
    actual fun create(): HabitTrackerDatabase {
        val dbDir = File(System.getProperty("user.home"), ".habitsTracker")
        dbDir.mkdirs()
        val dbPath = File(dbDir, "habit_tracker.db").absolutePath

        return Room.databaseBuilder<HabitTrackerDatabase>(name = dbPath)
            .setDriver(BundledSQLiteDriver())
            .addMigrations(
                HabitTrackerDatabase.MIGRATION_1_2,
                HabitTrackerDatabase.MIGRATION_2_3,
                HabitTrackerDatabase.MIGRATION_3_4
            )
            .build()
    }
}
