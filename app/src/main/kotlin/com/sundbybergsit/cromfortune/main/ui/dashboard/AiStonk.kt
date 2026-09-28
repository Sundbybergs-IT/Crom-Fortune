package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt

enum class AiStonkMood { Neutral, Happy, Angry }

/** Fully code-drawn character. No bitmap is used as its base. */
@Composable
fun AiStonk(mood: AiStonkMood, userPortfolioIsWorthMore: Boolean, modifier: Modifier = Modifier) {
    val expressionAnimation = remember { Animatable(0f) }
    val expression = expressionAnimation.value
    var isInitialMood by remember { mutableStateOf(true) }

    LaunchedEffect(mood) {
        val targetExpression = when (mood) {
            AiStonkMood.Angry -> -1f
            AiStonkMood.Neutral -> 0f
            AiStonkMood.Happy -> 1f
        }
        if (isInitialMood && targetExpression != 0f) {
            delay(300)
        }
        isInitialMood = false
        expressionAnimation.animateTo(
            targetValue = targetExpression,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }
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
    val gestureDepth by idle.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(tween(320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Forward fist gesture"
    )

    Canvas(modifier.semantics {
        contentDescription = "AI Stonk, ${mood.name.lowercase()}, " +
            if (userPortfolioIsWorthMore) "shaking his fist" else "matching his expression"
    }) {
        val w = size.width
        val canvasHeight = size.height
        val h = canvasHeight * .70f
        val y = h * hover
        val shellLight = Color(0xFFE1D8CC)
        val shellMid = Color(0xFFAAA096)
        val shellDark = Color(0xFF4A4642)
        val seam = Color(0xFF625D57)
        val eyeColor = lerp(
            start = Color(0xFFFF3025),
            stop = Color(0xFF35D66F),
            fraction = expression.coerceAtLeast(0f)
        )

        // Arms are drawn behind the bust. Their targets interpolate with the same
        // expression value as the face, keeping the whole character emotionally coherent.
        fun drawArm(
            shoulder: Offset,
            elbow: Offset,
            wrist: Offset,
            fist: Boolean = false,
            drawLimb: Boolean = true,
            drawHandLayer: Boolean = true,
            handDepthScale: Float = 1f,
            limbDepthScale: Float = 1f
        ) {
            fun unitVector(from: Offset, to: Offset): Offset {
                val dx = to.x - from.x
                val dy = to.y - from.y
                val length = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
                return Offset(dx / length, dy / length)
            }

            fun muscularSegment(
                start: Offset,
                end: Offset,
                startRadius: Float,
                endRadius: Float,
                bulge: Float
            ) {
                val direction = unitVector(start, end)
                val normal = Offset(-direction.y, direction.x)
                val length = sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y))
                val third = start + direction * (length * .38f)
                val twoThirds = start + direction * (length * .72f)
                val silhouette = Path().apply {
                    moveTo((start + normal * startRadius).x, (start + normal * startRadius).y)
                    cubicTo(
                        (third + normal * (startRadius + bulge)).x,
                        (third + normal * (startRadius + bulge)).y,
                        (twoThirds + normal * (endRadius + bulge * .35f)).x,
                        (twoThirds + normal * (endRadius + bulge * .35f)).y,
                        (end + normal * endRadius).x,
                        (end + normal * endRadius).y
                    )
                    lineTo((end - normal * endRadius).x, (end - normal * endRadius).y)
                    cubicTo(
                        (twoThirds - normal * (endRadius + bulge * .18f)).x,
                        (twoThirds - normal * (endRadius + bulge * .18f)).y,
                        (third - normal * (startRadius + bulge * .55f)).x,
                        (third - normal * (startRadius + bulge * .55f)).y,
                        (start - normal * startRadius).x,
                        (start - normal * startRadius).y
                    )
                    close()
                }
                drawPath(silhouette, shellDark)
                val inset = w * .010f
                val inner = Path().apply {
                    moveTo((start + normal * (startRadius - inset)).x, (start + normal * (startRadius - inset)).y)
                    cubicTo(
                        (third + normal * (startRadius + bulge - inset)).x,
                        (third + normal * (startRadius + bulge - inset)).y,
                        (twoThirds + normal * (endRadius + bulge * .35f - inset)).x,
                        (twoThirds + normal * (endRadius + bulge * .35f - inset)).y,
                        (end + normal * (endRadius - inset)).x,
                        (end + normal * (endRadius - inset)).y
                    )
                    lineTo((end - normal * (endRadius - inset)).x, (end - normal * (endRadius - inset)).y)
                    cubicTo(
                        (twoThirds - normal * (endRadius + bulge * .18f - inset)).x,
                        (twoThirds - normal * (endRadius + bulge * .18f - inset)).y,
                        (third - normal * (startRadius + bulge * .55f - inset)).x,
                        (third - normal * (startRadius + bulge * .55f - inset)).y,
                        (start - normal * (startRadius - inset)).x,
                        (start - normal * (startRadius - inset)).y
                    )
                    close()
                }
                drawPath(inner, Brush.linearGradient(listOf(shellDark, shellMid, shellLight), start, end))
                drawLine(
                    shellLight.copy(alpha = .45f),
                    start + normal * (startRadius * .35f),
                    end + normal * (endRadius * .30f),
                    w * .009f,
                    StrokeCap.Round
                )
            }

            fun local(origin: Offset, direction: Offset, along: Float, across: Float): Offset {
                val normal = Offset(-direction.y, direction.x)
                return origin + direction * along + normal * across
            }

            fun drawHand() {
                val direction = unitVector(elbow, wrist)
                val normal = Offset(-direction.y, direction.x)
                val palmLength = w * (if (fist) .130f else .118f) * handDepthScale
                val palmHalfWidth = w * (if (fist) .083f else .065f) * handDepthScale
                val palmCenter = wrist + direction * (palmLength * .45f)
                val depthFraction = ((handDepthScale - .82f) / .36f).coerceIn(0f, 1f)
                val palm = Path().apply {
                    moveTo(local(wrist, direction, 0f, palmHalfWidth * .72f).x, local(wrist, direction, 0f, palmHalfWidth * .72f).y)
                    cubicTo(
                        local(palmCenter, direction, 0f, palmHalfWidth).x,
                        local(palmCenter, direction, 0f, palmHalfWidth).y,
                        local(wrist, direction, palmLength, palmHalfWidth * .82f).x,
                        local(wrist, direction, palmLength, palmHalfWidth * .82f).y,
                        local(wrist, direction, palmLength, 0f).x,
                        local(wrist, direction, palmLength, 0f).y
                    )
                    cubicTo(
                        local(wrist, direction, palmLength, -palmHalfWidth * .88f).x,
                        local(wrist, direction, palmLength, -palmHalfWidth * .88f).y,
                        local(palmCenter, direction, 0f, -palmHalfWidth).x,
                        local(palmCenter, direction, 0f, -palmHalfWidth).y,
                        local(wrist, direction, 0f, -palmHalfWidth * .72f).x,
                        local(wrist, direction, 0f, -palmHalfWidth * .72f).y
                    )
                    close()
                }
                drawPath(palm, shellDark)
                drawPath(
                    palm,
                    Brush.linearGradient(
                        listOf(shellDark.copy(alpha = .70f), shellMid, shellLight),
                        wrist - normal * palmHalfWidth,
                        wrist + normal * palmHalfWidth
                    ),
                    style = Stroke(w * .010f * handDepthScale)
                )
                drawPath(palm, shellMid)
                drawOval(
                    Brush.radialGradient(
                        listOf(shellLight.copy(alpha = .35f + depthFraction * .40f), Color.Transparent),
                        palmCenter - normal * (palmHalfWidth * .25f),
                        palmLength
                    ),
                    palmCenter - Offset(palmLength * .42f, palmLength * .42f),
                    Size(palmLength * .84f, palmLength * .84f)
                )
                drawPath(palm, seam, style = Stroke(w * .010f * handDepthScale))

                if (fist) {
                    // Four curled fingers form the front plane; the thumb crosses
                    // them on top, making palm/back orientation unambiguous.
                    repeat(4) { index ->
                        val across = palmHalfWidth * (.70f - index * .47f)
                        val knuckle = local(wrist, direction, palmLength * .82f, across)
                        drawCircle(shellDark, w * .030f * handDepthScale, knuckle)
                        drawCircle(
                            if (index == 0) shellLight else shellMid,
                            w * .023f * handDepthScale,
                            knuckle
                        )
                        drawLine(
                            seam,
                            local(wrist, direction, palmLength * .48f, across),
                            local(wrist, direction, palmLength * .75f, across),
                            w * .006f * handDepthScale,
                            StrokeCap.Round
                        )
                    }
                    val thumb = Path().apply {
                        moveTo(local(wrist, direction, palmLength * .18f, -palmHalfWidth * .95f).x, local(wrist, direction, palmLength * .18f, -palmHalfWidth * .95f).y)
                        quadraticTo(
                            local(wrist, direction, palmLength * .60f, -palmHalfWidth * .42f).x,
                            local(wrist, direction, palmLength * .60f, -palmHalfWidth * .42f).y,
                            local(wrist, direction, palmLength * .68f, palmHalfWidth * .28f).x,
                            local(wrist, direction, palmLength * .68f, palmHalfWidth * .28f).y
                        )
                    }
                    drawPath(
                        thumb,
                        shellLight,
                        style = Stroke(w * .038f * handDepthScale, cap = StrokeCap.Round)
                    )
                    drawPath(
                        thumb,
                        seam,
                        style = Stroke(w * .007f * handDepthScale, cap = StrokeCap.Round)
                    )
                } else {
                    // Relaxed, separated fingers extend from the palm with unequal
                    // lengths; the thumb fans away on its own plane.
                    val lengths = listOf(.75f, 1f, .93f, .70f)
                    lengths.forEachIndexed { index, lengthFactor ->
                        val across = palmHalfWidth * (.68f - index * .45f)
                        drawLine(
                            shellDark,
                            local(wrist, direction, palmLength * .75f, across),
                            local(wrist, direction, palmLength * (1.0f + lengthFactor * .28f), across),
                            w * .031f,
                            StrokeCap.Round
                        )
                        drawLine(
                            shellLight.copy(alpha = .55f),
                            local(wrist, direction, palmLength * .78f, across),
                            local(wrist, direction, palmLength * (1.0f + lengthFactor * .25f), across),
                            w * .012f,
                            StrokeCap.Round
                        )
                    }
                    drawLine(
                        shellMid,
                        local(wrist, direction, palmLength * .35f, -palmHalfWidth * .85f),
                        local(wrist, direction, palmLength * .82f, -palmHalfWidth * 1.45f),
                        w * .039f,
                        StrokeCap.Round
                    )
                }
            }

            if (drawLimb) {
                val upperDepthScale = 1f + (limbDepthScale - 1f) * .35f
                muscularSegment(
                    shoulder,
                    elbow,
                    w * .080f * upperDepthScale,
                    w * .070f * upperDepthScale,
                    w * .035f * upperDepthScale
                )
                drawCircle(shellDark, w * .075f * upperDepthScale, elbow)
                drawCircle(shellMid, w * .062f * upperDepthScale, elbow)
                muscularSegment(
                    elbow,
                    wrist,
                    w * .068f * limbDepthScale,
                    w * .048f * limbDepthScale,
                    w * .028f * limbDepthScale
                )
            }
            if (drawHandLayer) drawHand()
        }

        // Attach the arms at the lower outside edge of the bust. Keeping raised
        // hands outside the head silhouette is important because the bust is
        // painted over the arms to make the shoulder joint look natural.
        val shoulderY = canvasHeight * .655f
        val leftShoulder = Offset(w * .13f, shoulderY)
        val rightShoulder = Offset(w * .87f, shoulderY)
        val leftElbow: Offset
        val leftHand: Offset
        val rightElbow: Offset
        val rightHand: Offset
        val hasCrossedArms = !userPortfolioIsWorthMore && expression < -.35f
        if (userPortfolioIsWorthMore) {
            // Crom raises and shakes one fist when the user's portfolio overtakes his.
            leftElbow = Offset(w * .15f, canvasHeight * .76f)
            leftHand = Offset(w * .17f, canvasHeight * .85f)
            val approach = (gestureDepth + 1f) / 2f
            rightElbow = Offset(
                w * (.93f - approach * .008f),
                canvasHeight * (.59f + approach * .008f)
            )
            val restingWrist = Offset(w * .86f, canvasHeight * .37f)
            rightHand = restingWrist + (rightElbow - restingWrist) * (approach * .13f)
        } else if (expression > .35f) {
            leftElbow = Offset(w * .11f, canvasHeight * .59f)
            leftHand = Offset(w * .07f, canvasHeight * .43f)
            rightElbow = Offset(w * .89f, canvasHeight * .59f)
            rightHand = Offset(w * .93f, canvasHeight * .43f)
        } else if (expression < -.35f) {
            leftElbow = Offset(w * .14f, canvasHeight * .78f)
            leftHand = Offset(w * .62f, canvasHeight * .84f)
            rightElbow = Offset(w * .86f, canvasHeight * .78f)
            rightHand = Offset(w * .38f, canvasHeight * .87f)
        } else {
            leftElbow = Offset(w * .15f, canvasHeight * .76f)
            leftHand = Offset(w * .17f, canvasHeight * .85f)
            rightElbow = Offset(w * .85f, canvasHeight * .76f)
            rightHand = Offset(w * .83f, canvasHeight * .85f)
        }
        drawArm(
            leftShoulder,
            leftElbow,
            leftHand,
            fist = expression < -.35f,
            drawHandLayer = hasCrossedArms
        )
        drawArm(
            rightShoulder,
            rightElbow,
            rightHand,
            fist = userPortfolioIsWorthMore || expression < -.35f,
            drawLimb = !userPortfolioIsWorthMore,
            drawHandLayer = hasCrossedArms,
            limbDepthScale = if (userPortfolioIsWorthMore) 1f + gestureDepth * .10f else 1f
        )

        // A broad shoulder and upper-chest plate gives the muscular arms a
        // believable attachment. It is painted over the shoulder joints but
        // behind the narrower neck pedestal below.
        val torso = Path().apply {
            moveTo(w * .35f, h * .72f + y)
            cubicTo(w * .31f, h * .80f, w * .20f, h * .87f, w * .08f, h * .92f)
            cubicTo(w * .045f, h * .94f, w * .035f, h * .98f, w * .055f, h * .995f)
            lineTo(w * .945f, h * .995f)
            cubicTo(w * .965f, h * .98f, w * .955f, h * .94f, w * .92f, h * .92f)
            cubicTo(w * .80f, h * .87f, w * .69f, h * .80f, w * .65f, h * .72f + y)
            close()
        }
        drawPath(
            torso,
            Brush.horizontalGradient(
                listOf(shellDark, shellMid, shellLight, shellLight, shellMid, shellDark)
            )
        )
        clipPath(torso) {
            drawOval(
                Brush.radialGradient(
                    listOf(shellLight.copy(alpha = .30f), Color.Transparent),
                    Offset(w * .50f, h * .86f),
                    w * .45f
                ),
                Offset(w * .20f, h * .72f),
                Size(w * .60f, h * .28f)
            )
        }
        drawPath(torso, seam, style = Stroke(w * .007f))
        drawLine(
            seam.copy(alpha = .55f),
            Offset(w * .50f, h * .76f),
            Offset(w * .50f, h * .99f),
            w * .004f
        )

        // The raised arm emerges in front of the shoulder plate, while the
        // neck and head below are still allowed to occlude it naturally.
        if (userPortfolioIsWorthMore) {
            drawArm(
                rightShoulder,
                rightElbow,
                rightHand,
                fist = true,
                drawHandLayer = false,
                limbDepthScale = 1f + gestureDepth * .10f
            )
        }

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
            cubicTo(w * .18f, h * .55f + y, w * .22f, h * .62f + y, w * .29f, h * .68f + y)
            lineTo(w * .40f, h * .77f + y)
            cubicTo(w * .45f, h * .805f + y, w * .55f, h * .805f + y, w * .60f, h * .77f + y)
            lineTo(w * .71f, h * .68f + y)
            cubicTo(w * .78f, h * .62f + y, w * .82f, h * .55f + y, w * .83f, h * .43f + y)
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
            val eyeBounds = Rect(
                left = centerX - eyeWidth / 2,
                top = eyeY - eyeHeight / 2,
                right = centerX + eyeWidth / 2,
                bottom = eyeY + eyeHeight / 2
            )
            val aperture = Path().apply { addOval(eyeBounds) }
            drawPath(aperture, Color(0xFF160E0D))
            if (blink < .82f) {
                val pupil = center + Offset(gaze * w * .009f, 0f)
                val angryIntensity = if (expression < 0f) 1f - expression * .7f else 1f
                clipPath(aperture) {
                    drawCircle(eyeColor.copy(alpha = (.18f * pulse * angryIntensity).coerceAtMost(.42f)), w * .058f, pupil)
                    drawCircle(Color(0xFF3A0605), w * .031f, pupil)
                    drawCircle(eyeColor.copy(alpha = .7f + .3f * pulse), w * .020f, pupil, style = Stroke(w * .008f))
                    drawCircle(Color.White, w * .007f, pupil - Offset(w * .005f, h * .004f))
                }
            }
            if (blink > .55f) {
                drawLine(
                    seam.copy(alpha = .45f + blink * .4f),
                    Offset(centerX - eyeWidth * .43f, eyeY),
                    Offset(centerX + eyeWidth * .43f, eyeY),
                    w * .006f,
                    StrokeCap.Round
                )
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

        // Hands belong to the foreground plane. Drawing them after the head
        // prevents a raised fist from disappearing behind the face while the
        // upper and lower arm remain correctly behind the bust.
        if (!hasCrossedArms) {
            drawArm(
                leftShoulder,
                leftElbow,
                leftHand,
                drawLimb = false
            )
            drawArm(
                rightShoulder,
                rightElbow,
                rightHand,
                fist = userPortfolioIsWorthMore,
                drawLimb = false,
                handDepthScale = if (userPortfolioIsWorthMore) 1f + gestureDepth * .18f else 1f
            )
        }
    }
}
