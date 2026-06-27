package compose.project.data

import compose.project.data.local.HabitTrackerDatabase

expect class DatabaseFactory {
    fun create(): HabitTrackerDatabase
}