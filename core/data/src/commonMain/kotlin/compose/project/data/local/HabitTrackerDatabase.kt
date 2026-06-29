package compose.project.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [HabitDayEntity::class, HabitEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(HabitTrackerTypeConverters::class)
@ConstructedBy(HabitTrackerDatabaseConstructor::class)
abstract class HabitTrackerDatabase : RoomDatabase() {

    abstract fun habitDayDao(): HabitDayDao
    abstract fun habitDao(): HabitDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconResName TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconResName TEXT NOT NULL,
                        isAdded INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    INSERT INTO habits_new (name, iconResName, isAdded)
                    VALUES 
                        ('Drink', 'drink_icon_selector', 0),
                        ('Sport', 'sport_icon_selector', 0),
                        ('Cannabis', 'cannabis_icon_selector', 0),
                        ('Run', 'run_icon_selector', 0)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    INSERT INTO habits_new (name, iconResName, isAdded)
                    SELECT DISTINCT
                        'Unknown',
                        h.iconResName,
                        1
                    FROM habits h
                    LEFT JOIN habits_new n ON n.iconResName = h.iconResName
                    WHERE n.id IS NULL
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    UPDATE habits_new
                    SET isAdded = 1
                    WHERE iconResName IN (
                        SELECT iconResName
                        FROM habits
                    )
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    UPDATE habit_day_entries
                    SET habit_id = (
                        SELECT n.id
                        FROM habits h
                        JOIN habits_new n ON n.iconResName = h.iconResName
                        WHERE h.id = habit_day_entries.habit_id
                    )
                    WHERE habit_id IN (SELECT id FROM habits)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    DELETE FROM habit_day_entries
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM habit_day_entries
                        GROUP BY habit_id, date
                    )
                    """.trimIndent()
                )

                connection.execSQL("DROP TABLE habits")
                connection.execSQL("ALTER TABLE habits_new RENAME TO habits")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconResName TEXT NOT NULL,
                        isAdded INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habits_iconResName ON habits_temp(iconResName)")

                connection.execSQL(
                    """
                    INSERT INTO habits_temp (id, name, iconResName, isAdded)
                    SELECT
                        MIN(id) AS id,
                        MIN(name) AS name,
                        iconResName,
                        MAX(isAdded) AS isAdded
                    FROM habits
                    GROUP BY iconResName
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    UPDATE habit_day_entries
                    SET habit_id = (
                        SELECT t.id
                        FROM habits h
                        JOIN habits_temp t ON t.iconResName = h.iconResName
                        WHERE h.id = habit_day_entries.habit_id
                    )
                    WHERE habit_id IN (SELECT id FROM habits)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    DELETE FROM habit_day_entries
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM habit_day_entries
                        GROUP BY habit_id, date
                    )
                    """.trimIndent()
                )

                connection.execSQL("DROP TABLE habits")
                connection.execSQL("ALTER TABLE habits_temp RENAME TO habits")
            }
        }
    }
}

expect object HabitTrackerDatabaseConstructor : RoomDatabaseConstructor<HabitTrackerDatabase> {
    override fun initialize(): HabitTrackerDatabase
}