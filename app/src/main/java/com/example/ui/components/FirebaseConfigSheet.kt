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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.DataSourceMode
import com.example.model.FirebaseConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseConfigSheet(
    currentConfig: FirebaseConfig,
    dataSourceMode: DataSourceMode,
    connectionStatus: ConnectionStatus,
    onConnectFirebase: (FirebaseConfig) -> Unit,
    onStartSimulator: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var url by remember { mutableStateOf(currentConfig.databaseUrl) }
    var nodePath by remember { mutableStateOf(currentConfig.nodePath) }
    var authToken by remember { mutableStateOf(currentConfig.authToken) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Conexión Firebase Realtime Database",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nodo 'mediciones' en tiempo real",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estado actual
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Color(0xFF34D399)
                            ConnectionStatus.CONNECTING -> Color(0xFFFBBF24)
                            ConnectionStatus.DISCONNECTED -> Color(0xFF94A3B8)
                            ConnectionStatus.ERROR -> Color(0xFFF87171)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (dataSourceMode == DataSourceMode.FIREBASE_REALTIME) {
                                when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "Conectado a Firebase"
                                    ConnectionStatus.CONNECTING -> "Estableciendo conexión..."
                                    ConnectionStatus.DISCONNECTED -> "Desconectado"
                                    ConnectionStatus.ERROR -> "Error de conexión"
                                }
                            } else {
                                "Modo Simulador de Sensores IoT"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (dataSourceMode == DataSourceMode.FIREBASE_REALTIME) "RTDB Activa" else "Hardware Simulado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: URL de la base de datos
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL de Firebase Realtime Database") },
                placeholder = { Text("https://mi-estacion-meteo-rtdb.firebaseio.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_url_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Nodo de mediciones
            OutlinedTextField(
                value = nodePath,
                onValueChange = { nodePath = it },
                label = { Text("Nombre del Nodo") },
                placeholder = { Text("mediciones") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_node_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo opcional: Auth Token / Secret
            OutlinedTextField(
                value = authToken,
                onValueChange = { authToken = it },
                label = { Text("Token Secreto / Auth (Opcional si es pública)") },
                placeholder = { Text("Dejar en blanco para lectura abierta") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_auth_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón: Conectar a Firebase
            Button(
                onClick = {
                    onConnectFirebase(
                        FirebaseConfig(
                            databaseUrl = url.trim(),
                            nodePath = nodePath.trim().ifEmpty { "mediciones" },
                            authToken = authToken.trim()
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connect_firebase_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conectar a Firebase Realtime Database", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón: Activar Simulador IoT
            OutlinedButton(
                onClick = {
                    onStartSimulator()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activate_simulator_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usar Modo Simulador de Sensores (Offline / Demo)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 La aplicación escucha en vivo el nodo 'mediciones' y extrae temperatura, humedad, presión, luz, altitud y timestamp. En modo simulador, emula el envío en vivo de una placa ESP32/Arduino.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}
