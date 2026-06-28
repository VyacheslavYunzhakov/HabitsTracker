package compose.project.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import compose.project.designsystem.theme.HabitsTrackerTheme
import compose.project.home.navigation.HomeScreen


@Composable
fun App() {
    val appState = rememberExplorerAppState()

    CompositionLocalProvider(LocalAppState provides appState) {
        HabitsTrackerTheme {
            Navigator(HomeScreen()) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CurrentScreen()
                }
            }
        }
    }
}