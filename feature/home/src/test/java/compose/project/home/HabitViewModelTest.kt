package compose.project.home

import app.cash.turbine.test
import compose.project.data.local.HabitEntity
import compose.project.data.model.HabitDay
import compose.project.data.model.HabitStatus
import compose.project.domain.HabitInteractor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {

    private val habitInteractor: HabitInteractor = mockk(relaxed = true)
    private lateinit var viewModel: HabitViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load habits and available habits`() = runTest {
        val habits = listOf(HabitEntity(id = 1, name = "Habit 1", iconResName = "icon1", isAdded = true))
        val available = listOf(HabitEntity(id = 2, name = "Habit 2", iconResName = "icon2", isAdded = false))

        every { habitInteractor.getAddedHabits() } returns flowOf(habits)
        every { habitInteractor.getAvailableHabits() } returns flowOf(available)
        coEvery { habitInteractor.getHabitDaysByHabitId(1) } returns emptyList()

        viewModel = HabitViewModel(habitInteractor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(habits, state.habits)
            assertEquals(available, state.availableHabits)
            assertEquals(1L, state.selectedHabitId)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `onHabitSelected should update selectedHabitId and load habit days`() = runTest {
        val habitId = 1L
        val habitDays = listOf(
            HabitDay(
                habitId = habitId,
                status = HabitStatus.COMPLETED,
                date = LocalDate.now(),
                createdAt = Instant.now()
            )
        )

        every { habitInteractor.getAddedHabits() } returns flowOf(emptyList())
        every { habitInteractor.getAvailableHabits() } returns flowOf(emptyList())
        coEvery { habitInteractor.getHabitDaysByHabitId(habitId) } returns habitDays

        viewModel = HabitViewModel(habitInteractor)
        viewModel.onHabitSelected(habitId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(habitId, state.selectedHabitId)
            // Verify that at least one day in the calendar has the COMPLETED status
            val hasCompletedDay = state.calendarState.months.any { month ->
                month.weeks.any { week ->
                    week.days.any { it?.habitStatus == HabitStatus.COMPLETED }
                }
            }
            assertTrue(hasCompletedDay)
        }
    }

    @Test
    fun `addHabit should call interactor and dismiss selection`() = runTest {
        val habitId = 1L
        
        every { habitInteractor.getAddedHabits() } returns flowOf(emptyList())
        every { habitInteractor.getAvailableHabits() } returns flowOf(emptyList())
        
        viewModel = HabitViewModel(habitInteractor)
        
        viewModel.onAddHabitClicked()
        assertTrue(viewModel.uiState.value.showAddHabitSelection)

        viewModel.addHabit(habitId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { habitInteractor.addHabit(habitId) }
        assertFalse(viewModel.uiState.value.showAddHabitSelection)
    }

    @Test
    fun `toggleHabitStatus should call interactor and update state`() = runTest {
        val habitId = 1L
        val date = LocalDate.now()
        val dayUiModel = DayUiModel(date = date, epochDay = date.toEpochDay())
        
        every { habitInteractor.getAddedHabits() } returns flowOf(listOf(HabitEntity(id = habitId, name = "H", iconResName = "i")))
        every { habitInteractor.getAvailableHabits() } returns flowOf(emptyList())
        coEvery { habitInteractor.getHabitDaysByHabitId(habitId) } returns emptyList()

        viewModel = HabitViewModel(habitInteractor)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleHabitStatus(dayUiModel, HabitStatus.COMPLETED)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { 
            habitInteractor.updateHabitDay(match { 
                it.habitId == habitId && it.status == HabitStatus.COMPLETED && it.date == date
            }) 
        }
        
        val currentState = viewModel.uiState.value
        val panelState = currentState.panelState as? HabitPanelUiState.Visible
        assertNotNull(panelState)
        assertEquals(HabitStatus.COMPLETED, panelState?.closingStatus)
    }
}
