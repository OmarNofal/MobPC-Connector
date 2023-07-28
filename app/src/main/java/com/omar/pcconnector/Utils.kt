package com.omar.pcconnector

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import java.nio.ByteBuffer
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.pow


val Path.absolutePath: String
    get() = absolutePathString().removePrefix("/")


val sizeRanges = arrayOf(
    2.0.pow(10.0).toLong() until 2.0.pow(20.0).toLong() to "KB",
    2.0.pow(20.0).toLong() until 2.0.pow(30.0).toLong() to "MB",
    2.0.pow(30.0).toLong() until Long.MAX_VALUE to "GB"
)

// Converts size in bytes to human-readable format
// ex 4096bytes = 4KB
fun Long.bytesToSizeString(): String {

    if (this in 0 until 1024) return "$this Bytes"

    val result = try {
        val sizeRange = sizeRanges.first { this in it.first }
        "${this / sizeRange.first.first} ${sizeRange.second}"
    } catch (e: NoSuchElementException) {
        "Unknown size"
    }

    return result
}


// The image formats coil-kt can view
val supportedImageExtension = listOf("jpg", "jpeg", "png", "bmp", "webp")
fun String.isSupportedImageExtension() = this in supportedImageExtension


/**
 * Suspends coroutine until the buffer is filled
 */
suspend fun BufferedSource.fillBuffer(byteBuffer: ByteBuffer) {
    withContext(Dispatchers.IO) {
        while (byteBuffer.remaining() > 0) {
            this@fillBuffer.read(byteBuffer)
        }
    }
}



fun Modifier.drawAnimatedBorder(
    strokeWidth: Dp,
    shape: Shape,
    gradientColors: List<Color>,
    brush: (Size) -> Brush = {
        Brush.sweepGradient(gradientColors)
    },
    durationMillis: Int
) = composed {

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Modifier
        .clip(shape)
        .drawWithCache {
            val strokeWidthPx = strokeWidth.toPx()

            val outline: Outline = shape.createOutline(size, layoutDirection, this)

            onDrawWithContent {
                // This is actual content of the Composable that this modifier is assigned to
                drawContent()

                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)

                    // Destination

                    // We draw 2 times of the stroke with since we want actual size to be inside
                    // bounds while the outer stroke with is clipped with Modifier.clip

                    // 🔥 Using a maskPath with op(this, outline.path, PathOperation.Difference)
                    // And GenericShape can be used as Modifier.border does instead of clip
                    drawOutline(
                        outline = outline,
                        color = Color.Gray,
                        style = Stroke(strokeWidthPx * 2)
                    )

                    // Source
                    rotate(angle) {

                        drawCircle(
                            brush = brush(size),
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                    restoreToCount(checkPoint)
                }
            }
        }
}
