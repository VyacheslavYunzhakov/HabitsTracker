package compose.project.data

import androidx.room.Room
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
            .build()
    }
}

fun documentDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String