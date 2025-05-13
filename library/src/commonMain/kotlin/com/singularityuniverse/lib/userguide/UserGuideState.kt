package com.singularityuniverse.lib.userguide

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

@Stable
class UserGuideState {
    private val _targets = mutableStateMapOf<String, TargetInfo>()
    val targets: SnapshotStateMap<String, TargetInfo> = _targets

    private var _isShowing by mutableStateOf(false)
    val isShowing: Boolean get() = _isShowing

    private var _currentStep by mutableIntStateOf(-1)
    val currentStep: Int get() = _currentStep

    private val orderedTargets = mutableStateListOf<TargetInfo>()

    private var _animatedRect by mutableStateOf(Rect.Companion.Zero)
    val animatedRect: Rect get() = _animatedRect

    private var _animationState by mutableStateOf(GuideAnimationState.IDLE)
    val animationState: GuideAnimationState get() = _animationState

    private var _fullScreenRect by mutableStateOf(Rect.Companion.Zero)
    val fullScreenRect: Rect get() = _fullScreenRect
    
    private var _clipMode by mutableStateOf(ClipMode.FitBounds)
    val clipMode: ClipMode get() = _clipMode

    val navigationAnimationSpec: AnimationSpec<Rect> = SpringSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val transitionAnimationSpec: AnimationSpec<Rect> = SpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    internal fun registerTarget(id: String, order: Int, position: Offset, size: Size, tooltipContent: String? = null) {
        val targetInfo = TargetInfo(id, order, position, size, tooltipContent)
        _targets[id] = targetInfo
        updateOrderedTargets()
    }

    internal fun updateTargetPosition(id: String, position: Offset, size: Size) {
        _targets[id]?.let { target ->
            target.position = position
            target.size = size
        }
    }
    
    internal fun updateTargetTooltip(id: String, tooltipContent: String?) {
        _targets[id]?.let { target ->
            target.tooltipContent = tooltipContent
        }
    }

    internal fun setFullScreenRect(rect: Rect) {
        _fullScreenRect = rect
    }

    private fun updateOrderedTargets() {
        orderedTargets.clear()
        orderedTargets.addAll(_targets.values.sortedBy { it.order })
    }

    fun getCurrentTarget(): TargetInfo? {
        return if (_currentStep >= 0 && _currentStep < orderedTargets.size) {
            orderedTargets[_currentStep]
        } else {
            null
        }
    }

    fun getCurrentTargetRect(): Rect {
        val target = getCurrentTarget() ?: return Rect.Companion.Zero
        val targetRect = target.rect
        
        return when (clipMode) {
            ClipMode.FitWidth -> {
                // Create a vertical strip that spans the full height of the screen
                // but maintains the horizontal position and width of the target
                Rect(
                    left = targetRect.left,
                    top = 0f,
                    right = targetRect.right,
                    bottom = _fullScreenRect.bottom
                )
            }
            ClipMode.FitHeight -> {
                // Create a horizontal strip that spans the full width of the screen
                // but maintains the vertical position and height of the target
                Rect(
                    left = 0f,
                    top = targetRect.top,
                    right = _fullScreenRect.right,
                    bottom = targetRect.bottom
                )
            }
            ClipMode.FitBounds -> {
                // Use the exact bounds of the target (default behavior)
                targetRect
            }
        }
    }

    internal fun setAnimatedRect(rect: Rect) {
        _animatedRect = rect
    }

    internal fun setAnimationState(state: GuideAnimationState) {
        _animationState = state
    }

    fun setClipMode(mode: ClipMode) {
        _clipMode = mode
    }

    fun showGuide() {
        updateOrderedTargets()
        if (orderedTargets.isNotEmpty()) {
            _currentStep = 0
            _isShowing = true

            _animationState = GuideAnimationState.ENTERING
        }
    }

    fun nextGuide() {
        if (_currentStep < orderedTargets.size - 1) {
            // First set the animation state to ENTERING for the transition
            _animationState = GuideAnimationState.ENTERING
            // Then increment the step
            _currentStep++
            // The animation will handle the transition and then set state to NAVIGATING
        } else {
            _animationState = GuideAnimationState.EXITING
        }
    }

    fun skipGuide() {
        if (_currentStep < orderedTargets.size - 1) {
            // First set the animation state to ENTERING for the transition
            _animationState = GuideAnimationState.ENTERING
            // Then increment the step
            _currentStep++
            // The animation will handle the transition and then set state to NAVIGATING
        } else {
            _animationState = GuideAnimationState.EXITING
        }
    }

    fun skipAllGuide() {
        _animationState = GuideAnimationState.EXITING
    }

    internal fun finishExit() {
        _isShowing = false
        _currentStep = -1
        _animationState = GuideAnimationState.IDLE
    }
}

@Composable
fun rememberUserGuideState(clipMode: ClipMode = ClipMode.FitBounds): UserGuideState {
    return remember { 
        UserGuideState().apply {
            setClipMode(clipMode)
        }
    }
}