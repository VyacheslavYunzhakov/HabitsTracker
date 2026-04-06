package compose.project.home

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import compose.project.designsystem.R
import kotlin.math.roundToInt

@Composable
fun HabitIcon(
    @DrawableRes selectorRes: Int,
    state: HabitState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = remember(selectorRes, context) {
        requireNotNull(AppCompatResources.getDrawable(context, selectorRes)).mutate()
    }

    Spacer(
        modifier = modifier.drawWithCache {
            onDrawBehind {
                val stateSet = when (state) {
                    HabitState.DEFAULT -> intArrayOf()
                    HabitState.COMPLETED -> intArrayOf(R.attr.state_completed)
                    HabitState.MISSED -> intArrayOf(R.attr.state_missed)
                    HabitState.UNMARKED -> intArrayOf(R.attr.state_unmarked)
                }

                if (!drawable.state.contentEquals(stateSet)) {
                    drawable.state = stateSet
                }

                drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
                drawIntoCanvas { canvas ->
                    drawable.draw(canvas.nativeCanvas)
                }
            }
        }
    )
}