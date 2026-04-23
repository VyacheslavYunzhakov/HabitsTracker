package compose.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isAdded = 1")
    fun getAddedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isAdded = 0")
    fun getAvailableHabits(): Flow<List<HabitEntity>>

    @Query("UPDATE habits SET isAdded = :isAdded WHERE id = :id")
    suspend fun updateHabitStatus(id: Long, isAdded: Boolean)

    @Query("UPDATE habits SET isAdded = 0 WHERE id = :id")
    suspend fun removeHabit(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>
}
