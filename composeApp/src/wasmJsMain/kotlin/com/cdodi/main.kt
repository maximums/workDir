package com.cdodi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.window.ComposeViewport
import com.cdodi.components.UiCard
import kotlinx.browser.document
import org.jetbrains.skia.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

@Composable
private fun App() {
    val time by iTime
    val runtimeEffect = remember { RuntimeEffect.makeForShader(Shaders.wetNeuralNetwork) }

    MaterialTheme {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .drawWithCache {
                        val shaderPaint = Paint().apply {
                            shader = runtimeEffect.makeShader(
                                uniforms = uniformData(
                                    size.width.fastRoundToInt(),
                                    size.height.fastRoundToInt(),
                                    time
                                ),
                                children = null,
                                localMatrix = null
                            )
                        }
                        onDrawBehind {
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawPaint(shaderPaint)
                            }
                        }
                    }
            ) {
                UiCard(modifier = Modifier.size(800.dp, 600.dp)) {
                    Text(text = "Welcome", fontSize = 40.sp, color = Color(0xa0_5ad6ff))
                }
            }
        }
}

@Composable
private fun TopBar() {
    UiCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Home",
                color = Color(0xff5ad6ff),
                fontSize = 25.sp,
            )
            Text(
                text = "About",
                color = Color(0xff5ad6ff),
                fontSize = 25.sp,
            )
            Text(
                text = "Contacts",
                color = Color(0xff5ad6ff),
                fontSize = 25.sp,
            )
        }
    }
}