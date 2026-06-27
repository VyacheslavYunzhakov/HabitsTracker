package compose.project.data2.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.datetime.LocalDate

@Dao
interface HabitDayDao {

    @Query("SELECT * FROM habit_day_entries WHERE habit_id = :habitId ORDER BY date DESC")
    suspend fun getByHabitId(habitId: Long): List<HabitDayEntity>

    @Query("SELECT * FROM habit_day_entries WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitIdAndDate(habitId: Long, date: LocalDate): HabitDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HabitDayEntity)
}
