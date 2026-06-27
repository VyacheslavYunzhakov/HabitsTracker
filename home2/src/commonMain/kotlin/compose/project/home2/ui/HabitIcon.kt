package compose.project.home2.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import compose.project.designsystem.HabitIconType
import compose.project.domain2.model.HabitStatus
import compose.project.home2.HabitState

@Composable
fun HabitIcon(
    iconType: HabitIconType,
    modifier: Modifier = Modifier,
    habitState: HabitState
) {
    val icon = when (habitState) {
        HabitState.COMPLETED -> iconType.completedIcon
        HabitState.MISSED -> iconType.missedIcon
        HabitState.UNMARKED -> iconType.unmarkedIcon
        HabitState.DEFAULT -> iconType.defaultIcon
    }

    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun HabitIcon(
    iconType: HabitIconType,
    habitStatus: HabitStatus?,
    modifier: Modifier = Modifier
) {
    val icon = when (habitStatus) {
        HabitStatus.COMPLETED -> iconType.completedIcon
        HabitStatus.MISSED -> iconType.missedIcon
        else -> iconType.unmarkedIcon
    }

    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier
    )
}