package compose.project.home.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.project.home.ui.HabitTrackerScreen

// commonMain

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
    }
}

data class HabitScreen(val habitId: Long) : Screen {
    @Composable
    override fun Content() {
        HabitTrackerScreen()
    }
}