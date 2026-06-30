package compose.project.data

import android.content.Context
import androidx.room.Room
import compose.project.data.local.HabitTrackerDatabase
import kotlin.jvm.java

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): HabitTrackerDatabase {
        return Room.databaseBuilder(
            context,
            HabitTrackerDatabase::class.java,
            "habit_tracker.db"
        )
            .addMigrations(
                HabitTrackerDatabase.MIGRATION_1_2,
                HabitTrackerDatabase.MIGRATION_2_3,
                HabitTrackerDatabase.MIGRATION_3_4
            )
            .addCallback(HabitTrackerDatabase.SEED_CALLBACK)
            .build()
    }
}