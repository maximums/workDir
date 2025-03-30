package com.cdodi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UiCard(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = contentAlignment,
        content = content,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0x50_5a_d6_ff),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .background(
                color = Color(0xA0_00_00_00),
                shape = RoundedCornerShape(size = 12.dp)
            )
    )
}