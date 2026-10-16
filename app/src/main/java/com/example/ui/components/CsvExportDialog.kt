package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Measurement
import com.example.util.CsvExporter

@Composable
fun CsvExportDialog(
    measurements: List<Measurement>,
    onDismiss: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val csvContent = remember(measurements) { CsvExporter.generateCsvString(measurements) }
    var copiedFeedback by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("csv_export_dialog"),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Exportar a CSV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${measurements.size} registros preparados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "El archivo CSV contiene las variables de temperatura, humedad, presión, luz, altitud y marcas de tiempo para su apertura en Excel, Google Sheets o herramientas de análisis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cuadro de Vista Previa del CSV
                Text(
                    text = "VISTA PREVIA DEL ARCHIVO:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = csvContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (copiedFeedback) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Contenido CSV copiado al portapapeles",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val file = CsvExporter.saveCsvLocally(context, measurements)
                        CsvExporter.shareCsvFile(context, file)
                        onShowSnackbar("Archivo CSV guardado en el dispositivo: ${file.name}")
                        onDismiss()
                    } catch (e: Exception) {
                        onShowSnackbar("Error al exportar: ${e.message}")
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("share_csv_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar / Compartir")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    CsvExporter.copyToClipboard(context, csvContent)
                    copiedFeedback = true
                    onShowSnackbar("CSV copiado al portapapeles")
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("copy_csv_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copiar")
            }
        }
    )
}
