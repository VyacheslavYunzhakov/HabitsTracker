package compose.project.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
class AppState {
}

@Composable
fun rememberExplorerAppState(): AppState {
    return remember { AppState() }
}

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("AppState was not provided")
}
