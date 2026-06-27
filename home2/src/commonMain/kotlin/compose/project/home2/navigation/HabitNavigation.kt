package compose.project.home2.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.project.home2.ui.HabitTrackerScreen

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HabitTrackerScreen()
    }
}

data class HabitScreen(val habitId: Long) : Screen {
    @Composable
    override fun Content() {
        HabitTrackerScreen()
    }
}