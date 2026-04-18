package compose.project.designsystem

import androidx.annotation.DrawableRes

enum class HabitIconType(
    val iconName: String,
    @param:DrawableRes
    val resId: Int
) {
    DRINK("drink_icon_selector", R.drawable.drink_icon_selector),
    SPORT("sport_icon_selector", R.drawable.sport_icon_selector),
    CANNABIS("cannabis_icon_selector", R.drawable.cannabis_icon_selector),
    RUN("run_icon_selector", R.drawable.run_icon_selector),
    TRASH("trash_can", R.drawable.trash_can);

    companion object {
        fun fromName(name: String): HabitIconType {
            return entries.find { it.iconName == name } ?: DRINK
        }
    }
}