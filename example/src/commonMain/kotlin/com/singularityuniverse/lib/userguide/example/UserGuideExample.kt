package com.singularityuniverse.lib.userguide.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.singularityuniverse.lib.userguide.*
import com.singularityuniverse.lib.userguide.tools.LocalIsDebugModeEnabled
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun UserGuideExample() {
    val userGuideState = rememberUserGuideState(clipMode = ClipMode.FitBounds)

    Box(modifier = Modifier.fillMaxSize()) {
        // Content
        Content(userGuideState)

        // Control just for interaction simulation
        Control(userGuideState)
    }
}

@Preview
@Composable
private fun Content(userGuideState: UserGuideState) {
    val density = LocalDensity.current
    val isDebugMode = true

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .userGuide(userGuideState)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .addTargetToTargetGuide(
                        userGuideState,
                        id = "target-1",
                        order = 0,
                        tooltipContent = "This is the first target in our guide. The tooltip appears above or below based on available space."
                    ),
                text = "This is Target 1"
            )

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End)
                    .addTargetToTargetGuide(
                        userGuideState,
                        id = "target-2",
                        order = 1,
                        tooltipContent = "This is the second target. Notice how the tooltip automatically positions itself."
                    ),
                text = "This is Target 2"
            )

            Spacer(modifier = Modifier.height(100.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .addTargetToTargetGuide(
                        userGuideState,
                        id = "target-3",
                        order = 2,
                        tooltipContent = "This is the third target in our guide sequence. You can add detailed instructions here."
                    )
                    .size(120.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Target 3")
            }
        }
    }

    // Guide Overlay to show tooltips
    CompositionLocalProvider(LocalIsDebugModeEnabled provides isDebugMode) {
        UserGuideOverlay(
            state = userGuideState
        ) { info ->
            // Tool Tip
            drawAuto {
                Box(
                    modifier = Modifier.padding(10.dp)
                ) {
                    ToolTip(content = info.tooltipContent)
                }
            }

            // Pointer
            drawPointer {
                Pointer(rotationDegrees = if (isPreferTop) 180f else 0f)
            }
        }
    }
}

@Composable
fun BoxScope.Control(userGuideState: UserGuideState) {
    Column(
        modifier = Modifier.align(Alignment.BottomStart)
            .zIndex(1f)
    ) {
        Button(
            onClick = { userGuideState.showGuide() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Start Guide Fit Bound")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { userGuideState.nextGuide() }) {
                Text("Next")
            }

            Button(onClick = { userGuideState.previousGuide() }) {
                Text("Previous")
            }

            Button(onClick = { userGuideState.skipAllGuide() }) {
                Text("Skip All")
            }
        }
    }
}

@Composable
fun ToolTip(modifier: Modifier = Modifier, content: String) {
    Card(modifier) {
        Surface {
            Text(
                modifier = Modifier.padding(16.dp),
                text = content
            )
        }
    }
}

@Composable
fun Pointer(
    rotationDegrees: Float = 0f
) {
    val color = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(24.dp)
            .drawWithContent {
                drawContent()
                val width = size.width
                val height = size.height

                val triangle = Path().apply {
                    // Triangle pointing up
                    moveTo(width / 2f, 0f)              // Top middle
                    lineTo(0f, height)                  // Bottom left
                    lineTo(width, height)
                    // Bottom right
                    close()
                }

                rotate(rotationDegrees, pivot = size.center) {
                    drawPath(
                        path = triangle,
                        color = color
                    )
                }
            }
    )
}