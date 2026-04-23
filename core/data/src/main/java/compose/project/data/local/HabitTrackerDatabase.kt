package compose.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitDayEntity::class, HabitEntity::class],
    version = 4,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN isAdded INTEGER NOT NULL DEFAULT 0")

                db.execSQL("""
                    CREATE TABLE habits_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconResName TEXT NOT NULL,
                        isAdded INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                val baseHabits = listOf(
                    "Drink" to "drink_icon_selector",
                    "Sport" to "sport_icon_selector",
                    "Cannabis" to "cannabis_icon_selector",
                    "Run" to "run_icon_selector"
                )
                for ((name, icon) in baseHabits) {
                    db.execSQL("INSERT INTO habits_new (name, iconResName, isAdded) VALUES ('$name', '$icon', 0)")
                }

                val cursor = db.query("SELECT id, iconResName FROM habits")
                val oldHabits = mutableListOf<Pair<Long, String>>()
                while (cursor.moveToNext()) {
                    oldHabits.add(cursor.getLong(0) to cursor.getString(1))
                }
                cursor.close()

                for ((oldId, iconName) in oldHabits) {
                    val checkCursor = db.query("SELECT id FROM habits_new WHERE iconResName = '$iconName'")
                    val newId: Long
                    if (checkCursor.moveToFirst()) {
                        newId = checkCursor.getLong(0)
                        db.execSQL("UPDATE habits_new SET isAdded = 1 WHERE id = $newId")
                    } else {
                        db.execSQL("INSERT INTO habits_new (name, iconResName, isAdded) VALUES ('Unknown', '$iconName', 1)")
                        val lastIdCursor = db.query("SELECT last_insert_rowid()")
                        lastIdCursor.moveToFirst()
                        newId = lastIdCursor.getLong(0)
                        lastIdCursor.close()
                    }
                    checkCursor.close()

                    db.execSQL("DELETE FROM habit_day_entries WHERE habit_id NOT IN (SELECT id FROM habits)")
                    db.execSQL("UPDATE habit_day_entries SET habit_id = $newId WHERE habit_id = $oldId")
                }

                db.execSQL("""
                    DELETE FROM habit_day_entries 
                    WHERE id NOT IN (
                        SELECT MIN(id) 
                        FROM habit_day_entries 
                        GROUP BY habit_id, date
                    )
                """.trimIndent())

                db.execSQL("DROP TABLE habits")
                db.execSQL("ALTER TABLE habits_new RENAME TO habits")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE habits_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconResName TEXT NOT NULL,
                        isAdded INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX index_habits_iconResName ON habits_temp(iconResName)")

                db.execSQL("""
                    INSERT INTO habits_temp (id, name, iconResName, isAdded)
                    SELECT MIN(id), name, iconResName, MAX(isAdded)
                    FROM habits
                    GROUP BY iconResName
                """.trimIndent())

                db.execSQL("DELETE FROM habit_day_entries WHERE habit_id NOT IN (SELECT id FROM habits)")

                db.execSQL("""
                    UPDATE habit_day_entries
                    SET habit_id = (
                        SELECT id FROM habits_temp 
                        WHERE iconResName = (SELECT iconResName FROM habits WHERE habits.id = habit_day_entries.habit_id)
                    )
                    WHERE habit_id IN (SELECT id FROM habits)
                """.trimIndent())

                db.execSQL("DROP TABLE habits")
                db.execSQL("ALTER TABLE habits_temp RENAME TO habits")
            }
        }
    }
}
