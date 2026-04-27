package compose.project.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var habitDao: HabitDao
    private lateinit var db: HabitTrackerDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HabitTrackerDatabase::class.java).build()
        habitDao = db.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHabits() = runBlocking {
        val habits = listOf(
            HabitEntity(name = "Drink", iconResName = "drink", isAdded = true),
            HabitEntity(name = "Sport", iconResName = "sport", isAdded = false)
        )
        habitDao.insertHabits(habits)

        val addedHabits = habitDao.getAddedHabits().first()
        assertEquals(1, addedHabits.size)
        assertEquals("Drink", addedHabits[0].name)

        val availableHabits = habitDao.getAvailableHabits().first()
        assertEquals(1, availableHabits.size)
        assertEquals("Sport", availableHabits[0].name)
    }

    @Test
    fun updateHabitStatus() = runBlocking {
        val habit = HabitEntity(id = 1, name = "Run", iconResName = "run", isAdded = false)
        habitDao.insertHabits(listOf(habit))

        habitDao.updateHabitStatus(1, true)

        val addedHabits = habitDao.getAddedHabits().first()
        assertEquals(1, addedHabits.size)
        assertTrue(addedHabits[0].isAdded)
    }

    @Test
    fun removeHabit() = runBlocking {
        val habit = HabitEntity(id = 1, name = "Read", iconResName = "read", isAdded = true)
        habitDao.insertHabits(listOf(habit))

        habitDao.removeHabit(1)

        val addedHabits = habitDao.getAddedHabits().first()
        assertTrue(addedHabits.isEmpty())
    }
}
