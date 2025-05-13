package com.singularityuniverse.lib.userguide.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.singularityuniverse.lib.userguide.ClipMode
import com.singularityuniverse.lib.userguide.addTargetToTargetGuide
import com.singularityuniverse.lib.userguide.rememberUserGuideState
import com.singularityuniverse.lib.userguide.userGuide
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun UserGuideFitWidthPreview() {
    val userGuideState = rememberUserGuideState(clipMode = ClipMode.FitWidth)
    
    UserGuidePreviewContent(userGuideState, "FitWidth Mode")
}

@Preview
@Composable
fun UserGuideFitHeightPreview() {
    val userGuideState = rememberUserGuideState(clipMode = ClipMode.FitHeight)
    
    UserGuidePreviewContent(userGuideState, "FitHeight Mode")
}

@Preview
@Composable
fun UserGuideFitBoundsPreview() {
    val userGuideState = rememberUserGuideState(clipMode = ClipMode.FitBounds)
    
    UserGuidePreviewContent(userGuideState, "FitBounds Mode")
}

@Composable
private fun UserGuidePreviewContent(userGuideState: com.singularityuniverse.lib.userguide.UserGuideState, title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .zIndex(1f)
        ) {
            Button(
                onClick = { userGuideState.showGuide() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Start Guide ($title)")
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

                Button(onClick = { userGuideState.skipGuide() }) {
                    Text("Skip")
                }

                Button(onClick = { userGuideState.skipAllGuide() }) {
                    Text("Skip All")
                }
            }
        }

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
                        .addTargetToTargetGuide(userGuideState, id = "target-1", order = 0),
                    text = "This is Target 1"
                )

                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .addTargetToTargetGuide(userGuideState, id = "target-2", order = 2),
                    text = "This is Target 3"
                )

                Spacer(modifier = Modifier.height(100.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .addTargetToTargetGuide(userGuideState, id = "target-3", order = 1)
                        .size(120.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Target 2")
                }
            }
        }
    }
}