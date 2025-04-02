package com.cdodi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun UiCard(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    shape: Shape = leftButtonShape,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = contentAlignment,
        content = content,
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = Color(0xFF_FF_00_00),
//                color = Color(0x50_5a_d6_ff),
                shape = shape
            )
            .background(color = Color(0xA0_00_00_00))
            .clickable(enabled = true) {} // TODO finish later with LocalIndications
    )
}

enum class Orientation {
    Start,
    End;
}

@Stable
val leftButtonShape = ButtonShape(Orientation.Start)

@Stable
val rightButtonShape = ButtonShape(Orientation.End)

@Stable
class ButtonShape(private val orientation: Orientation) : Shape {

        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
            Outline.Generic(path = buttonPath(size = size, orientation = orientation))
}

@Stable
private fun buttonPath(size: Size, orientation: Orientation) : Path =  Path().apply {
    reset()

    when(orientation) {
        Orientation.Start -> {
            lineTo(x = size.width, y = size.height / 4)
            lineTo(x = size.width, y = size.height)
            lineTo(x = 0f, y = size.height * 3/4)
        }
        Orientation.End -> {
            moveTo(x = size.width, y = 0f)

            lineTo(x = size.width, y = size.height * 3/4)
            lineTo(x = 0f, y = size.height)
            lineTo(x = 0f , y = size.height / 4)
        }
    }

    close()
}