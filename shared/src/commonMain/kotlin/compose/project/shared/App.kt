package compose.project.shared

import androidx.compose.runtime.Composable
import compose.project.designsystem.theme.HabitsTrackerTheme

@Composable
fun App() {
    HabitsTrackerTheme {
        AppNavHost()
    }
}