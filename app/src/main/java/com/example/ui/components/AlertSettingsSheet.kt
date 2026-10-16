package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSettings
import com.example.model.SensorThreshold
import com.example.ui.theme.SensorAltitudeColor
import com.example.ui.theme.SensorHumidityColor
import com.example.ui.theme.SensorLightColor
import com.example.ui.theme.SensorPressureColor
import com.example.ui.theme.SensorTemperatureColor
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsSheet(
    initialSettings: AlertSettings,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSaveSettings: (AlertSettings) -> Unit,
    onSendTestNotification: () -> Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentSettings by remember { mutableStateOf(initialSettings) }
    var testResultMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Configuración de Alertas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Umbrales y notificaciones push",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permiso de notificaciones
            if (!hasNotificationPermission) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permiso de Notificaciones Requerido",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Para recibir alertas cuando un sensor supere el umbral.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_permission_button")
                        ) {
                            Text("Activar", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Switch General de Alertas
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notificaciones Push Activas",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alertas en tiempo real al detectar anomalías",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = currentSettings.notificationsEnabled,
                        onCheckedChange = {
                            currentSettings = currentSettings.copy(notificationsEnabled = it)
                            onSaveSettings(currentSettings)
                        },
                        modifier = Modifier.testTag("global_notifications_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "UMBRALES POR SENSOR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Temperatura
            ThresholdRangeCard(
                icon = Icons.Default.Thermostat,
                accentColor = SensorTemperatureColor,
                title = "Temperatura",
                unit = "°C",
                valueRange = 0f..50f,
                threshold = currentSettings.temperatureThreshold,
                onThresholdChange = { updated ->
                    currentSettings = currentSettings.copy(temperatureThreshold = updated)
                    onSaveSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Humedad
            ThresholdRangeCard(
                icon = Icons.Default.WaterDrop,
                accentColor = SensorHumidityColor,
                title = "Humedad Relativa",
                unit = "%",
                valueRange = 10f..95f,
                threshold = currentSettings.humidityThreshold,
                onThresholdChange = { updated ->
                    currentSettings = currentSettings.copy(humidityThreshold = updated)
                    onSaveSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Presión
            ThresholdRangeCard(
                icon = Icons.Default.Compress,
                accentColor = SensorPressureColor,
                title = "Presión Barométrica",
                unit = "hPa",
                valueRange = 960f..1040f,
                threshold = currentSettings.pressureThreshold,
                onThresholdChange = { updated ->
                    currentSettings = currentSettings.copy(pressureThreshold = updated)
                    onSaveSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Luz
            ThresholdRangeCard(
                icon = Icons.Default.LightMode,
                accentColor = SensorLightColor,
                title = "Iluminación / Lux",
                unit = "lux",
                valueRange = 0f..20000f,
                threshold = currentSettings.lightThreshold,
                onThresholdChange = { updated ->
                    currentSettings = currentSettings.copy(lightThreshold = updated)
                    onSaveSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Altitud
            ThresholdRangeCard(
                icon = Icons.Default.Landscape,
                accentColor = SensorAltitudeColor,
                title = "Altitud",
                unit = "m",
                valueRange = -500f..5000f,
                threshold = currentSettings.altitudeThreshold,
                onThresholdChange = { updated ->
                    currentSettings = currentSettings.copy(altitudeThreshold = updated)
                    onSaveSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Opciones de Sonido y Vibración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sonido", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = currentSettings.soundEnabled,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(soundEnabled = it)
                                onSaveSettings(currentSettings)
                            },
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vibración", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = currentSettings.vibrationEnabled,
                            onCheckedChange = {
                                currentSettings = currentSettings.copy(vibrationEnabled = it)
                                onSaveSettings(currentSettings)
                            },
                            modifier = Modifier.testTag("vibration_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de Prueba de Notificación
            Button(
                onClick = {
                    val sent = onSendTestNotification()
                    testResultMsg = if (sent) "¡Notificación de prueba enviada con éxito!" else "Permiso denegado. Concede permiso para recibir alertas."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_notification_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Probar Notificación Ahora",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            testResultMsg?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ThresholdRangeCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    threshold: SensorThreshold,
    onThresholdChange: (SensorThreshold) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = threshold.enabled,
                    onCheckedChange = { onThresholdChange(threshold.copy(enabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                )
            }

            if (threshold.enabled) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mín: ${threshold.min.roundToInt()} $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Máx: ${threshold.max.roundToInt()} $unit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                RangeSlider(
                    value = threshold.min..threshold.max,
                    onValueChange = { range ->
                        onThresholdChange(threshold.copy(min = range.start, max = range.endInclusive))
                    },
                    valueRange = valueRange,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = accentColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
