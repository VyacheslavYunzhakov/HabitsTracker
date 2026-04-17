package compose.project.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import compose.project.data.model.HabitStatus
import compose.project.designsystem.R
import compose.project.home.HabitIcon
import compose.project.home.HabitPanelUiState
import compose.project.home.HabitState
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid

private enum class PanelDirection {
    Start,
    End
}

@Composable
fun CalendarPanelOverlay(
    panelAnchor: PanelAnchor?,
    panelState: HabitPanelUiState,
    onSelect: (Long, HabitStatus) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit,
    panelLiquidState: LiquidState,
    iconResId: Int
) {
    panelAnchor?.let { anchor ->
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current

        val dayCellWidth = 60.dp
        val dayCellHeight = 70.dp
        val targetWidth = 136.dp

        val screenWidthPx = windowInfo.containerSize.width.toFloat()

        val cellWidthPx = with(density) { dayCellWidth.toPx() }
        val targetWidthPx = with(density) { targetWidth.toPx() }
        val sidePaddingPx = with(density) { 6.dp.toPx() }

        val widthAnim = remember { Animatable(dayCellWidth.value) }

        val direction = remember(anchor.x, screenWidthPx) {
            val openToRightFits = anchor.x + targetWidthPx + sidePaddingPx <= screenWidthPx
            if (openToRightFits) PanelDirection.Start else PanelDirection.End
        }

        val isVisibleContent =
            panelState is HabitPanelUiState.Visible || widthAnim.value > dayCellWidth.value

        LaunchedEffect(panelState) {
            if (panelState is HabitPanelUiState.Visible) {
                widthAnim.animateTo(
                    targetWidth.value,
                    animationSpec = tween(300)
                )
            } else {
                widthAnim.snapTo(dayCellWidth.value)
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

        val x = xPx.toInt().coerceIn(
            0,
            (screenWidthPx - currentWidthPx).toInt()
        )
        val y = yPx.toInt()

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
                    selectedStatus = anchor.day.habitStatus,
                    widthDp = widthAnim.value.dp,
                    targetWidth = targetWidth,
                    onSelect = { state ->
                        val status = when (state) {
                            HabitState.COMPLETED -> HabitStatus.COMPLETED
                            HabitState.MISSED -> HabitStatus.MISSED
                            HabitState.UNMARKED -> HabitStatus.UNMARKED
                            HabitState.DEFAULT -> return@HabitStatePanel
                        }
                        onSelect(anchor.day.epochDay, status)
                    },
                    panelLiquidState = panelLiquidState,
                    iconResId = iconResId
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
    onSelect: (HabitState) -> Unit,
    panelLiquidState: LiquidState,
    iconResId: Int
) {
    val states = remember(selectedStatus, direction) {
        buildOrderedStates(selectedStatus, direction)
    }

    val startPadding = 8.dp
    val endPadding = 8.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .liquid(panelLiquidState) {
                shape = RoundedCornerShape(100)
                frost = 10.dp
                refraction = 0.5f
                curve = 0.5f
                edge = 0.1f
                tint = Color.White.copy(alpha = 0.2f)
                saturation = 1.5f
                dispersion = 0.25f
            }
            .padding(top = 18.dp, bottom = 10.dp, start = startPadding, end = endPadding)
    ) {
        when (direction) {
            PanelDirection.Start -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    states.forEach { state ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onSelect(state) },
                            contentAlignment = Alignment.Center
                        ) {
                            HabitIcon(
                                selectorRes = iconResId,
                                habitState = state,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            }

            PanelDirection.End -> {
                RevealEndByWidth(
                    states = states,
                    widthDp = widthDp,
                    targetWidth = targetWidth,
                    onSelect = onSelect,
                    iconResId = iconResId
                )
            }
        }
    }
}

@Composable
private fun RevealEndByWidth(
    states: List<HabitState>,
    widthDp: Dp,
    targetWidth: Dp,
    onSelect: (HabitState) -> Unit,
    iconResId: Int
) {
    val density = LocalDensity.current

    val iconSize = 40.dp
    val spacing = 3.dp

    val iconPx = with(density) { iconSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    val progress = ((widthDp.value - 60f) / (targetWidth.value - 60f))
        .coerceIn(0f, 1f)

    val w1 = iconPx * ((progress - 0.33f) / 0.33f).coerceIn(0f, 1f)
    val w0 = iconPx * ((progress - 0.66f) / 0.34f).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val x2 = maxWidthPx - iconPx
        val x1 = x2 - spacingPx - w1
        val x0 = x1 - spacingPx - w0

        IconCellAtWidth(
            state = states[2],
            x = x2,
            widthPx = iconPx,
            iconSize = iconSize,
            onSelect = onSelect,
            iconResId = iconResId
        )

        if (w1 > 0f) {
            IconCellAtWidth(
                state = states[1],
                x = x1,
                widthPx = w1,
                iconSize = iconSize,
                onSelect = onSelect,
                iconResId = iconResId
            )
        }

        if (w0 > 0f) {
            IconCellAtWidth(
                state = states[0],
                x = x0,
                widthPx = w0,
                iconSize = iconSize,
                onSelect = onSelect,
                iconResId = iconResId
            )
        }
    }
}

@Composable
private fun IconCellAtWidth(
    state: HabitState,
    x: Float,
    widthPx: Float,
    iconSize: Dp,
    onSelect: (HabitState) -> Unit,
    iconResId: Int
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset { IntOffset(x.toInt(), 0) }
            .width(with(density) { widthPx.toDp() })
            .height(iconSize)
            .clipToBounds(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .clickable(enabled = widthPx > 10f) {
                    onSelect(state)
                },
            contentAlignment = Alignment.Center
        ) {
            HabitIcon(
                selectorRes = iconResId,
                habitState = state,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

private fun buildOrderedStates(
    selectedStatus: HabitStatus?,
    direction: PanelDirection
): List<HabitState> {
    val selectedState = when (selectedStatus) {
        HabitStatus.COMPLETED -> HabitState.COMPLETED
        HabitStatus.MISSED -> HabitState.MISSED
        HabitStatus.UNMARKED -> HabitState.UNMARKED
        null -> HabitState.COMPLETED
    }

    val others = listOf(
        HabitState.COMPLETED,
        HabitState.MISSED,
        HabitState.UNMARKED
    ).filter { it != selectedState }

    return when (direction) {
        PanelDirection.Start -> listOf(selectedState) + others
        PanelDirection.End -> others + listOf(selectedState)
    }
}