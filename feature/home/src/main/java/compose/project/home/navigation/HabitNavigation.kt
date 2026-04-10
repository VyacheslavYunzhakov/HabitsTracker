package compose.project.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import compose.project.home.ui.HabitTrackerScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeBaseRoute

@Serializable
data class HabitRoute(val habitId: Long)

fun NavController.navigateToHabit(habitId: Long) {
    navigate(HabitRoute(habitId = habitId))
}

fun NavGraphBuilder.homeScreen() {
    navigation<HomeBaseRoute>(startDestination = HabitRoute(habitId = 1L)) {
        composable<HabitRoute> {
            HabitTrackerScreen()
        }
    }
}