package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.AtmosphericEffectType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AtmosphericBackgroundOverlay(
    effectType: AtmosphericEffectType,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "weatherEffects")

    val animProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    val sunPulse by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    val sunRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )

    // Semillas aleatorias fijas para partículas
    val rainDropSeed = remember {
        List(30) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 15f + 10f)
        }
    }

    val leafSeed = remember {
        List(14) {
            LeafData(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.4f + 0.6f,
                size = Random.nextFloat() * 8f + 12f,
                colorType = Random.nextInt(3)
            )
        }
    }

    val starSeed = remember {
        List(25) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1.5f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when (effectType) {
            AtmosphericEffectType.SUN_RAYS -> {
                // Rayos de sol apenas visibles y cálidos saliendo de la esquina superior derecha
                drawSunRays(width, height, sunRotation, sunPulse)
            }
            AtmosphericEffectType.RAIN_DROPS -> {
                // Suaves gotas de lluvia diagonales apenas visibles
                drawRainDrops(width, height, animProgress, rainDropSeed)
            }
            AtmosphericEffectType.DRIFTING_LEAVES -> {
                // Hojas de árboles meciéndose con el viento de forma sutil
                drawDriftingLeavesAndWind(width, height, animProgress, leafSeed)
            }
            AtmosphericEffectType.CLEAR_STARS -> {
                // Estrellas tenues con titileo en noche despejada
                drawStars(width, height, animProgress, starSeed)
            }
        }
    }
}

private fun DrawScope.drawSunRays(width: Float, height: Float, rotation: Float, pulseAlpha: Float) {
    val origin = Offset(width * 1.05f, -height * 0.05f)
    val rayCount = 12
    val maxRayLength = width * 1.3f

    // Resplandor cálido en la esquina
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD54F).copy(alpha = pulseAlpha * 0.7f),
                Color(0xFFFFB300).copy(alpha = pulseAlpha * 0.3f),
                Color.Transparent
            ),
            center = origin,
            radius = width * 0.65f
        ),
        radius = width * 0.65f,
        center = origin
    )

    // Rayos tenues rotando suavemente
    rotate(degrees = rotation, pivot = origin) {
        for (i in 0 until rayCount) {
            val angleDeg = (i * 360f / rayCount)
            val rad = angleDeg * PI / 180.0
            val endX = origin.x + (maxRayLength * cos(rad)).toFloat()
            val endY = origin.y + (maxRayLength * sin(rad)).toFloat()

            val rayPath = Path().apply {
                moveTo(origin.x, origin.y)
                val perpRad = rad + (PI / 2.0)
                val beamWidth = 35f
                val offset1X = origin.x + (beamWidth * cos(perpRad)).toFloat()
                val offset1Y = origin.y + (beamWidth * sin(perpRad)).toFloat()
                val offset2X = endX + (beamWidth * 3.5f * cos(perpRad)).toFloat()
                val offset2Y = endY + (beamWidth * 3.5f * sin(perpRad)).toFloat()

                lineTo(offset1X, offset1Y)
                lineTo(offset2X, offset2Y)
                close()
            }

            drawPath(
                path = rayPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4).copy(alpha = pulseAlpha * 0.45f),
                        Color(0xFFFFE082).copy(alpha = pulseAlpha * 0.15f),
                        Color.Transparent
                    ),
                    start = origin,
                    end = Offset(endX, endY)
                )
            )
        }
    }
}

private fun DrawScope.drawRainDrops(
    width: Float,
    height: Float,
    progress: Float,
    seeds: List<Triple<Float, Float, Float>>
) {
    val dropColor = Color(0xFF67E8F9).copy(alpha = 0.28f)
    val angleRad = (75.0 * PI / 180.0).toFloat()
    val dx = (cos(angleRad) * 22f)
    val dy = (sin(angleRad) * 22f)

    for (drop in seeds) {
        val initialX = drop.first * width
        val initialY = drop.second * height
        val length = drop.third

        // Desplazamiento animado
        val currentY = (initialY + (progress * height * 1.5f)) % (height + 50f)
        val currentX = (initialX + (progress * width * 0.3f)) % (width + 50f)

        drawLine(
            color = dropColor,
            start = Offset(currentX, currentY),
            end = Offset(currentX + (dx * length / 20f), currentY + (dy * length / 20f)),
            strokeWidth = 1.3f,
            cap = StrokeCap.Round
        )
    }
}

private data class LeafData(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val colorType: Int
)

private fun DrawScope.drawDriftingLeavesAndWind(
    width: Float,
    height: Float,
    progress: Float,
    leaves: List<LeafData>
) {
    // 1. Líneas sutiles de viento
    val windColor = Color(0xFFE2E8F0).copy(alpha = 0.15f)
    for (i in 0..2) {
        val yOffset = height * (0.25f + i * 0.3f)
        val windProgress = (progress + (i * 0.33f)) % 1f
        val startX = (windProgress * width * 1.4f) - (width * 0.2f)

        val windPath = Path().apply {
            moveTo(startX, yOffset)
            quadraticTo(
                startX + 60f, yOffset - 15f,
                startX + 120f, yOffset
            )
            quadraticTo(
                startX + 180f, yOffset + 15f,
                startX + 240f, yOffset
            )
        }
        drawPath(
            path = windPath,
            color = windColor,
            style = Stroke(width = 1.2f, cap = StrokeCap.Round)
        )
    }

    // 2. Hojas de árboles flotando
    for (leaf in leaves) {
        val leafX = ((leaf.startX * width) + (progress * leaf.speed * width * 1.2f)) % (width + 40f)
        val waveY = sin((progress * 4.0 * PI + leaf.startX * 10f)).toFloat() * 25f
        val leafY = (leaf.startY * height + waveY) % height

        val leafColor = when (leaf.colorType) {
            0 -> Color(0xFFFBBF24).copy(alpha = 0.32f) // Ámbar otoñal
            1 -> Color(0xFF34D399).copy(alpha = 0.28f) // Verde menta
            else -> Color(0xFFFB923C).copy(alpha = 0.30f) // Naranja cálido
        }

        // Dibujar forma de hoja orgánica
        val s = leaf.size
        val leafPath = Path().apply {
            moveTo(leafX, leafY)
            quadraticTo(leafX + s, leafY - s * 0.6f, leafX + s * 1.5f, leafY)
            quadraticTo(leafX + s, leafY + s * 0.6f, leafX, leafY)
            close()
        }
        drawPath(path = leafPath, color = leafColor)
    }
}

private fun DrawScope.drawStars(
    width: Float,
    height: Float,
    progress: Float,
    stars: List<Triple<Float, Float, Float>>
) {
    for ((idx, star) in stars.withIndex()) {
        val x = star.first * width
        val y = star.second * height * 0.7f // Mayormente en la mitad superior
        val radius = star.third

        // Titileo suave desfasado
        val phase = (progress * 2.0 * PI + idx).toFloat()
        val alpha = (0.2f + 0.45f * ((sin(phase) + 1f) / 2f)).coerceIn(0.1f, 0.7f)

        drawCircle(
            color = Color(0xFFE0E7FF).copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
    }
}
