@file:OptIn(ExperimentalAnimatableApi::class)

package com.cdodi.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ApproachLayoutModifierNode
import androidx.compose.ui.layout.ApproachMeasureScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round

/**
 * Creates a custom implementation of ApproachLayoutModifierNode to approach the placement of
 * the layout using an animation.
 */
class AnimatedPlacementModifierNode(var lookaheadScope: LookaheadScope) :
    ApproachLayoutModifierNode, Modifier.Node() {
    // Creates an offset animation, the target of which will be known during placement.
    val offsetAnimation: DeferredTargetAnimation<IntOffset, AnimationVector2D> =
        DeferredTargetAnimation(IntOffset.VectorConverter)
    val sizeAnimation: DeferredTargetAnimation<IntSize, AnimationVector2D> =
        DeferredTargetAnimation(IntSize.VectorConverter)

    @OptIn(ExperimentalAnimatableApi::class)
    override fun isMeasurementApproachInProgress(lookaheadSize: IntSize): Boolean {
        sizeAnimation.updateTarget(lookaheadSize, coroutineScope, tween(durationMillis = 1800))
        return !offsetAnimation.isIdle
    }

    // Returns true when the offset animation is in progress, false otherwise.
    @OptIn(ExperimentalAnimatableApi::class)
    override fun Placeable.PlacementScope.isPlacementApproachInProgress(
        lookaheadCoordinates: LayoutCoordinates
    ): Boolean {
        val target =
            with(lookaheadScope) {
                lookaheadScopeCoordinates.localLookaheadPositionOf(lookaheadCoordinates).round()
            }
        offsetAnimation.updateTarget(target, coroutineScope, tween(durationMillis = 2000))
        return !offsetAnimation.isIdle
    }

    override fun ApproachMeasureScope.approachMeasure(measurable: Measurable,constraints: Constraints): MeasureResult {
        val (animWidth, animHeight) = sizeAnimation.updateTarget(lookaheadSize, coroutineScope)
        val placeable = measurable.measure(Constraints.fixed(animWidth, animHeight))
        return layout(placeable.width, placeable.height) {
            val coordinates = coordinates
            if (coordinates != null) {
                // Calculates the target offset within the lookaheadScope
                val target =
                    with(lookaheadScope) {
                        lookaheadScopeCoordinates.localLookaheadPositionOf(coordinates).round()
                    }

                // Uses the target offset to start an offset animation
                val animatedOffset = offsetAnimation.updateTarget(target, coroutineScope)
                // Calculates the *current* offset within the given LookaheadScope
                val placementOffset =
                    with(lookaheadScope) {
                        lookaheadScopeCoordinates
                            .localPositionOf(coordinates, Offset.Zero)
                            .round()
                    }
                // Calculates the delta between animated position in scope and current
                // position in scope, and places the child at the delta offset. This puts
                // the child layout at the animated position.
                val (x, y) = animatedOffset - placementOffset
                placeable.place(x, y)
            } else {
                placeable.place(0, 0)
            }
        }
    }
}

// Creates a custom node element for the AnimatedPlacementModifierNode above.
public data class AnimatePlacementNodeElement(val lookaheadScope: LookaheadScope) :
    ModifierNodeElement<AnimatedPlacementModifierNode>() {

    override fun update(node: AnimatedPlacementModifierNode) {
        node.lookaheadScope = lookaheadScope
    }

    override fun create(): AnimatedPlacementModifierNode {
        return AnimatedPlacementModifierNode(lookaheadScope)
    }
}