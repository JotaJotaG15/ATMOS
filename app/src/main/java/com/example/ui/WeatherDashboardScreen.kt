package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartMetric
import com.example.model.SensorType
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.AlertHistorySheet
import com.example.ui.components.AlertSettingsSheet
import com.example.ui.components.AtmosphericBackgroundOverlay
import com.example.ui.components.ExtendedForecastSection
import com.example.ui.components.FirebaseConfigSheet
import com.example.ui.components.FriendlyWelcomeBanner
import com.example.ui.components.InteractiveWeatherChart
import com.example.ui.components.PredictionHeroCard
import com.example.ui.components.RecentMeasurementsList
import com.example.ui.components.SensorCard
import com.example.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

enum class DashboardTab(val label: String, val icon: ImageVector, val tag: String) {
    STATION("Estación", Icons.Default.Sensors, "tab_station"),
    FORECAST("Pronóstico", Icons.Default.WbSunny, "tab_forecast"),
    CHARTS("Gráficos", Icons.AutoMirrored.Filled.ShowChart, "tab_charts")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboardScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val currentMeasurement by viewModel.currentMeasurement.collectAsState()
    val weatherForecast by viewModel.weatherForecast.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val dataSourceMode by viewModel.dataSourceMode.collectAsState()
    val firebaseConfig by viewModel.firebaseConfig.collectAsState()
    val alertSettings by viewModel.alertSettings.collectAsState()
    val alertHistory by viewModel.alertHistory.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val selectedChartMetric by viewModel.selectedChartMetric.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val recentMeasurements by viewModel.recentMeasurements.collectAsState()
    val extendedForecast by viewModel.extendedForecast.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Control de hojas modales
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showFirebaseSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    // Control de pestaña/sección actual
    var currentTab by remember { mutableStateOf(DashboardTab.STATION) }

    // Manejo de retroceso para volver a la pestaña de inicio/estación
    BackHandler(enabled = currentTab != DashboardTab.STATION) {
        currentTab = DashboardTab.STATION
    }

    // Lanzador para permisos de notificaciones push en Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            if (isGranted) {
                snackbarHostState.showSnackbar("Notificaciones push locales habilitadas.")
            } else {
                snackbarHostState.showSnackbar("Las alertas no podrán mostrar avisos push sin este permiso.")
            }
        }
    }

    val backgroundGradient = if (isDarkMode) {
        listOf(
            Color(0xFF070C14),
            Color(0xFF0F1A28),
            Color(0xFF132235)
        )
    } else {
        listOf(
            Color(0xFFE8F1FA),
            Color(0xFFF1F5F9),
            Color(0xFFF8FAFC)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_atmos_launcher_1792024541485),
                            contentDescription = "ATMOS Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ATMOS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Estación Meteorológica",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Botón: Configuración de Firebase RTDB
                    IconButton(
                        onClick = { showFirebaseSheet = true },
                        modifier = Modifier.testTag("action_firebase_config")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Configurar Firebase",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón: Historial de Alertas con Badge
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("action_alert_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (alertHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(alertHistory.size.coerceAtMost(99).toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial de alertas",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Botón: Ajustes de Alertas y Umbrales
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("action_alert_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Ajustar umbrales",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Botón: Modo Oscuro / Claro
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("action_toggle_theme")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                DashboardTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backgroundGradient))
                .padding(paddingValues)
        ) {
            // Efecto atmosférico sutil y amigable según el clima (rayos de sol, lluvia, hojas de viento, estrellas)
            AtmosphericBackgroundOverlay(
                effectType = extendedForecast.atmosphericEffect
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                when (currentTab) {
                    DashboardTab.STATION -> {
                        // 1. BANNER AMIGABLE DE BIENVENIDA Y CONSEJO PERSONALIZADO
                        FriendlyWelcomeBanner(
                            greeting = extendedForecast.friendlyGreeting,
                            tip = extendedForecast.friendlyTip,
                            currentMeasurement = currentMeasurement,
                            effectType = extendedForecast.atmosphericEffect,
                            onNavigateToForecast = { currentTab = DashboardTab.FORECAST }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. TARJETA PRINCIPAL: Estado actual y pronóstico sintetizado
                        PredictionHeroCard(
                            forecast = weatherForecast,
                            measurement = currentMeasurement,
                            connectionStatus = connectionStatus,
                            dataSourceMode = dataSourceMode
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. ENCABEZADO DE MEDICIONES DE SENSORES
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MEDICIONES DE SENSORES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "Nodo: ${firebaseConfig.nodePath}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // TARJETAS INDEPENDIENTES CON BORDES REDONDEADOS (16dp)
                        // Fila 1: Temperatura y Humedad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SensorCard(
                                sensorType = SensorType.TEMPERATURE,
                                measurement = currentMeasurement,
                                onClick = {
                                    viewModel.setChartMetric(ChartMetric.TEMPERATURE)
                                    currentTab = DashboardTab.CHARTS
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SensorCard(
                                sensorType = SensorType.HUMIDITY,
                                measurement = currentMeasurement,
                                onClick = {
                                    viewModel.setChartMetric(ChartMetric.HUMIDITY)
                                    currentTab = DashboardTab.CHARTS
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fila 2: Presión Barométrica y Luz
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SensorCard(
                                sensorType = SensorType.PRESSURE,
                                measurement = currentMeasurement,
                                onClick = {
                                    viewModel.setChartMetric(ChartMetric.PRESSURE)
                                    currentTab = DashboardTab.CHARTS
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SensorCard(
                                sensorType = SensorType.LIGHT,
                                measurement = currentMeasurement,
                                onClick = {
                                    viewModel.setChartMetric(ChartMetric.LIGHT)
                                    currentTab = DashboardTab.CHARTS
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fila 3: Altitud y Timestamp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SensorCard(
                                sensorType = SensorType.ALTITUDE,
                                measurement = currentMeasurement,
                                onClick = { showSettingsSheet = true },
                                modifier = Modifier.weight(1f)
                            )
                            SensorCard(
                                sensorType = SensorType.TIMESTAMP,
                                measurement = currentMeasurement,
                                onClick = { showHistorySheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Banner de Control Rápido de Alertas y Conexión
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Alertas Inteligentes en Tiempo Real",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (alertSettings.notificationsEnabled) "Push locales activas con umbrales personalizados" else "Alertas desactivadas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { showSettingsSheet = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("manage_alerts_button")
                                ) {
                                    Text("Ajustar", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    DashboardTab.FORECAST -> {
                        // SECCIÓN EXCLUSIVA DE PRONÓSTICO (POR HORA 24H Y POR DÍA 7 DÍAS CON ZAMBRETTI)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PRONÓSTICO METEOROLÓGICO",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Ecuación de Zambretti & Modelado por Hora/Día",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "Z = ${extendedForecast.zambrettiCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ExtendedForecastSection(
                            forecastData = extendedForecast
                        )
                    }

                    DashboardTab.CHARTS -> {
                        // SECCIÓN EXCLUSIVA DE GRÁFICOS E HISTORIAL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HISTORIAL Y ANÁLISIS DINÁMICO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "Tiempo Real (Firebase)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        InteractiveWeatherChart(
                            selectedMetric = selectedChartMetric,
                            onMetricSelected = { viewModel.setChartMetric(it) },
                            timeRange = selectedTimeRange,
                            onTimeRangeSelected = { viewModel.setTimeRange(it) },
                            dataPoints = chartData
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        RecentMeasurementsList(
                            measurements = recentMeasurements,
                            onShowSnackbar = { msg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Configuración de Alertas
    if (showSettingsSheet) {
        AlertSettingsSheet(
            initialSettings = alertSettings,
            hasNotificationPermission = viewModel.hasNotificationPermission(),
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onSaveSettings = { updated ->
                viewModel.updateAlertSettings(updated)
            },
            onSendTestNotification = {
                if (!viewModel.hasNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.sendTestNotification()
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    // Modal Sheet: Conexión Firebase Realtime Database
    if (showFirebaseSheet) {
        FirebaseConfigSheet(
            currentConfig = firebaseConfig,
            dataSourceMode = dataSourceMode,
            connectionStatus = connectionStatus,
            onConnectFirebase = { config ->
                viewModel.connectToFirebase(config)
                scope.launch {
                    snackbarHostState.showSnackbar("Conectando a Firebase RTDB: ${config.nodePath}")
                }
            },
            onStartSimulator = {
                viewModel.startSimulator()
                scope.launch {
                    snackbarHostState.showSnackbar("Modo Simulador de Sensores IoT activado.")
                }
            },
            onDismiss = { showFirebaseSheet = false }
        )
    }

    // Modal Sheet: Historial de Alertas
    if (showHistorySheet) {
        AlertHistorySheet(
            alertEvents = alertHistory,
            onClearHistory = {
                viewModel.clearAlertHistory()
                scope.launch {
                    snackbarHostState.showSnackbar("Historial de alertas limpiado.")
                }
            },
            onDismiss = { showHistorySheet = false }
        )
    }
}
