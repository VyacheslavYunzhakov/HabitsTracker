import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.project.data.di.dataModule
import compose.project.domain.di.domainModule
import compose.project.home.di.viewModelModule
import compose.project.shared.App
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(dataModule, domainModule, viewModelModule)
    }

    application {
        val windowState = rememberWindowState(
            size = DpSize(390.dp, 844.dp)
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "Habits Tracker",
            state = windowState
        ) {
            App()
        }
    }
}
