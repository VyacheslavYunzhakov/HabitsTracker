package compose.project.home

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import compose.project.designsystem.R
import kotlin.math.roundToInt

@Composable
fun HabitIcon(
    @DrawableRes selectorRes: Int,
    state: HabitState,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val drawable = remember(selectorRes) {
        requireNotNull(AppCompatResources.getDrawable(context, selectorRes)).mutate()
    }

    val stateSet = remember(state) {
        when (state) {
            HabitState.DEFAULT -> intArrayOf()
            HabitState.COMPLETED -> intArrayOf(R.attr.state_completed)
            HabitState.MISSED -> intArrayOf(R.attr.state_missed)
            HabitState.UNMARKED -> intArrayOf(R.attr.state_unmarked)
        }
    }

    SideEffect {
        drawable.state = stateSet
    }

    Image(
        painter = remember(drawable, state) { DrawablePainter(drawable) },
        contentDescription = contentDescription,
        modifier = modifier
    )
}

private class DrawablePainter(
    private val drawable: Drawable
) : Painter() {

    override val intrinsicSize: Size
        get() = if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
            Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        } else {
            Size.Unspecified
        }

    override fun DrawScope.onDraw() {
        drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        drawIntoCanvas { canvas ->
            drawable.draw(canvas.nativeCanvas)
        }
    }
}