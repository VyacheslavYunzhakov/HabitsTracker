package compose.project.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = SkyDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f)
)

val LightColorScheme = lightColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = SkyDark,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFFE0E0E0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black.copy(alpha = 0.7f),
)


@Composable
internal expect fun rememberAppColorScheme(darkTheme: Boolean): ColorScheme