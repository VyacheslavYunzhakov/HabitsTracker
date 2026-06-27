package compose.project.home.ui

import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun formatMonth(monthNumber: Int): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "LLLL"
        this.locale = NSLocale.currentLocale
    }

    // используем любой день месяца
    val components = NSDateComponents().apply {
        year = 2020
        month = monthNumber.toLong()
        day = 1
    }

    val date = NSCalendar.currentCalendar.dateFromComponents(components)!!
    return formatter.stringFromDate(date)
}