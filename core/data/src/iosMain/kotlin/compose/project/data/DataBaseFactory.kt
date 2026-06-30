package compose.project.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import compose.project.data.local.HabitTrackerDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {

    actual fun create(): HabitTrackerDatabase {
        val dbFile = documentDirectory() + "/habit_tracker.db"

        return Room.databaseBuilder<HabitTrackerDatabase>(
            name = dbFile
        )
            .setDriver(BundledSQLiteDriver())
            .addMigrations(
                HabitTrackerDatabase.MIGRATION_1_2,
                HabitTrackerDatabase.MIGRATION_2_3,
                HabitTrackerDatabase.MIGRATION_3_4
            )
            .addCallback(HabitTrackerDatabase.SEED_CALLBACK)
            .build()
    }
}

fun documentDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String