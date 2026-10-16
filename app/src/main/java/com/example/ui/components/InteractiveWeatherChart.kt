package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartMetric
import com.example.model.ChartTimeRange
import com.example.model.Measurement
import com.example.ui.theme.SensorHumidityColor
import com.example.ui.theme.SensorLightColor
import com.example.ui.theme.SensorPressureColor
import com.example.ui.theme.SensorTemperatureColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InteractiveWeatherChart(
    selectedMetric: ChartMetric,
    onMetricSelected: (ChartMetric) -> Unit,
    timeRange: ChartTimeRange,
    onTimeRangeSelected: (ChartTimeRange) -> Unit,
    dataPoints: List<Measurement>,
    modifier: Modifier = Modifier
) {
    val isDual = selectedMetric == ChartMetric.DUAL_TEMP_HUM

    val accentColor = when (selectedMetric) {
        ChartMetric.DUAL_TEMP_HUM -> SensorTemperatureColor
        ChartMetric.TEMPERATURE -> SensorTemperatureColor
        ChartMetric.HUMIDITY -> SensorHumidityColor
        ChartMetric.PRESSURE -> SensorPressureColor
        ChartMetric.LIGHT -> SensorLightColor
    }

    val tempValues by remember(dataPoints) { derivedStateOf { dataPoints.map { it.temperatura } } }
    val humValues by remember(dataPoints) { derivedStateOf { dataPoints.map { it.humedad } } }

    val singleMetricValues by remember(dataPoints, selectedMetric) {
        derivedStateOf {
            dataPoints.map { m ->
                when (selectedMetric) {
                    ChartMetric.DUAL_TEMP_HUM, ChartMetric.TEMPERATURE -> m.temperatura
                    ChartMetric.HUMIDITY -> m.humedad
                    ChartMetric.PRESSURE -> m.presion
                    ChartMetric.LIGHT -> m.luz
                }
            }
        }
    }

    var activeTouchIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_weather_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            // Fila 1: Pestañas de Selección de Métrica (incluyendo Tendencias Duales 24h)
            ScrollableTabRow(
                selectedTabIndex = selectedMetric.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                ChartMetric.entries.forEach { metric ->
                    val isSelected = metric == selectedMetric
                    val metricColor = when (metric) {
                        ChartMetric.DUAL_TEMP_HUM -> SensorTemperatureColor
                        ChartMetric.TEMPERATURE -> SensorTemperatureColor
                        ChartMetric.HUMIDITY -> SensorHumidityColor
                        ChartMetric.PRESSURE -> SensorPressureColor
                        ChartMetric.LIGHT -> SensorLightColor
                    }

                    Tab(
                        selected = isSelected,
                        onClick = { onMetricSelected(metric) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) metricColor.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) metricColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("metric_tab_${metric.name.lowercase()}")
                    ) {
                        Text(
                            text = "${metric.label} (${metric.unit})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) metricColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila 2: Selector de Rango Temporal ("En Vivo", "24 Horas", "7 Días")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rango Temporal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartTimeRange.entries.forEach { range ->
                        val isSelected = range == timeRange
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onTimeRangeSelected(range)
                                activeTouchIndex = null
                            },
                            label = {
                                Text(
                                    text = range.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                selectedLabelColor = accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                selectedBorderColor = accentColor
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("time_range_${range.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 3: Indicadores de Leyenda si es Dual
            if (isDual) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendIndicator(color = SensorTemperatureColor, label = "Temperatura (°C)")
                    LegendIndicator(color = SensorHumidityColor, label = "Humedad Relativa (%)")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Fila 4: Tooltip Interactivo Activo o Estadísticas
            val activeIndex = activeTouchIndex
            if (activeIndex != null && activeIndex in dataPoints.indices) {
                val touchedPoint = dataPoints[activeIndex]
                val timeStr = when (timeRange) {
                    ChartTimeRange.LIVE -> SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(touchedPoint.timestamp))
                    ChartTimeRange.HOURS_24 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(touchedPoint.timestamp))
                    ChartTimeRange.DAYS_7 -> SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(touchedPoint.timestamp))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lectura ($timeStr):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isDual) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Temp: ${String.format(Locale.US, "%.1f", touchedPoint.temperatura)}°C",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SensorTemperatureColor
                                )
                                Text(
                                    text = "Hum: ${String.format(Locale.US, "%.1f", touchedPoint.humedad)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SensorHumidityColor
                                )
                            }
                        } else {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", singleMetricValues[activeIndex])} ${selectedMetric.unit}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            } else {
                if (isDual) {
                    val minT = tempValues.minOrNull() ?: 0.0
                    val maxT = tempValues.maxOrNull() ?: 0.0
                    val minH = humValues.minOrNull() ?: 0.0
                    val maxH = humValues.maxOrNull() ?: 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatLabel(label = "Temp 24h", value = "${String.format(Locale.US, "%.1f", minT)}°C - ${String.format(Locale.US, "%.1f", maxT)}°C")
                        StatLabel(label = "Hum 24h", value = "${String.format(Locale.US, "%.0f", minH)}% - ${String.format(Locale.US, "%.0f", maxH)}%")
                    }
                } else {
                    val minVal = singleMetricValues.minOrNull() ?: 0.0
                    val maxVal = singleMetricValues.maxOrNull() ?: 1.0
                    val avgVal = if (singleMetricValues.isNotEmpty()) singleMetricValues.average() else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatLabel(label = "Mínimo", value = "${String.format(Locale.US, "%.1f", minVal)} ${selectedMetric.unit}")
                        StatLabel(label = "Promedio", value = "${String.format(Locale.US, "%.1f", avgVal)} ${selectedMetric.unit}")
                        StatLabel(label = "Máximo", value = "${String.format(Locale.US, "%.1f", maxVal)} ${selectedMetric.unit}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas de Gráfico de Líneas con Curvas Bézier Suaves (Soporte Dual y Simple)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            ) {
                if (dataPoints.size < 2) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Recopilando datos de Firebase...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
                            .pointerInput(dataPoints) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        activeTouchIndex = findClosestDataIndex(offset.x, size.width.toFloat(), dataPoints.size)
                                    }
                                )
                            }
                            .pointerInput(dataPoints) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        activeTouchIndex = findClosestDataIndex(offset.x, size.width.toFloat(), dataPoints.size)
                                    },
                                    onDrag = { change, _ ->
                                        activeTouchIndex = findClosestDataIndex(change.position.x, size.width.toFloat(), dataPoints.size)
                                    }
                                )
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val paddingY = canvasHeight * 0.12f
                        val effectiveHeight = canvasHeight - (paddingY * 2)

                        // 1. Líneas de cuadrícula horizontales
                        val gridSteps = 3
                        for (i in 0..gridSteps) {
                            val y = paddingY + (effectiveHeight * i / gridSteps)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        if (isDual) {
                            // DIBUJAR MODO DUAL: Curva de Humedad (Azul) + Curva de Temperatura (Naranja)
                            val humPoints = calculateNormalizedPoints(humValues, canvasWidth, canvasHeight, paddingY, effectiveHeight)
                            val tempPoints = calculateNormalizedPoints(tempValues, canvasWidth, canvasHeight, paddingY, effectiveHeight)

                            // Curva Humedad
                            drawBezierCurve(humPoints, SensorHumidityColor, canvasHeight)
                            // Curva Temperatura
                            drawBezierCurve(tempPoints, SensorTemperatureColor, canvasHeight)

                            // Puntos y crosshair interactivo
                            val touchIdx = activeTouchIndex ?: (tempPoints.size - 1)
                            if (touchIdx in tempPoints.indices && touchIdx in humPoints.indices) {
                                val pTemp = tempPoints[touchIdx]
                                val pHum = humPoints[touchIdx]

                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(pTemp.x, 0f),
                                    end = Offset(pTemp.x, canvasHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )

                                drawHighlightPoint(pTemp, SensorTemperatureColor)
                                drawHighlightPoint(pHum, SensorHumidityColor)
                            }
                        } else {
                            // DIBUJAR MODO INDIVIDUAL
                            val points = calculateNormalizedPoints(singleMetricValues, canvasWidth, canvasHeight, paddingY, effectiveHeight)
                            drawBezierCurve(points, accentColor, canvasHeight)

                            val touchIdx = activeTouchIndex ?: (points.size - 1)
                            if (touchIdx in points.indices) {
                                val touchedPoint = points[touchIdx]
                                drawLine(
                                    color = accentColor.copy(alpha = 0.6f),
                                    start = Offset(touchedPoint.x, 0f),
                                    end = Offset(touchedPoint.x, canvasHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                                drawHighlightPoint(touchedPoint, accentColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Toca o arrastra sobre el gráfico para comparar hora por hora",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun calculateNormalizedPoints(
    values: List<Double>,
    width: Float,
    height: Float,
    paddingY: Float,
    effectiveHeight: Float
): List<Offset> {
    if (values.isEmpty()) return emptyList()
    val minVal = values.minOrNull() ?: 0.0
    val maxVal = values.maxOrNull() ?: 1.0
    val range = if (maxVal == minVal) 1.0 else maxVal - minVal

    return values.mapIndexed { index, value ->
        val x = (index.toFloat() / (values.size - 1).coerceAtLeast(1).toFloat()) * width
        val normalizedY = ((value - minVal) / range).toFloat()
        val y = (height - paddingY) - (normalizedY * effectiveHeight)
        Offset(x, y)
    }
}

private fun DrawScope.drawBezierCurve(points: List<Offset>, color: Color, canvasHeight: Float) {
    if (points.size < 2) return
    val strokePath = Path()
    val fillPath = Path()

    strokePath.moveTo(points.first().x, points.first().y)
    fillPath.moveTo(points.first().x, canvasHeight)
    fillPath.lineTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val cp1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
        val cp2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

        strokePath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p1.x, p1.y)
        fillPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p1.x, p1.y)
    }

    fillPath.lineTo(points.last().x, canvasHeight)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.28f),
                color.copy(alpha = 0.0f)
            )
        )
    )

    drawPath(
        path = strokePath,
        color = color,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawHighlightPoint(point: Offset, color: Color) {
    drawCircle(
        color = color.copy(alpha = 0.25f),
        radius = 12.dp.toPx(),
        center = point
    )
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = point
    )
    drawCircle(
        color = color,
        radius = 3.5.dp.toPx(),
        center = point
    )
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun findClosestDataIndex(touchX: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    val fraction = (touchX / width).coerceIn(0f, 1f)
    return (fraction * (count - 1)).toInt().coerceIn(0, count - 1)
}
