package com.john.inkwell.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

/**
 * Subtle procedural paper grain, drawn as scattered translucent dots
 * instead of a shipped texture bitmap — keeps the APK small and the
 * effect crisp at any screen density. Points are generated once with
 * a fixed seed so the grain doesn't shift on recomposition.
 *
 * Usage: put this as the first child in a Box, with your real content
 * layered on top of it.
 */
@Composable
fun PaperGrain(
    grainColor: Color = InkBrown.copy(alpha = 0.035f),
    dotCount: Int = 900,
    modifier: Modifier = Modifier
) {
    val points = remember {
        val rng = Random(42)
        List(dotCount) {
            Triple(rng.nextFloat(), rng.nextFloat(), rng.nextFloat() * 1.4f + 0.4f)
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        points.forEach { (nx, ny, r) ->
            drawGrainDot(nx, ny, r, grainColor)
        }
    }
}

private fun DrawScope.drawGrainDot(nx: Float, ny: Float, radius: Float, color: Color) {
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(nx * size.width, ny * size.height)
    )
}
