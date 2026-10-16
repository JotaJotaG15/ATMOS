package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NightsStay
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
import com.example.model.AtmosphericEffectType
import com.example.model.Measurement
import java.util.Calendar

@Composable
fun FriendlyWelcomeBanner(
    greeting: String,
    tip: String,
    currentMeasurement: Measurement,
    effectType: AtmosphericEffectType,
    onNavigateToForecast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when (hourOfDay) {
        in 6..12 -> "¡Buenos días!"
        in 13..19 -> "¡Buenas tardes!"
        else -> "¡Buenas noches!"
    }

    val (icon, iconTint, gradientColors) = when (effectType) {
        AtmosphericEffectType.RAIN_DROPS -> Triple(
            Icons.Default.WaterDrop,
            Color(0xFF38BDF8),
            listOf(
                Color(0xFF0284C7).copy(alpha = 0.22f),
                Color(0xFF0369A1).copy(alpha = 0.12f),
                Color(0xFF0C4A6E).copy(alpha = 0.05f)
            )
        )
        AtmosphericEffectType.SUN_RAYS -> Triple(
            Icons.Default.WbSunny,
            Color(0xFFF59E0B),
            listOf(
                Color(0xFFF59E0B).copy(alpha = 0.22f),
                Color(0xFFEA580C).copy(alpha = 0.10f),
                Color(0xFFB45309).copy(alpha = 0.04f)
            )
        )
        AtmosphericEffectType.DRIFTING_LEAVES -> Triple(
            Icons.Default.Cloud,
            Color(0xFF10B981),
            listOf(
                Color(0xFF10B981).copy(alpha = 0.20f),
                Color(0xFF059669).copy(alpha = 0.10f),
                Color(0xFF047857).copy(alpha = 0.04f)
            )
        )
        AtmosphericEffectType.CLEAR_STARS -> Triple(
            Icons.Default.NightsStay,
            Color(0xFF818CF8),
            listOf(
                Color(0xFF6366F1).copy(alpha = 0.22f),
                Color(0xFF4F46E5).copy(alpha = 0.10f),
                Color(0xFF312E81).copy(alpha = 0.04f)
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("friendly_welcome_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .border(
                    width = 1.dp,
                    color = iconTint.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // Fila Superior: Saludo del día + Badge de Clima
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iconTint.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = timeGreeting,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = iconTint,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "ATMOS • Estación Meteorológica",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = iconTint.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "IA Clima",
                                style = MaterialTheme.typography.labelSmall,
                                color = iconTint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje Principal Amigable (e.g. "Hoy va a ser un hermoso día", "¿Por qué no llevás un abrigo hoy?")
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Consejo amigable o recomendación de vestimenta
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fila Inferior: Acceso directo a la sección de pronóstico
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToForecast() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver pronóstico por hora y 7 días",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a pronóstico",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
