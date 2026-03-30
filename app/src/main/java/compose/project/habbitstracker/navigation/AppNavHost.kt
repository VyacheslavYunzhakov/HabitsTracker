package compose.project.habbitstracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import compose.project.habbitstracker.AppState
import compose.project.home.navigation.HomeBaseRoute
import compose.project.home.navigation.homeScreen

@Composable
fun AppNavHost(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier
    ) {
        homeScreen()
    }
}