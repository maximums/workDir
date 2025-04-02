package com.cdodi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.window.ComposeViewport
import com.cdodi.components.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.*
import org.jetbrains.skia.Paint

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
    var isInCenter by remember { mutableStateOf(true) }

    MaterialTheme {
        LookaheadScope {
            BoxWithConstraints(
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
                val homeButton = remember {
                    movableContentWithReceiverOf<LookaheadScope> {
                        UiCard(
                            modifier = Modifier
                                .size(200.dp, 250.dp)
                                .padding(10.dp)
                                .then(AnimatePlacementNodeElement(lookaheadScope = this))
                        ) {
                            Text(text = "Home", fontSize = 30.sp, color = Color(0xa0_5a_d6_ff))
                        }
                    }
                }

                val aboutButton = remember {
                    movableContentWithReceiverOf<LookaheadScope, Shape, Modifier> { shape, modifier ->
                        UiCard(
                            shape = shape,
                            modifier = modifier
                                .padding(10.dp)
                                .then(AnimatePlacementNodeElement(lookaheadScope = this))
                        ) {
                            Text(text = "About", fontSize = 30.sp, color = Color(0xa0_5a_d6_ff))
                        }
                    }
                }

                val contactsButton = remember {
                    movableContentWithReceiverOf<LookaheadScope, Shape> { shape ->
                        UiCard(
                            shape = shape,
                            modifier = Modifier
                                .size(200.dp, 250.dp)
                                .padding(10.dp)
                                .then(AnimatePlacementNodeElement(lookaheadScope = this))
                        ) {
                            Text(text = "Contacts", fontSize = 30.sp, color = Color(0xa0_5a_d6_ff))
                        }
                    }
                }

                val sketchButton = remember {
                    movableContentWithReceiverOf<LookaheadScope> {
                        UiCard(
                            modifier = Modifier
                                .size(200.dp, 250.dp)
                                .padding(10.dp)
                                .then(AnimatePlacementNodeElement(lookaheadScope = this))
                        ) {
                            Text(text = "Sketch", fontSize = 30.sp, color = Color(0xa0_5a_d6_ff))
                        }
                    }
                }
                Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = { isInCenter = !isInCenter }) {Text("Click Me")}

                if (isInCenter) {
                    CenterMenuForm {
                        homeButton()
                        aboutButton(rightButtonShape, Modifier.size(200.dp, 250.dp))
                        contactsButton(rightButtonShape)
                        sketchButton()
                    }
                } else {
                    TopBarForm {
                        homeButton()
                        aboutButton(RectangleShape, Modifier.size(300.dp, 100.dp))
                        contactsButton(RectangleShape)
                        sketchButton()
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CenterMenuForm(content: @Composable () -> Unit) {
    FlowRow(
//        horizontalArrangement = Arrangement.Center,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier.fillMaxSize(),
        maxItemsInEachRow = 2
    ) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BoxScope.TopBarForm(content: @Composable () -> Unit) {
//    FlowRow(
////        horizontalArrangement = Arrangement.Center,
////        verticalArrangement = Arrangement.Top,
//        modifier = Modifier.align(Alignment.TopCenter),
//        maxItemsInEachRow = 4
//    ) {
//        content()
//    }
    Row(
        modifier = Modifier.align(Alignment.TopCenter),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}