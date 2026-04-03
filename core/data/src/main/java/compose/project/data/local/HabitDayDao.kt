package compose.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitDayDao {

    @Query("SELECT * FROM habit_day_entries WHERE habit_id = :habitId ORDER BY date DESC")
    suspend fun getByHabitId(habitId: Long): List<HabitDayEntity>

    @Query("SELECT * FROM habit_day_entries WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitIdAndDate(habitId: Long, date: java.time.LocalDate): HabitDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HabitDayEntity)
}
