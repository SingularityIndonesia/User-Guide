package com.singularityuniverse.lib.userguide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.singularityuniverse.lib.userguide.tools.LocalIsDebugModeEnabled
import com.singularityuniverse.lib.userguide.tools.borderStroke1Dp
import kotlin.math.absoluteValue

@Composable
fun UserGuideOverlay(
    state: UserGuideState,
    tooltipBuilder: (UserGuideOverlayScope.(targetInfo: TargetInfo) -> Unit)
) {
    val density = LocalDensity.current
    val isDebugMode = LocalIsDebugModeEnabled.current
    val currentTarget = state.getCurrentTarget()
    val scope = UserGuideOverlayScope(state, density)
        .apply { tooltipBuilder(this, currentTarget ?: return) }

    if (state.isShowing && state.animationState == GuideAnimationState.NAVIGATING) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // top section container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((scope.highlightRect.top / density.density).dp)
                    .border(borderStroke1Dp(if (isDebugMode) Color.Red else Color.Transparent)),
                contentAlignment = when {
                    scope.isTargetHorizontallyCentered -> Alignment.BottomCenter
                    scope.isPreferLeft -> Alignment.BottomEnd
                    else -> Alignment.BottomStart
                }
            ) {
                scope.aboveContents.map { it.invoke(this) }
            }

            // top section container
            Box(
                modifier = Modifier
                    .padding(top = (scope.highlightRect.bottom / density.density).dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .border(borderStroke1Dp(if (isDebugMode) Color.Red else Color.Transparent)),
                contentAlignment =
                    when {
                        scope.isTargetHorizontallyCentered -> Alignment.TopCenter
                        scope.isPreferLeft -> Alignment.TopStart
                        else -> Alignment.TopEnd
                    }
            ) {

                scope.bellowContents.map { it.invoke(this) }

                if (isDebugMode)
                    Text(
                        """
                        preferOnTop = ${scope.isPreferTop}
                        preferLeft = ${scope.isPreferLeft}
                        centerVertically = ${scope.isTargetVerticallyCentered}
                        centerHorizontally = ${scope.isTargetHorizontallyCentered}
                    """.trimIndent(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(1f)
                    )
            }

            scope.topContents.map { it.invoke(this) }
        }
    }
}

class UserGuideOverlayScope(
    private val state: UserGuideState,
    private val density: Density
) {
    companion object {
        private val FLOAT_ROUNDING_PIXEL_TOLERANCE = 10
    }

    val rootRect: Rect get() = state.fullScreenRect
    val highlightRect: Rect get() = state.animatedRect
    val targetRect: Rect? get() = state.getCurrentTarget()?.rect

    val isPreferTop: Boolean get() = highlightRect.center.y > rootRect.center.y
    val isPreferBottom: Boolean get() = !isPreferTop
    val isPreferLeft: Boolean get() = highlightRect.center.x < rootRect.center.x
    val isPreferRight: Boolean get() = !isPreferLeft

    val isTargetHorizontallyCentered: Boolean
        get() = (highlightRect.center.x - rootRect.center.x).absoluteValue < FLOAT_ROUNDING_PIXEL_TOLERANCE
    val isTargetVerticallyCentered: Boolean = highlightRect.center.y == rootRect.center.y

    val targetLeftSpace: Dp = (highlightRect.left / density.density).dp
    val targetRightSpace: Dp = ((rootRect.width - highlightRect.right) / density.density).dp

    internal var bellowContents: List<@Composable BoxScope.() -> Unit> = emptyList()
        private set

    fun drawBellow(content: @Composable BoxScope.() -> Unit) {
        bellowContents += content
    }

    internal var aboveContents: List<@Composable BoxScope.() -> Unit> = emptyList()
        private set

    fun drawAbove(content: @Composable BoxScope.() -> Unit) {
        aboveContents += content
    }

    internal var topContents: List<@Composable BoxScope.() -> Unit> = emptyList()
        private set

    fun drawOnTop(content: @Composable BoxScope.() -> Unit) {
        topContents += content
    }

    fun drawPointer(content: @Composable BoxScope.() -> Unit) {
        topContents += @Composable {
            val pointerSize = remember { mutableStateOf(IntSize.Zero) }
            val pointerCenter = pointerSize.value.center

            Box(
                modifier = Modifier
                    .onSizeChanged { pointerSize.value = it }
                    // translate container into proper position automatically
                    .offset {
                        val targetCenter = targetRect?.center
                            ?.let { IntOffset(it.x.toInt(), it.y.toInt()) }
                            ?: IntOffset.Zero

                        val center = targetCenter - pointerCenter

                        val verticalOffsetCorrection = (if (isPreferTop) -1 else 1) *
                                (pointerSize.value.height + (targetRect?.height ?: 0f)) / 2

                        center + IntOffset(x = 0, y = verticalOffsetCorrection.toInt())
                    }
            ) {
                content()
            }
        }
    }

    fun drawAuto(content: @Composable BoxScope.() -> Unit) {
        if (isPreferTop) drawAbove {
            DrawAutoContainer {
                content()
            }
        }

        if (isPreferBottom) drawBellow {
            DrawAutoContainer {
                content()
            }
        }
    }

    @Composable
    private fun DrawAutoContainer(content: @Composable BoxScope.() -> Unit) {
        val isDebugMode = LocalIsDebugModeEnabled.current
        Box(
            modifier = Modifier
                .padding(
                    start = when {
                        isTargetHorizontallyCentered -> 0.dp
                        isPreferLeft -> targetLeftSpace
                        else -> 0.dp
                    },
                    end = when {
                        isTargetHorizontallyCentered -> 0.dp
                        isPreferRight -> targetRightSpace
                        else -> 0.dp
                    },
                )
                .border(borderStroke1Dp(if (isDebugMode) Color.Blue else Color.Transparent)),
        ) {
            content()
        }
    }
}