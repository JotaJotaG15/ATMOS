package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Measurement
import com.example.model.SensorType
import com.example.ui.theme.SensorAltitudeColor
import com.example.ui.theme.SensorHumidityColor
import com.example.ui.theme.SensorLightColor
import com.example.ui.theme.SensorPressureColor
import com.example.ui.theme.SensorTemperatureColor
import com.example.ui.theme.SensorTimestampColor
import java.util.Locale

@Composable
fun SensorCard(
    sensorType: SensorType,
    measurement: Measurement,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor, valueText, unitText, statusBadge, secondaryDetail) = getSensorDetails(sensorType, measurement)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag("sensor_card_${sensorType.name.lowercase()}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Fila Superior: Icono con halo + Nombre + Badge de Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = sensorType.displayName,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = sensorType.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }

                    // Badge de estado
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = statusBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fila Central: Valor Grande + Unidad
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = valueText,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "sensorValueAnim"
                    ) { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    if (unitText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unitText,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Detalle inferior (rango, referencia o descripción contextual)
                Text(
                    text = secondaryDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class SensorDataPresentation(
    val icon: ImageVector,
    val accentColor: Color,
    val valueText: String,
    val unitText: String,
    val statusBadge: String,
    val secondaryDetail: String
)

private fun getSensorDetails(sensor: SensorType, m: Measurement): SensorDataPresentation {
    return when (sensor) {
        SensorType.TEMPERATURE -> {
            val badge = when {
                m.temperatura > 32.0 -> "Caluroso"
                m.temperatura < 15.0 -> "Fresco"
                else -> "Agradable"
            }
            SensorDataPresentation(
                icon = Icons.Default.Thermostat,
                accentColor = SensorTemperatureColor,
                valueText = String.format(Locale.US, "%.1f", m.temperatura),
                unitText = "°C",
                statusBadge = badge,
                secondaryDetail = "Sensación: ${String.format(Locale.US, "%.1f", m.feelsLike)}°C"
            )
        }
        SensorType.HUMIDITY -> {
            val badge = when {
                m.humedad > 70.0 -> "Húmedo"
                m.humedad < 30.0 -> "Seco"
                else -> "Confort"
            }
            SensorDataPresentation(
                icon = Icons.Default.WaterDrop,
                accentColor = SensorHumidityColor,
                valueText = String.format(Locale.US, "%.1f", m.humedad),
                unitText = "%",
                statusBadge = badge,
                secondaryDetail = "Punto rocío: ${String.format(Locale.US, "%.1f", m.dewPoint)}°C"
            )
        }
        SensorType.PRESSURE -> {
            val badge = when {
                m.presion > 1020.0 -> "Alta"
                m.presion < 1008.0 -> "Baja"
                else -> "Normal"
            }
            SensorDataPresentation(
                icon = Icons.Default.Compress,
                accentColor = SensorPressureColor,
                valueText = String.format(Locale.US, "%.1f", m.presion),
                unitText = "hPa",
                statusBadge = badge,
                secondaryDetail = "Nivel del mar: ~1013 hPa"
            )
        }
        SensorType.LIGHT -> {
            val badge = when {
                m.luz > 2000.0 -> "Sol Pleno"
                m.luz > 200.0 -> "Luz Día"
                m.luz > 20.0 -> "Penumbra"
                else -> "Noche"
            }
            SensorDataPresentation(
                icon = Icons.Default.LightMode,
                accentColor = SensorLightColor,
                valueText = String.format(Locale.US, "%.0f", m.luz),
                unitText = "lux",
                statusBadge = badge,
                secondaryDetail = "Fotocélula LDR / BH1750"
            )
        }
        SensorType.ALTITUDE -> {
            SensorDataPresentation(
                icon = Icons.Default.Landscape,
                accentColor = SensorAltitudeColor,
                valueText = String.format(Locale.US, "%.0f", m.altitud),
                unitText = "m",
                statusBadge = "Barométrica",
                secondaryDetail = "Calculada vía fórmula hipsométrica"
            )
        }
        SensorType.TIMESTAMP -> {
            SensorDataPresentation(
                icon = Icons.Default.AccessTime,
                accentColor = SensorTimestampColor,
                valueText = m.formattedTime,
                unitText = "",
                statusBadge = "En Tiempo Real",
                secondaryDetail = m.formattedDate
            )
        }
    }
}
