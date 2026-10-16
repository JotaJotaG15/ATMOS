package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Modelo de datos para las lecturas de los sensores de la estación meteorológica.
 */
data class Measurement(
    val temperatura: Double = 22.5,
    val humedad: Double = 55.0,
    val presion: Double = 1013.25,
    val luz: Double = 450.0,
    val altitud: Double = 152.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Calcula la sensación térmica aproximada (°C) usando fórmula simplificada de Steadman.
     */
    val feelsLike: Double
        get() {
            return if (temperatura >= 20.0) {
                val e = (humedad / 100.0) * 6.105 * kotlin.math.exp((17.27 * temperatura) / (237.7 + temperatura))
                temperatura + 0.33 * e - 4.00
            } else {
                temperatura
            }
        }

    /**
     * Calcula el punto de rocío (°C) mediante aproximación de Magnus-Tetens.
     */
    val dewPoint: Double
        get() {
            val a = 17.27
            val b = 237.7
            val alpha = ((a * temperatura) / (b + temperatura)) + ln((humedad.coerceIn(1.0, 100.0) / 100.0))
            return (b * alpha) / (a - alpha)
        }
}

enum class WeatherIconType {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    RAINY,
    THUNDERSTORM,
    CLEAR_NIGHT
}

data class WeatherForecastInfo(
    val headline: String,
    val description: String,
    val iconType: WeatherIconType,
    val barometricTrend: String,
    val isStable: Boolean
)

/**
 * Motor de predicción meteorológica basado en la física de Zambretti, presión barométrica, humedad y luz.
 */
object WeatherPredictionEngine {
    fun calculateForecast(
        current: Measurement,
        previousPressure: Double? = null
    ): WeatherForecastInfo {
        val pressureDelta = if (previousPressure != null) current.presion - previousPressure else 0.0
        return ZambrettiEngine.calculateUnifiedForecast(current, pressureDelta)
    }
}

enum class AtmosphericEffectType {
    SUN_RAYS,
    RAIN_DROPS,
    DRIFTING_LEAVES,
    CLEAR_STARS
}

data class HourlyForecastItem(
    val hourLabel: String,
    val timestamp: Long,
    val temperature: Double,
    val humidity: Double,
    val rainProbability: Int,
    val condition: String,
    val iconType: WeatherIconType
)

data class DailyForecastItem(
    val dayLabel: String,
    val dateLabel: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainProbability: Int,
    val condition: String,
    val iconType: WeatherIconType,
    val summary: String
)

data class ExtendedForecastData(
    val friendlyGreeting: String,
    val friendlyTip: String,
    val zambrettiCode: Int,
    val zambrettiForecast: String,
    val barometricTrendDesc: String,
    val detailedDescription: String,
    val hourlyList: List<HourlyForecastItem>,
    val dailyList: List<DailyForecastItem>,
    val atmosphericEffect: AtmosphericEffectType
)

enum class SensorType(val displayName: String, val unit: String) {
    TEMPERATURE("Temperatura", "°C"),
    HUMIDITY("Humedad", "%"),
    PRESSURE("Presión", "hPa"),
    LIGHT("Luz", "lux"),
    ALTITUDE("Altitud", "m"),
    TIMESTAMP("Última Medición", "")
}

data class SensorThreshold(
    val enabled: Boolean = true,
    val min: Float,
    val max: Float,
    val unit: String
)

data class AlertSettings(
    val notificationsEnabled: Boolean = true,
    val temperatureThreshold: SensorThreshold = SensorThreshold(true, min = 10f, max = 34f, unit = "°C"),
    val humidityThreshold: SensorThreshold = SensorThreshold(true, min = 25f, max = 80f, unit = "%"),
    val pressureThreshold: SensorThreshold = SensorThreshold(true, min = 995f, max = 1030f, unit = "hPa"),
    val lightThreshold: SensorThreshold = SensorThreshold(true, min = 10f, max = 12000f, unit = "lux"),
    val altitudeThreshold: SensorThreshold = SensorThreshold(true, min = -100f, max = 2500f, unit = "m"),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

enum class AlertSeverity {
    WARNING,
    CRITICAL
}

data class AlertEvent(
    val id: String,
    val sensorType: SensorType,
    val title: String,
    val message: String,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: AlertSeverity = AlertSeverity.WARNING
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

enum class ChartTimeRange(val label: String) {
    LIVE("En Vivo"),
    HOURS_24("24 Horas"),
    DAYS_7("7 Días")
}

enum class ChartMetric(val label: String, val unit: String) {
    DUAL_TEMP_HUM("Temp + Humedad (24h)", "°C / %"),
    TEMPERATURE("Temperatura", "°C"),
    HUMIDITY("Humedad", "%"),
    PRESSURE("Presión", "hPa"),
    LIGHT("Luz", "lux")
}

enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

enum class DataSourceMode {
    FIREBASE_REALTIME,
    SIMULATOR
}

data class FirebaseConfig(
    val databaseUrl: String = "https://estacionmeteorologica1-default-rtdb.firebaseio.com",
    val nodePath: String = "mediciones",
    val authToken: String = ""
)
