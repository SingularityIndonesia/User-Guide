package com.singularityuniverse.lib.userguide.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.singularityuniverse.lib.userguide.*
import com.singularityuniverse.lib.userguide.tools.LocalIsDebugModeEnabled
import com.singularityuniverse.lib.userguide.tools.borderStroke1Dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun UserGuideFitBoundsPreview() {
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
                        tooltipContent = "This is the third target. Notice how the tooltip automatically positions itself."
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
                        tooltipContent = "This is the second target in our guide sequence. You can add detailed instructions here."
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
            if (isPreferTop) drawAbove {
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
                            }
                        )
                        .border(borderStroke1Dp(if (isDebugMode) Color.Blue else Color.Transparent)),
                ) {
                    ToolTip(content = info.tooltipContent)
                }
            }

            if (isPreferBottom) drawBellow {
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
                            }
                        )
                        .border(borderStroke1Dp(if (isDebugMode) Color.Blue else Color.Transparent)),
                ) {
                    ToolTip(content = info.tooltipContent)
                }
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
