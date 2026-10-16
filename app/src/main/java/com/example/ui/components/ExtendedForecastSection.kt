package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.model.DailyForecastItem
import com.example.model.ExtendedForecastData
import com.example.model.HourlyForecastItem
import com.example.model.WeatherIconType
import com.example.ui.theme.SensorHumidityColor
import com.example.ui.theme.SensorTemperatureColor
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExtendedForecastSection(
    forecastData: ExtendedForecastData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("extended_forecast_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. BANNER AMIGABLE DE BIENVENIDA Y RECOMENDACIÓN DIARIA
        FriendlyWelcomeCard(
            greeting = forecastData.friendlyGreeting,
            tip = forecastData.friendlyTip
        )

        // 2. PRONÓSTICO POR HORA (PRÓXIMAS 24 HORAS)
        HourlyForecastCard(hourlyList = forecastData.hourlyList)

        // 3. PRONÓSTICO EXTENDIDO POR DÍA (PRÓXIMOS 7 DÍAS) & ZAMBRETTI
        DailyForecastCard(
            dailyList = forecastData.dailyList,
            zambrettiCode = forecastData.zambrettiCode,
            zambrettiForecast = forecastData.zambrettiForecast,
            detailedDescription = forecastData.detailedDescription
        )
    }
}

@Composable
private fun FriendlyWelcomeCard(
    greeting: String,
    tip: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friendly_welcome_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            Color(0xFF38BDF8).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONSEJO DEL DÍA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF34D399).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "IA Meteorológica",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(hourlyList: List<HourlyForecastItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRONÓSTICO POR HORA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Próximas 24 horas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila con desplazamiento horizontal de horas
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(hourlyList, key = { it.timestamp }) { item ->
                    HourlyItemPill(item)
                }
            }
        }
    }
}

@Composable
private fun HourlyItemPill(item: HourlyForecastItem) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.hourLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            val (icon, tint) = getForecastIconAndTint(item.iconType)
            Icon(
                imageVector = icon,
                contentDescription = item.condition,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${String.format(Locale.US, "%.1f", item.temperature)}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Probabilidad de precipitación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = SensorHumidityColor,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${item.rainProbability}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SensorHumidityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DailyForecastCard(
    dailyList: List<DailyForecastItem>,
    zambrettiCode: Int,
    zambrettiForecast: String,
    detailedDescription: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRONÓSTICO EXTENDIDO (7 DÍAS)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Zambretti Z=$zambrettiCode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detalle científico y descripción
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detailedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lista de los 7 días
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                dailyList.forEach { day ->
                    DailyRowItem(day)
                }
            }
        }
    }
}

@Composable
private fun DailyRowItem(day: DailyForecastItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Día y fecha
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = day.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Icono + Probabilidad de lluvia
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(80.dp)
            ) {
                val (icon, tint) = getForecastIconAndTint(day.iconType)
                Icon(
                    imageVector = icon,
                    contentDescription = day.condition,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (day.rainProbability > 15) {
                    Text(
                        text = "${day.rainProbability}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = SensorHumidityColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Temperaturas Máx y Mín con barra visual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${day.minTemp.roundToInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Barra de gradiente térmico
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    SensorHumidityColor.copy(alpha = 0.6f),
                                    SensorTemperatureColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${day.maxTemp.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun getForecastIconAndTint(type: WeatherIconType): Pair<ImageVector, Color> {
    return when (type) {
        WeatherIconType.SUNNY -> Icons.Default.WbSunny to Color(0xFFFBBF24)
        WeatherIconType.PARTLY_CLOUDY -> Icons.Default.Cloud to Color(0xFF67E8F9)
        WeatherIconType.CLOUDY -> Icons.Default.Cloud to Color(0xFF94A3B8)
        WeatherIconType.RAINY -> Icons.Default.WaterDrop to Color(0xFF38BDF8)
        WeatherIconType.THUNDERSTORM -> Icons.Default.Thunderstorm to Color(0xFFA78BFA)
        WeatherIconType.CLEAR_NIGHT -> Icons.Default.NightsStay to Color(0xFF818CF8)
    }
}
