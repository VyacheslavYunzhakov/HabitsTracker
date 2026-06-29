package compose.project.data.local

import androidx.room.RoomDatabaseConstructor

actual object HabitTrackerDatabaseConstructor :
    RoomDatabaseConstructor<HabitTrackerDatabase> {
    actual override fun initialize(): HabitTrackerDatabase {
        TODO("Not yet implemented")
    }
}