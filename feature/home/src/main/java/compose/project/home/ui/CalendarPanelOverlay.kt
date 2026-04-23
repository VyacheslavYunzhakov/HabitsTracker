package compose.project.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import compose.project.designsystem.HabitIconType
import compose.project.data.model.HabitStatus
import compose.project.home.DayUiModel
import compose.project.home.HabitIcon
import compose.project.home.HabitPanelUiState
import compose.project.home.HabitState
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import kotlin.math.roundToInt

private enum class PanelDirection {
    Start,
    End
}

@Composable
fun CalendarPanelOverlay(
    panelAnchor: PanelAnchor?,
    panelState: HabitPanelUiState,
    onSelect: (DayUiModel, HabitStatus) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit,
    panelLiquidState: LiquidState,
    iconType: HabitIconType,
    onHideFinished: () -> Unit
) {
    panelAnchor?.let { anchor ->
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current

        val dayCellWidth = 57.dp
        val dayCellHeight = 70.dp
        val targetWidth = 136.dp

        val screenWidthPx = windowInfo.containerSize.width.toFloat()
        val cellWidthPx = with(density) { dayCellWidth.toPx() }
        val targetWidthPx = with(density) { targetWidth.toPx() }
        val sidePaddingPx = with(density) { 6.dp.toPx() }

        val widthAnim = remember { Animatable(dayCellWidth.value) }
        val liquidCloseAnim = remember { Animatable(0f) }

        val direction = remember(anchor.x, screenWidthPx) {
            val openToRightFits = anchor.x + targetWidthPx + sidePaddingPx <= screenWidthPx
            if (openToRightFits) PanelDirection.Start else PanelDirection.End
        }

        val isVisible = panelState is HabitPanelUiState.Visible
        val isClosing = (panelState as? HabitPanelUiState.Visible)?.closingStatus != null
        val isVisibleContent = isVisible || widthAnim.value > dayCellWidth.value

        LaunchedEffect(isVisible, isClosing) {
            when {
                isVisible && !isClosing -> {
                    liquidCloseAnim.snapTo(0f)
                    widthAnim.animateTo(targetWidth.value, animationSpec = tween(300))
                }

                isClosing -> {
                    widthAnim.animateTo(dayCellWidth.value, animationSpec = tween(300))
                    liquidCloseAnim.animateTo(1f, animationSpec = tween(180))
                    onHideFinished()
                }

                else -> {
                    widthAnim.snapTo(dayCellWidth.value)
                    liquidCloseAnim.snapTo(0f)
                }
            }
        }

        val currentWidthPx = with(density) { widthAnim.value.dp.toPx() }

        val xPx = when (direction) {
            PanelDirection.Start -> {
                anchor.x - cellWidthPx / 2f - with(density) { 13.dp.toPx() }
            }
            PanelDirection.End -> {
                anchor.x + cellWidthPx / 2f - currentWidthPx - with(density) { 18.dp.toPx() }
            }
        }

        val yPx =
            anchor.y - with(density) { 6.dp.toPx() + dayCellHeight.toPx() } + with(density) { 10.dp.toPx() }

        val x = xPx.toInt().coerceIn(0, (screenWidthPx - currentWidthPx).toInt())
        val y = yPx.toInt()

        val displayedStatus = when (panelState) {
            is HabitPanelUiState.Visible -> panelState.closingStatus ?: anchor.day.habitStatus
            else -> anchor.day.habitStatus
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(x, y) }
                .size(widthAnim.value.dp, dayCellHeight)
                .zIndex(100f)
                .onGloballyPositioned { coords ->
                    onBoundsChanged(coords.boundsInParent())
                }
        ) {
            if (isVisibleContent) {
                HabitStatePanel(
                    direction = direction,
                    selectedStatus = displayedStatus,
                    widthDp = widthAnim.value.dp,
                    targetWidth = targetWidth,
                    closeProgress = liquidCloseAnim.value,
                    onSelect = { state ->
                        val status = when (state) {
                            HabitState.COMPLETED -> HabitStatus.COMPLETED
                            HabitState.MISSED -> HabitStatus.MISSED
                            HabitState.UNMARKED -> HabitStatus.UNMARKED
                            else -> HabitStatus.UNMARKED
                        }
                        onSelect(anchor.day, status)
                    },
                    panelLiquidState = panelLiquidState,
                    iconType = iconType
                )
            }
        }
    }
}

@Composable
private fun HabitStatePanel(
    direction: PanelDirection,
    selectedStatus: HabitStatus?,
    widthDp: Dp,
    targetWidth: Dp,
    closeProgress: Float,
    onSelect: (HabitState) -> Unit,
    panelLiquidState: LiquidState,
    iconType: HabitIconType
) {
    val startPadding = 6.dp
    val endPadding = 6.dp

    val frost = androidx.compose.ui.unit.lerp(10.dp, 0.dp, closeProgress)
    val refraction = androidx.compose.ui.util.lerp(0.5f, 0f, closeProgress)
    val curve = androidx.compose.ui.util.lerp(0.5f, 0f, closeProgress)
    val edge = androidx.compose.ui.util.lerp(0.1f, 0f, closeProgress)
    val tintAlpha = androidx.compose.ui.util.lerp(0.2f, 0f, closeProgress)
    val saturation = androidx.compose.ui.util.lerp(1.5f, 1f, closeProgress)
    val dispersion = androidx.compose.ui.util.lerp(0.25f, 0f, closeProgress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquid(panelLiquidState) {
                shape = RoundedCornerShape(100)
                this.frost = frost
                this.refraction = refraction
                this.curve = curve
                this.edge = edge
                tint = Color.White.copy(alpha = tintAlpha)
                this.saturation = saturation
                this.dispersion = dispersion
            }
            .padding(top = 18.dp, bottom = 10.dp, start = startPadding, end = endPadding)
    ) {
        RevealByWidth(
            selectedStatus = selectedStatus,
            direction = direction,
            widthDp = widthDp,
            targetWidth = targetWidth,
            closeProgress = closeProgress,
            onSelect = onSelect,
            iconType = iconType
        )
    }
}

private val allStates = listOf(
    HabitState.COMPLETED,
    HabitState.MISSED,
    HabitState.UNMARKED
)

@Composable
private fun RevealByWidth(
    selectedStatus: HabitStatus?,
    direction: PanelDirection,
    widthDp: Dp,
    targetWidth: Dp,
    closeProgress: Float,
    onSelect: (HabitState) -> Unit,
    iconType: HabitIconType
) {
    val density = LocalDensity.current

    val iconSize = 40.dp
    val spacing = 0.dp
    val alpha = androidx.compose.ui.util.lerp(1f, 0f, closeProgress)
    val iconPx = with(density) { iconSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    val selectedIdx = selectedIndex(selectedStatus)

    val openProgress = ((widthDp.value - 60f) / (targetWidth.value - 60f))
        .coerceIn(0f, 1f)

    val collapseProgress = 1f - openProgress

    val order = collapseOrder(direction, selectedIdx)

    val widths = FloatArray(3) { iconPx }

    val gap0 = gapFor(widths[0], iconPx, spacingPx)
    val gap1 = gapFor(widths[1], iconPx, spacingPx)

    fun widthForPhase(start: Float, end: Float): Float {
        val t = ((collapseProgress - start) / (end - start)).coerceIn(0f, 1f)
        return iconPx * (1f - t)
    }

    widths[order[0]] = widthForPhase(0f, 0.5f)
    widths[order[1]] = widthForPhase(0.5f, 1f)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        when (direction) {
            PanelDirection.Start -> {
                val x0 = 0f
                val x1 = x0 + widths[0] + gap0
                val x2 = x1 + widths[1] + gap1

                IconCellAtWidth(
                    state = allStates[0],
                    x = x0,
                    widthPx = widths[0],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
                IconCellAtWidth(
                    state = allStates[1],
                    x = x1,
                    widthPx = widths[1],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
                IconCellAtWidth(
                    state = allStates[2],
                    x = x2,
                    widthPx = widths[2],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
            }

            PanelDirection.End -> {
                val x2 = maxWidthPx - widths[2]
                val x1 = x2 - widths[1] - gap1
                val x0 = x1 - widths[0] - gap0

                IconCellAtWidth(
                    state = allStates[0],
                    x = x0,
                    widthPx = widths[0],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
                IconCellAtWidth(
                    state = allStates[1],
                    x = x1,
                    widthPx = widths[1],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
                IconCellAtWidth(
                    state = allStates[2],
                    x = x2,
                    widthPx = widths[2],
                    iconSize = iconSize,
                    alpha = alpha,
                    onSelect = onSelect,
                    iconType = iconType
                )
            }
        }
    }
}

private fun gapFor(widthPx: Float, iconPx: Float, spacingPx: Float): Float {
    return spacingPx * (widthPx / iconPx).coerceIn(0f, 1f)
}

private fun selectedIndex(selectedStatus: HabitStatus?): Int = when (selectedStatus) {
    HabitStatus.COMPLETED -> 0
    HabitStatus.MISSED -> 1
    HabitStatus.UNMARKED -> 2
    else -> 0
}

private fun collapseOrder(direction: PanelDirection, selectedIndex: Int): IntArray {
    return when (direction) {
        PanelDirection.Start -> when (selectedIndex) {
            0 -> intArrayOf(1, 2)
            1 -> intArrayOf(0, 2)
            2 -> intArrayOf(0, 1)
            else -> intArrayOf(1, 2)
        }

        PanelDirection.End -> when (selectedIndex) {
            0 -> intArrayOf(2, 1)
            1 -> intArrayOf(2, 0)
            2 -> intArrayOf(1, 0)
            else -> intArrayOf(2, 1)
        }
    }
}

@Composable
private fun IconCellAtWidth(
    state: HabitState,
    x: Float,
    widthPx: Float,
    iconSize: Dp,
    alpha: Float,
    onSelect: (HabitState) -> Unit,
    iconType: HabitIconType
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), 0) }
            .width(with(density) { widthPx.toDp() })
            .height(iconSize)
            .clipToBounds(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .alpha(alpha)
                .size(iconSize)
                .clip(CircleShape)
                .clickable(enabled = widthPx > 10f && alpha > 0.2f) {
                    onSelect(state)
                },
            contentAlignment = Alignment.Center
        ) {
            HabitIcon(
                iconType = iconType,
                habitState = state,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}