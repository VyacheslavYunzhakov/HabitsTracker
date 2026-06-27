package compose.project.designsystem

import habitstracker.core.designsystem.generated.resources.Res
import habitstracker.core.designsystem.generated.resources.cannabis_color_black
import habitstracker.core.designsystem.generated.resources.cannabis_color_green
import habitstracker.core.designsystem.generated.resources.cannabis_color_grey
import habitstracker.core.designsystem.generated.resources.cannabis_color_red
import habitstracker.core.designsystem.generated.resources.run_color_black
import habitstracker.core.designsystem.generated.resources.run_color_green
import habitstracker.core.designsystem.generated.resources.run_color_grey
import habitstracker.core.designsystem.generated.resources.run_color_red
import habitstracker.core.designsystem.generated.resources.sport_color_black
import habitstracker.core.designsystem.generated.resources.sport_color_green
import habitstracker.core.designsystem.generated.resources.sport_color_grey
import habitstracker.core.designsystem.generated.resources.sport_color_red
import habitstracker.core.designsystem.generated.resources.trash_can
import habitstracker.core.designsystem.generated.resources.wine_color_black
import habitstracker.core.designsystem.generated.resources.wine_color_green
import habitstracker.core.designsystem.generated.resources.wine_color_grey
import habitstracker.core.designsystem.generated.resources.wine_color_red
import org.jetbrains.compose.resources.DrawableResource

enum class HabitIconType(
    val iconName: String,
    val completedIcon: DrawableResource,
    val missedIcon: DrawableResource,
    val unmarkedIcon: DrawableResource,
    val defaultIcon: DrawableResource
) {
    DRINK(
        "drink_icon_selector",
        Res.drawable.wine_color_green,
        Res.drawable.wine_color_red,
        Res.drawable.wine_color_grey,
        Res.drawable.wine_color_black
    ),
    SPORT(
        "sport_icon_selector",
        Res.drawable.sport_color_green,
        Res.drawable.sport_color_red,
        Res.drawable.sport_color_grey,
        Res.drawable.sport_color_black
    ),
    CANNABIS(
        "cannabis_icon_selector",
        Res.drawable.cannabis_color_green,
        Res.drawable.cannabis_color_red,
        Res.drawable.cannabis_color_grey,
        Res.drawable.cannabis_color_black
    ),
    RUN(
        "run_icon_selector",
        Res.drawable.run_color_green,
        Res.drawable.run_color_red,
        Res.drawable.run_color_grey,
        Res.drawable.run_color_black
    ),
    TRASH(
        "trash_can",
        Res.drawable.trash_can,
        Res.drawable.trash_can,
        Res.drawable.trash_can,
        Res.drawable.trash_can
    );

    companion object {
        fun fromName(name: String): HabitIconType =
            entries.find { it.iconName == name } ?: DRINK
    }
}