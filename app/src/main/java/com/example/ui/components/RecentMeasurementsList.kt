package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.model.Measurement
import com.example.ui.theme.SensorAltitudeColor
import com.example.ui.theme.SensorHumidityColor
import com.example.ui.theme.SensorLightColor
import com.example.ui.theme.SensorPressureColor
import com.example.ui.theme.SensorTemperatureColor
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentMeasurementsList(
    measurements: List<Measurement>,
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showExportDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_measurements_list"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            // Encabezado de la Sección
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "ÚLTIMAS 10 MEDICIONES",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Registros recibidos desde Firebase RTDB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showExportDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar CSV",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Exportar CSV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${measurements.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (measurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Esperando mediciones desde Firebase...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    measurements.forEachIndexed { index, item ->
                        val isLatest = index == 0

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLatest) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isLatest) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("measurement_item_$index")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Fila Superior: Marca de tiempo e indicador de orden
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isLatest) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = if (isLatest) "#1 • Más reciente" else "#${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${item.formattedTime}  (${item.formattedDate})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Fila Inferior: Chips con los valores exactos registrados
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    MeasurementValueTag(
                                        icon = Icons.Default.Thermostat,
                                        label = "Temp",
                                        value = "${String.format(Locale.US, "%.1f", item.temperatura)}°C",
                                        color = SensorTemperatureColor
                                    )

                                    MeasurementValueTag(
                                        icon = Icons.Default.WaterDrop,
                                        label = "Hum",
                                        value = "${String.format(Locale.US, "%.1f", item.humedad)}%",
                                        color = SensorHumidityColor
                                    )

                                    MeasurementValueTag(
                                        icon = Icons.Default.Compress,
                                        label = "Pres",
                                        value = "${String.format(Locale.US, "%.1f", item.presion)} hPa",
                                        color = SensorPressureColor
                                    )

                                    MeasurementValueTag(
                                        icon = Icons.Default.LightMode,
                                        label = "Luz",
                                        value = "${String.format(Locale.US, "%.0f", item.luz)} lux",
                                        color = SensorLightColor
                                    )

                                    MeasurementValueTag(
                                        icon = Icons.Default.Air,
                                        label = "Alt",
                                        value = "${String.format(Locale.US, "%.1f", item.altitud)} m",
                                        color = SensorAltitudeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        CsvExportDialog(
            measurements = measurements,
            onDismiss = { showExportDialog = false },
            onShowSnackbar = onShowSnackbar
        )
    }
}

@Composable
private fun MeasurementValueTag(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        }
    }
}
