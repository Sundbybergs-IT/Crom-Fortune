package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.abs

enum class AiStonkMood { Neutral, Happy, Angry }

/** Fully code-drawn character. No bitmap is used as its base. */
@Composable
fun AiStonk(mood: AiStonkMood, modifier: Modifier = Modifier) {
    val expression by animateFloatAsState(
        when (mood) {
            AiStonkMood.Angry -> -1f
            AiStonkMood.Neutral -> 0f
            AiStonkMood.Happy -> 1f
        },
        tween(500, easing = FastOutSlowInEasing),
        label = "Expression"
    )
    val idle = rememberInfiniteTransition(label = "Living face")
    val pulse by idle.animateFloat(
        .55f, 1f, infiniteRepeatable(tween(1_250), RepeatMode.Reverse), label = "Eye pulse"
    )
    val gaze by idle.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(2_100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Gaze"
    )
    val blinkPhase by idle.animateFloat(
        0f, 1f, infiniteRepeatable(tween(3_700, easing = LinearEasing)), label = "Blink"
    )
    val hover by idle.animateFloat(
        -.005f, .005f, infiniteRepeatable(tween(1_800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Hover"
    )

    Canvas(modifier.semantics { contentDescription = "AI Stonk, ${mood.name.lowercase()}" }) {
        val w = size.width
        val h = size.height
        val y = h * hover
        val shellLight = Color(0xFFE1D8CC)
        val shellMid = Color(0xFFAAA096)
        val shellDark = Color(0xFF4A4642)
        val seam = Color(0xFF625D57)
        val eyeColor = if (expression < -.5f) Color(0xFFFF3025) else Color(0xFFFF4B38)

        // Elongated neck and pedestal retain the recognizable Stonks bust form.
        val neck = Path().apply {
            moveTo(w * .34f, h * .66f + y)
            cubicTo(w * .38f, h * .78f, w * .31f, h * .87f, w * .22f, h * .93f)
            cubicTo(w * .16f, h * .99f, w * .84f, h * .99f, w * .78f, h * .93f)
            cubicTo(w * .69f, h * .87f, w * .62f, h * .78f, w * .66f, h * .66f + y)
            close()
        }
        drawPath(neck, Brush.horizontalGradient(listOf(shellDark, shellMid, shellLight, shellMid, shellDark)))
        clipPath(neck) {
            drawOval(
                Brush.radialGradient(
                    listOf(shellLight.copy(alpha = .26f), Color.Transparent),
                    center = Offset(w * .56f, h * .82f),
                    radius = w * .28f
                ),
                Offset(w * .32f, h * .68f + y),
                Size(w * .38f, h * .28f)
            )
            drawOval(
                Brush.radialGradient(
                    listOf(shellDark.copy(alpha = .28f), Color.Transparent),
                    center = Offset(w * .37f, h * .73f),
                    radius = w * .23f
                ),
                Offset(w * .22f, h * .66f + y),
                Size(w * .32f, h * .28f)
            )
        }
        drawLine(seam, Offset(w * .5f, h * .69f + y), Offset(w * .5f, h * .97f), w * .005f)

        // Smooth, tall cranium with a tapered jaw; all geometry moves together.
        val head = Path().apply {
            moveTo(w * .5f, h * .035f + y)
            cubicTo(w * .23f, h * .035f + y, w * .13f, h * .18f + y, w * .17f, h * .43f + y)
            cubicTo(w * .19f, h * .59f + y, w * .28f, h * .70f + y, w * .43f, h * .77f + y)
            cubicTo(w * .48f, h * .795f + y, w * .52f, h * .795f + y, w * .57f, h * .77f + y)
            cubicTo(w * .72f, h * .70f + y, w * .81f, h * .59f + y, w * .83f, h * .43f + y)
            cubicTo(w * .87f, h * .18f + y, w * .77f, h * .035f + y, w * .5f, h * .035f + y)
            close()
        }
        drawPath(head, Brush.horizontalGradient(listOf(shellDark, shellMid, shellLight, shellLight, shellMid, shellDark)))
        // Broad lighting planes sculpt the forehead, temples, sockets, cheeks and jaw.
        // Clipping is essential: soft gradients must never escape the head silhouette.
        clipPath(head) {
            drawOval(
            Brush.radialGradient(
                listOf(Color(0xFFFFFAF0).copy(alpha = .35f), Color.Transparent),
                center = Offset(w * .61f, h * .19f + y),
                radius = w * .38f
            ),
            Offset(w * .37f, h * .06f + y),
            Size(w * .39f, h * .34f)
        )
        drawOval(
            Brush.radialGradient(
                listOf(shellDark.copy(alpha = .30f), Color.Transparent),
                center = Offset(w * .20f, h * .38f + y),
                radius = w * .32f
            ),
            Offset(w * .14f, h * .16f + y),
            Size(w * .33f, h * .48f)
        )
        drawOval(
            Brush.radialGradient(
                listOf(shellDark.copy(alpha = .20f), Color.Transparent),
                center = Offset(w * .80f, h * .39f + y),
                radius = w * .30f
            ),
            Offset(w * .58f, h * .18f + y),
            Size(w * .28f, h * .42f)
        )
        // Eye sockets are shallow shadows rather than flat circles.
        drawOval(
            Brush.radialGradient(
                listOf(shellDark.copy(alpha = .27f), shellDark.copy(alpha = .06f), Color.Transparent),
                center = Offset(w * .36f, h * .43f + y),
                radius = w * .17f
            ),
            Offset(w * .24f, h * .35f + y),
            Size(w * .25f, h * .17f)
        )
        drawOval(
            Brush.radialGradient(
                listOf(shellDark.copy(alpha = .24f), shellDark.copy(alpha = .05f), Color.Transparent),
                center = Offset(w * .64f, h * .43f + y),
                radius = w * .17f
            ),
            Offset(w * .51f, h * .35f + y),
            Size(w * .25f, h * .17f)
        )
        // Light-catching cheekbones with darker lower cheeks and chin.
        drawOval(
            Brush.radialGradient(
                listOf(shellLight.copy(alpha = .30f), Color.Transparent),
                center = Offset(w * .35f, h * .53f + y),
                radius = w * .21f
            ),
            Offset(w * .22f, h * .45f + y),
            Size(w * .26f, h * .22f)
        )
        drawOval(
            Brush.radialGradient(
                listOf(shellLight.copy(alpha = .28f), Color.Transparent),
                center = Offset(w * .66f, h * .53f + y),
                radius = w * .21f
            ),
            Offset(w * .53f, h * .45f + y),
            Size(w * .25f, h * .22f)
        )
        drawOval(
            Brush.radialGradient(
                listOf(shellDark.copy(alpha = .20f), Color.Transparent),
                center = Offset(w * .50f, h * .74f + y),
                radius = w * .29f
            ),
            Offset(w * .30f, h * .62f + y),
            Size(w * .40f, h * .17f)
        )
        }

        drawPath(head, seam, style = Stroke(w * .006f))
        drawLine(seam.copy(alpha = .65f), Offset(w * .5f, h * .04f + y), Offset(w * .5f, h * .75f + y), w * .004f)

        drawLine(seam, Offset(w * .26f, h * .12f + y), Offset(w * .23f, h * .46f + y), w * .005f)
        drawLine(seam, Offset(w * .74f, h * .12f + y), Offset(w * .77f, h * .46f + y), w * .005f)
        repeat(5) { i ->
            drawLine(seam, Offset(w * (.68f + i * .018f), h * .18f + y), Offset(w * (.68f + i * .018f), h * .225f + y), w * .008f)
        }

        val blinkProgress = ((blinkPhase - .92f) / .08f).coerceIn(0f, 1f)
        val blink = 1f - abs(blinkProgress * 2f - 1f)
        val eyeY = h * .43f + y
        fun eye(centerX: Float, side: Float) {
            val eyeWidth = w * .17f
            val angrySquint = if (expression < 0f) 1f + expression * .34f else 1f
            val eyeHeight = h * .057f * angrySquint * (1f - blink * .88f)
            val center = Offset(centerX, eyeY)
            drawOval(Color(0xFF160E0D), Offset(centerX - eyeWidth / 2, eyeY - eyeHeight / 2), Size(eyeWidth, eyeHeight))
            if (blink < .82f) {
                val pupil = center + Offset(gaze * w * .009f, 0f)
                val angryIntensity = if (expression < 0f) 1f - expression * .7f else 1f
                drawCircle(eyeColor.copy(alpha = (.18f * pulse * angryIntensity).coerceAtMost(.42f)), w * .058f, pupil)
                drawCircle(Color(0xFF3A0605), w * .031f, pupil)
                drawCircle(eyeColor.copy(alpha = .7f + .3f * pulse), w * .020f, pupil, style = Stroke(w * .008f))
                drawCircle(Color.White, w * .007f, pupil - Offset(w * .005f, h * .004f))
            }
            val browY = eyeY - h * .065f
            // Angry brows descend toward the nose; happy brows lift away from it.
            val tilt = -expression * h * .026f * side
            drawLine(shellDark, Offset(centerX - w * .075f, browY - tilt), Offset(centerX + w * .075f, browY + tilt), w * .017f, StrokeCap.Round)
        }
        eye(w * .36f, 1f)
        eye(w * .64f, -1f)

        val nose = Path().apply {
            moveTo(w * .49f, h * .41f + y)
            lineTo(w * .45f, h * .60f + y)
            quadraticTo(w * .50f, h * .63f + y, w * .55f, h * .60f + y)
            lineTo(w * .51f, h * .41f + y)
            close()
        }
        drawPath(
            nose,
            Brush.horizontalGradient(
                listOf(shellDark.copy(alpha = .20f), shellLight.copy(alpha = .62f), shellDark.copy(alpha = .32f)),
                startX = w * .44f,
                endX = w * .56f
            )
        )
        drawPath(nose, seam.copy(alpha = .75f), style = Stroke(w * .009f, cap = StrokeCap.Round))

        // Happy becomes a smile; angry becomes a tight, asymmetric grimace rather
        // than a sad downward curve.
        val mouthY = h * .68f + y
        val mouth = Path().apply {
            val anger = (-expression).coerceAtLeast(0f)
            moveTo(w * .39f, mouthY + h * .010f * anger)
            quadraticTo(
                w * .50f,
                mouthY + h * (.052f * expression.coerceAtLeast(0f) - .004f * anger),
                w * .61f,
                mouthY - h * .004f * anger
            )
        }
        drawPath(mouth, shellDark, style = Stroke(w * .014f, cap = StrokeCap.Round))
        drawArc(
            shellLight.copy(alpha = .42f * ((expression + 1f) / 2f)),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * .42f, mouthY + h * .018f),
            size = Size(w * .16f, h * .055f),
            style = Stroke(w * .007f, cap = StrokeCap.Round)
        )
    }
}
