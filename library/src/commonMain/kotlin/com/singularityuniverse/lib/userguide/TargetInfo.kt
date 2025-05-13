package com.singularityuniverse.lib.userguide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

data class TargetInfo(
    val id: String,
    val order: Int,
    var position: Offset = Offset.Companion.Zero,
    var size: Size = Size.Companion.Zero
) {
    val rect: Rect
        get() = Rect(position, size)
}