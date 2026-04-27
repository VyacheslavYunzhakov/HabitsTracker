package compose.project.domain

import compose.project.data.HabitRepository
import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import compose.project.data.model.HabitStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class HabitInteractorImplTest {

    private val habitRepository: HabitRepository = mockk()
    private val interactor = HabitInteractorImpl(habitRepository)

    @Test
    fun `getAddedHabits should delegate to repository`() = runTest {
        val habits = listOf(HabitEntity(id = 1, name = "Habit 1", iconResName = "icon1"))
        every { habitRepository.getAddedHabits() } returns flowOf(habits)

        interactor.getAddedHabits().collect {
            assertEquals(habits, it)
        }
    }

    @Test
    fun `addHabit should call repository addHabit`() = runTest {
        val habitId = 1L
        coEvery { habitRepository.addHabit(habitId) } returns Unit

        interactor.addHabit(habitId)

        coVerify { habitRepository.addHabit(habitId) }
    }

    @Test
    fun `getHabitDaysByHabitId should delegate to repository`() = runTest {
        val habitId = 1L
        val days = listOf(HabitDay(habitId, HabitStatus.COMPLETED, LocalDate.now(), Instant.now()))
        coEvery { habitRepository.getHabitDaysByHabitId(habitId) } returns days

        val result = interactor.getHabitDaysByHabitId(habitId)

        assertEquals(days, result)
    }

    @Test
    fun `updateHabitDay should call repository updateHabitDay`() = runTest {
        val day = HabitDay(1L, HabitStatus.COMPLETED, LocalDate.now(), Instant.now())
        coEvery { habitRepository.updateHabitDay(day) } returns Unit

        interactor.updateHabitDay(day)

        coVerify { habitRepository.updateHabitDay(day) }
    }

    @Test
    fun `removeHabit should call repository removeHabit`() = runTest {
        val habitId = 1L
        coEvery { habitRepository.removeHabit(habitId) } returns Unit

        interactor.removeHabit(habitId)

        coVerify { habitRepository.removeHabit(habitId) }
    }
}
