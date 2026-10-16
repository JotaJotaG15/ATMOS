package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.WeatherRepository
import com.example.model.AlertEvent
import com.example.model.AlertSettings
import com.example.model.ChartMetric
import com.example.model.ChartTimeRange
import com.example.model.ConnectionStatus
import com.example.model.DataSourceMode
import com.example.model.FirebaseConfig
import com.example.model.Measurement
import com.example.model.WeatherForecastInfo
import com.example.model.WeatherPredictionEngine
import com.example.notification.WeatherNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val notificationManager: WeatherNotificationManager
) : ViewModel() {

    // Medición actual en tiempo real
    val currentMeasurement: StateFlow<Measurement> = repository.currentMeasurement

    // Estado de conexión a Firebase / Simulador
    val connectionStatus: StateFlow<ConnectionStatus> = repository.connectionStatus
    val dataSourceMode: StateFlow<DataSourceMode> = repository.dataSourceMode
    val firebaseConfig: StateFlow<FirebaseConfig> = repository.firebaseConfig

    // Configuración de alertas y registro
    val alertSettings: StateFlow<AlertSettings> = repository.alertSettings
    val alertHistory: StateFlow<List<AlertEvent>> = repository.alertHistory

    // Configuración visual de gráficos interactivos (por defecto: Tendencias 24h Temp + Humedad)
    private val _selectedTimeRange = MutableStateFlow(ChartTimeRange.HOURS_24)
    val selectedTimeRange: StateFlow<ChartTimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedChartMetric = MutableStateFlow(ChartMetric.DUAL_TEMP_HUM)
    val selectedChartMetric: StateFlow<ChartMetric> = _selectedChartMetric.asStateFlow()

    // Modo oscuro (true por defecto para diseño nocturno/atmosférico moderno de Play Store)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Cálculo dinámico de la predicción meteorológica basada en barometría y física atmosférica
    val weatherForecast: StateFlow<WeatherForecastInfo> = combine(
        currentMeasurement,
        repository.pressureDelta3h
    ) { current, delta ->
        WeatherPredictionEngine.calculateForecast(current, previousPressure = current.presion - delta)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeatherPredictionEngine.calculateForecast(
            repository.currentMeasurement.value,
            previousPressure = repository.currentMeasurement.value.presion - repository.pressureDelta3h.value
        )
    )

    // Predicción extendida completa: Ecuación de Zambretti, consejos amigables, 24 horas y 7 días
    val extendedForecast: StateFlow<com.example.model.ExtendedForecastData> = combine(
        currentMeasurement,
        repository.pressureDelta3h
    ) { current, delta ->
        com.example.model.ZambrettiEngine.generateExtendedForecast(current, pressureDelta3h = delta)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.model.ZambrettiEngine.generateExtendedForecast(
            repository.currentMeasurement.value,
            pressureDelta3h = repository.pressureDelta3h.value
        )
    )

    // Datos históricos filtrados según el rango de tiempo seleccionado
    val chartData: StateFlow<List<Measurement>> = combine(
        _selectedTimeRange,
        repository.liveHistory,
        repository.history24h,
        repository.history7d
    ) { range, live, h24, d7 ->
        when (range) {
            ChartTimeRange.LIVE -> live
            ChartTimeRange.HOURS_24 -> h24
            ChartTimeRange.DAYS_7 -> d7
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.liveHistory.value
    )

    // Últimas 10 mediciones recibidas desde Firebase (en orden cronológico inverso)
    val recentMeasurements: StateFlow<List<Measurement>> = repository.liveHistory.map { list ->
        list.takeLast(10).reversed()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.liveHistory.value.takeLast(10).reversed()
    )

    fun setTimeRange(range: ChartTimeRange) {
        _selectedTimeRange.value = range
    }

    fun setChartMetric(metric: ChartMetric) {
        _selectedChartMetric.value = metric
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateAlertSettings(newSettings: AlertSettings) {
        repository.updateAlertSettings(newSettings)
    }

    fun clearAlertHistory() {
        repository.clearAlertHistory()
    }

    fun sendTestNotification(): Boolean {
        return notificationManager.sendTestNotification()
    }

    fun hasNotificationPermission(): Boolean {
        return notificationManager.hasNotificationPermission()
    }

    fun connectToFirebase(config: FirebaseConfig) {
        repository.connectToFirebase(config)
    }

    fun startSimulator() {
        repository.startSimulator()
    }

    class Factory(
        private val repository: WeatherRepository,
        private val notificationManager: WeatherNotificationManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(repository, notificationManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
