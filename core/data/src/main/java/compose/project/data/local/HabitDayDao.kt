package compose.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitDayDao {

    @Query("SELECT * FROM habit_day_entries WHERE habit_id = :habitId ORDER BY date DESC")
    fun getByHabitId(habitId: Long): List<HabitDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: HabitDayEntity)
}
