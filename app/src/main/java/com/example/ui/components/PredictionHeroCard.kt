package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.ConnectionStatus
import com.example.model.DataSourceMode
import com.example.model.Measurement
import com.example.model.WeatherForecastInfo
import com.example.model.WeatherIconType
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PredictionHeroCard(
    forecast: WeatherForecastInfo,
    measurement: Measurement,
    connectionStatus: ConnectionStatus,
    dataSourceMode: DataSourceMode,
    modifier: Modifier = Modifier
) {
    // Animación de pulso para el indicador de transmisión en vivo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val gradientColors = when (forecast.iconType) {
        WeatherIconType.SUNNY -> listOf(
            Color(0xFF0C243B),
            Color(0xFF133B5C),
            Color(0xFF1E3A5F)
        )
        WeatherIconType.THUNDERSTORM -> listOf(
            Color(0xFF1F112E),
            Color(0xFF281C3D),
            Color(0xFF141E30)
        )
        WeatherIconType.RAINY -> listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
        WeatherIconType.CLEAR_NIGHT -> listOf(
            Color(0xFF0B132B),
            Color(0xFF1C2541),
            Color(0xFF3A506B)
        )
        else -> listOf(
            Color(0xFF112233),
            Color(0xFF1A365D),
            Color(0xFF2B4C7E)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_prediction_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Barra superior: Encabezado de Predicción + Indicador de Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "ATMOS • PREDICCIÓN METEOROLÓGICA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Estado de Conexión
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val statusColor = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Color(0xFF34D399)
                            ConnectionStatus.CONNECTING -> Color(0xFFFBBF24)
                            ConnectionStatus.DISCONNECTED -> Color(0xFF94A3B8)
                            ConnectionStatus.ERROR -> Color(0xFFF87171)
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dataSourceMode == DataSourceMode.FIREBASE_REALTIME) {
                                when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "Firebase RTDB (En Vivo)"
                                    ConnectionStatus.CONNECTING -> "Conectando..."
                                    ConnectionStatus.DISCONNECTED -> "Desconectado"
                                    ConnectionStatus.ERROR -> "Error Firebase"
                                }
                            } else {
                                "Simulador Activo"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fila Principal: Icono del Clima + Titular de la Predicción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contenedor del Icono
                    val (iconVector, iconTint) = getWeatherIconAndTint(forecast.iconType)

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(iconTint.copy(alpha = 0.15f))
                            .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = forecast.headline,
                            tint = iconTint,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = forecast.headline,
                            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                            label = "headlineAnim"
                        ) { headline ->
                            Text(
                                text = headline,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = forecast.barometricTrend,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción detallada de la predicción
                Text(
                    text = forecast.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chips Informativos Rápidos: Sensación Térmica, Punto de Rocío, Presión
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricPill(
                        icon = Icons.Default.Thermostat,
                        label = "Sensación",
                        value = "${String.format(Locale.US, "%.1f", measurement.feelsLike)}°C",
                        tint = Color(0xFFFF8A65)
                    )

                    MetricPill(
                        icon = Icons.Default.WaterDrop,
                        label = "P. Rocío",
                        value = "${String.format(Locale.US, "%.1f", measurement.dewPoint)}°C",
                        tint = Color(0xFF4FC3F7)
                    )

                    MetricPill(
                        icon = Icons.Default.Compress,
                        label = "Barómetro",
                        value = "${String.format(Locale.US, "%.0f", measurement.presion)} hPa",
                        tint = Color(0xFFB39DDB)
                    )

                    MetricPill(
                        icon = Icons.Default.Air,
                        label = "Altitud",
                        value = "${String.format(Locale.US, "%.0f", measurement.altitud)} m",
                        tint = Color(0xFF81C784)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

private fun getWeatherIconAndTint(type: WeatherIconType): Pair<ImageVector, Color> {
    return when (type) {
        WeatherIconType.SUNNY -> Icons.Default.WbSunny to Color(0xFFFBBF24)
        WeatherIconType.PARTLY_CLOUDY -> Icons.Default.Cloud to Color(0xFF67E8F9)
        WeatherIconType.CLOUDY -> Icons.Default.Cloud to Color(0xFF94A3B8)
        WeatherIconType.RAINY -> Icons.Default.WaterDrop to Color(0xFF38BDF8)
        WeatherIconType.THUNDERSTORM -> Icons.Default.Thunderstorm to Color(0xFFA78BFA)
        WeatherIconType.CLEAR_NIGHT -> Icons.Default.NightsStay to Color(0xFF818CF8)
    }
}
