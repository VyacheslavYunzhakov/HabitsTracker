package compose.project.home2.ui

import java.time.format.TextStyle
import java.util.Locale

actual fun formatMonth(monthNumber: Int): String {
    val systemLocale = Locale.getDefault()

    val month = java.time.Month.of(monthNumber)

    return month.getDisplayName(TextStyle.FULL, systemLocale)
}