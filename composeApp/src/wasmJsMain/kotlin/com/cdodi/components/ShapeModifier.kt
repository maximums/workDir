package com.cdodi.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement

class ShapeModifierNode(var startTranstion: () -> Unit) : DrawModifierNode, Modifier.Node() {
    private val offset: Animatable<Float, AnimationVector1D> by mutableStateOf( Animatable(initialValue = 0f))
    private var mSize by mutableStateOf(Size.Unspecified)
    override fun ContentDrawScope.draw() {
        mSize = size
//        lineTo(x = size.width, y = size.height / 4)
//        lineTo(x = size.width, y = size.height)
//        lineTo(x = 0f, y = size.height * 3/4)
    }
}

data class ShapeModifierElement(val startTranstion: () -> Unit) : ModifierNodeElement<ShapeModifierNode>() {
    override fun create() = ShapeModifierNode(startTranstion)

    override fun update(node: ShapeModifierNode) {
        node.startTranstion = startTranstion
    }
}

@Composable
fun Modifier.morphingShape(startTranstion: () -> Unit): Modifier = this then ShapeModifierElement(startTranstion)
