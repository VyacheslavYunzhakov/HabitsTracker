package compose.project.home.ui

import java.time.format.TextStyle
import java.util.Locale

actual fun formatMonth(monthNumber: Int): String {
    val month = java.time.Month.of(monthNumber)
    return month.getDisplayName(TextStyle.FULL, Locale.getDefault())
}
