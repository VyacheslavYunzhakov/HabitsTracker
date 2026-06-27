package compose.project.data2

import compose.project.data2.local.HabitTrackerDatabase

expect class DatabaseFactory {
    fun create(): HabitTrackerDatabase
}