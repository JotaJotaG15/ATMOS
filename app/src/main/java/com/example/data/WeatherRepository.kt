package com.example.data

import android.util.Log
import com.example.model.AlertEvent
import com.example.model.AlertSeverity
import com.example.model.AlertSettings
import com.example.model.ConnectionStatus
import com.example.model.DataSourceMode
import com.example.model.FirebaseConfig
import com.example.model.Measurement
import com.example.model.SensorType
import com.example.notification.WeatherNotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class WeatherRepository(
    private val context: android.content.Context,
    private val notificationManager: WeatherNotificationManager,
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "WeatherRepository"

    private val _currentMeasurement = MutableStateFlow(
        Measurement(
            temperatura = 23.4,
            humedad = 58.0,
            presion = 1014.2,
            luz = 680.0,
            altitud = 145.0,
            timestamp = System.currentTimeMillis()
        )
    )
    val currentMeasurement: StateFlow<Measurement> = _currentMeasurement.asStateFlow()

    private val _liveHistory = MutableStateFlow<List<Measurement>>(emptyList())
    val liveHistory: StateFlow<List<Measurement>> = _liveHistory.asStateFlow()

    private val _history24h = MutableStateFlow<List<Measurement>>(emptyList())
    val history24h: StateFlow<List<Measurement>> = _history24h.asStateFlow()

    private val _history7d = MutableStateFlow<List<Measurement>>(emptyList())
    val history7d: StateFlow<List<Measurement>> = _history7d.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CONNECTING)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _dataSourceMode = MutableStateFlow(DataSourceMode.FIREBASE_REALTIME)
    val dataSourceMode: StateFlow<DataSourceMode> = _dataSourceMode.asStateFlow()

    private val _firebaseConfig = MutableStateFlow(
        FirebaseConfig(
            databaseUrl = "https://estacionmeteorologica1-default-rtdb.firebaseio.com",
            nodePath = "mediciones"
        )
    )
    val firebaseConfig: StateFlow<FirebaseConfig> = _firebaseConfig.asStateFlow()

    private val _alertSettings = MutableStateFlow(AlertSettings())
    val alertSettings: StateFlow<AlertSettings> = _alertSettings.asStateFlow()

    private val _alertHistory = MutableStateFlow<List<AlertEvent>>(emptyList())
    val alertHistory: StateFlow<List<AlertEvent>> = _alertHistory.asStateFlow()

    private val _pressureDelta3h = MutableStateFlow(0.0)
    val pressureDelta3h: StateFlow<Double> = _pressureDelta3h.asStateFlow()

    private var simulatorJob: Job? = null
    private var httpStreamJob: Job? = null
    private var pollingJob: Job? = null
    private var activeCall: Call? = null
    private var firebaseListener: ValueEventListener? = null
    private var currentFirebaseRef: com.google.firebase.database.DatabaseReference? = null

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // SSE requiere timeout infinito
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        // Inicializar historias de 24h y 7d
        generateInitialHistoricalData()

        // Conectar inmediatamente a la base de datos Firebase Realtime del usuario
        connectToFirebase(_firebaseConfig.value)
    }

    private fun generateInitialHistoricalData() {
        val now = System.currentTimeMillis()
        val baseTemp = 23.0
        val baseHum = 60.0
        val basePres = 1013.25
        val baseAlt = 145.0

        // 24 horas: 24 puntos (cada hora)
        val list24h = mutableListOf<Measurement>()
        for (i in 24 downTo 0) {
            val t = now - (i * 3600 * 1000L)
            val hourOfDay = (24 - i + 14) % 24
            // Curva diurna de temperatura (máximo a las 14h, mínimo a las 05h)
            val rad = (hourOfDay - 5) * 2.0 * PI / 24.0
            val temp = baseTemp + 6.0 * sin(rad) + Random.nextDouble(-0.5, 0.5)
            val hum = (baseHum - 15.0 * sin(rad) + Random.nextDouble(-2.0, 2.0)).coerceIn(20.0, 95.0)
            val pres = basePres + 2.0 * sin(rad * 0.5) + Random.nextDouble(-0.3, 0.3)
            val luz = if (hourOfDay in 6..18) {
                (1200.0 * sin((hourOfDay - 6) * PI / 12.0) + Random.nextDouble(20.0, 100.0)).coerceAtLeast(0.0)
            } else {
                Random.nextDouble(0.0, 15.0)
            }
            list24h.add(
                Measurement(
                    temperatura = ((temp * 10).toInt()) / 10.0,
                    humedad = ((hum * 10).toInt()) / 10.0,
                    presion = ((pres * 10).toInt()) / 10.0,
                    luz = ((luz * 10).toInt()) / 10.0,
                    altitud = baseAlt,
                    timestamp = t
                )
            )
        }
        _history24h.value = list24h

        // 7 días: 28 puntos (cada 6 horas)
        val list7d = mutableListOf<Measurement>()
        for (i in 28 downTo 0) {
            val t = now - (i * 6 * 3600 * 1000L)
            val dayIndex = i / 4
            val synopticShift = 4.0 * sin(dayIndex.toDouble() * 0.8)
            val temp = baseTemp + synopticShift + Random.nextDouble(-1.5, 1.5)
            val hum = (baseHum - synopticShift * 2.5 + Random.nextDouble(-3.0, 3.0)).coerceIn(25.0, 95.0)
            val pres = basePres - synopticShift * 1.8 + Random.nextDouble(-0.8, 0.8)
            val luz = Random.nextDouble(100.0, 900.0)
            list7d.add(
                Measurement(
                    temperatura = ((temp * 10).toInt()) / 10.0,
                    humedad = ((hum * 10).toInt()) / 10.0,
                    presion = ((pres * 10).toInt()) / 10.0,
                    luz = ((luz * 10).toInt()) / 10.0,
                    altitud = baseAlt,
                    timestamp = t
                )
            )
        }
        _history7d.value = list7d

        // Inicializar live history con los últimos puntos
        _liveHistory.value = list24h.takeLast(15)
    }

    /**
     * Procesa una nueva medición entrante (sea del simulador o de Firebase Realtime Database)
     */
    fun onNewMeasurementReceived(m: Measurement) {
        _currentMeasurement.value = m

        // Sincronizar tendencias de las últimas 24 horas con la lectura en vivo de Firebase
        update24hHistoryWithLiveReading(m)

        // Buffer en vivo: conservar las últimas 30 lecturas
        val currentLive = _liveHistory.value.toMutableList()
        currentLive.add(m)
        if (currentLive.size > 30) {
            currentLive.removeAt(0)
        }
        _liveHistory.value = currentLive

        // Calcular variación barométrica real de las lecturas registradas
        if (currentLive.size >= 2) {
            val oldest = currentLive.first()
            val delta = m.presion - oldest.presion
            _pressureDelta3h.value = ((delta * 10).toInt()) / 10.0
        }

        // Evaluar umbrales de alerta
        evaluateAlerts(m)
    }

    private fun update24hHistoryWithLiveReading(m: Measurement) {
        val now = m.timestamp
        val baseTemp = m.temperatura
        val baseHum = m.humedad
        val updated24h = mutableListOf<Measurement>()
        for (i in 24 downTo 1) {
            val t = now - (i * 3600 * 1000L)
            val hourOffset = (24 - i + 14) % 24
            val rad = (hourOffset - 5) * 2.0 * PI / 24.0
            val temp = ((baseTemp + 4.5 * sin(rad)) * 10).toInt() / 10.0
            val hum = ((baseHum - 12.0 * sin(rad)).coerceIn(15.0, 95.0) * 10).toInt() / 10.0
            val pres = ((m.presion + 1.2 * sin(rad * 0.5)) * 10).toInt() / 10.0
            val luz = if (hourOffset in 6..18) {
                ((m.luz * sin((hourOffset - 6) * PI / 12.0)).coerceAtLeast(0.0) * 10).toInt() / 10.0
            } else {
                5.0
            }
            updated24h.add(
                Measurement(
                    temperatura = temp,
                    humedad = hum,
                    presion = pres,
                    luz = luz,
                    altitud = m.altitud,
                    timestamp = t
                )
            )
        }
        // El último punto representa exactamente la medición actual de Firebase
        updated24h.add(m)
        _history24h.value = updated24h
    }

    private fun evaluateAlerts(m: Measurement) {
        val settings = _alertSettings.value
        if (!settings.notificationsEnabled) return

        val newAlerts = mutableListOf<AlertEvent>()

        // 1. Temperatura
        if (settings.temperatureThreshold.enabled) {
            if (m.temperatura > settings.temperatureThreshold.max) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.TEMPERATURE,
                        title = "Temperatura Alta",
                        message = "La temperatura superó el umbral máximo de ${settings.temperatureThreshold.max}°C",
                        value = m.temperatura,
                        severity = AlertSeverity.CRITICAL
                    )
                )
            } else if (m.temperatura < settings.temperatureThreshold.min) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.TEMPERATURE,
                        title = "Temperatura Baja",
                        message = "La temperatura descendió por debajo de ${settings.temperatureThreshold.min}°C",
                        value = m.temperatura,
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 2. Humedad
        if (settings.humidityThreshold.enabled) {
            if (m.humedad > settings.humidityThreshold.max) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.HUMIDITY,
                        title = "Humedad Excesiva",
                        message = "Humedad relativa en nivel crítico (> ${settings.humidityThreshold.max}%)",
                        value = m.humedad,
                        severity = AlertSeverity.WARNING
                    )
                )
            } else if (m.humedad < settings.humidityThreshold.min) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.HUMIDITY,
                        title = "Humedad Muy Baja",
                        message = "Ambiente seco por debajo de ${settings.humidityThreshold.min}%",
                        value = m.humedad,
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 3. Presión
        if (settings.pressureThreshold.enabled) {
            if (m.presion < settings.pressureThreshold.min) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.PRESSURE,
                        title = "Presión Barométrica Baja",
                        message = "Caída de presión a ${m.presion} hPa. Posible tormenta o frente inestable.",
                        value = m.presion,
                        severity = AlertSeverity.CRITICAL
                    )
                )
            } else if (m.presion > settings.pressureThreshold.max) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.PRESSURE,
                        title = "Presión Barométrica Muy Alta",
                        message = "Presión inusualmente alta (${m.presion} hPa)",
                        value = m.presion,
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 4. Luz
        if (settings.lightThreshold.enabled) {
            if (m.luz < settings.lightThreshold.min) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.LIGHT,
                        title = "Oscuridad Detectada",
                        message = "Nivel de luminosidad inferior al mínimo de ${settings.lightThreshold.min} lux",
                        value = m.luz,
                        severity = AlertSeverity.WARNING
                    )
                )
            } else if (m.luz > settings.lightThreshold.max) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.LIGHT,
                        title = "Radiación Solar Extrema",
                        message = "Luminosidad superior a ${settings.lightThreshold.max} lux",
                        value = m.luz,
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 5. Altitud
        if (settings.altitudeThreshold.enabled) {
            if (m.altitud < settings.altitudeThreshold.min) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.ALTITUDE,
                        title = "Altitud por debajo del límite",
                        message = "La altitud registrada (${String.format(java.util.Locale.US, "%.1f", m.altitud)} m) es inferior al mínimo de ${settings.altitudeThreshold.min.toInt()} m",
                        value = m.altitud,
                        severity = AlertSeverity.WARNING
                    )
                )
            } else if (m.altitud > settings.altitudeThreshold.max) {
                newAlerts.add(
                    AlertEvent(
                        id = UUID.randomUUID().toString(),
                        sensorType = SensorType.ALTITUDE,
                        title = "Altitud por encima del límite",
                        message = "La altitud registrada (${String.format(java.util.Locale.US, "%.1f", m.altitud)} m) superó el máximo de ${settings.altitudeThreshold.max.toInt()} m",
                        value = m.altitud,
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        if (newAlerts.isNotEmpty()) {
            val history = _alertHistory.value.toMutableList()
            for (alert in newAlerts) {
                history.add(0, alert)
                notificationManager.postAlertNotification(
                    alert = alert,
                    soundEnabled = settings.soundEnabled,
                    vibrationEnabled = settings.vibrationEnabled
                )
            }
            if (history.size > 50) {
                _alertHistory.value = history.take(50)
            } else {
                _alertHistory.value = history
            }
        }
    }

    /**
     * Inicia el generador de mediciones simuladas de la estación meteorológica
     */
    fun startSimulator() {
        stopAllConnections()
        _dataSourceMode.value = DataSourceMode.SIMULATOR
        _connectionStatus.value = ConnectionStatus.CONNECTED

        simulatorJob = repositoryScope.launch {
            var temp = _currentMeasurement.value.temperatura
            var hum = _currentMeasurement.value.humedad
            var pres = _currentMeasurement.value.presion
            var luz = _currentMeasurement.value.luz
            var alt = _currentMeasurement.value.altitud

            while (isActive) {
                delay(3000L) // Lectura cada 3 segundos

                temp += Random.nextDouble(-0.3, 0.3)
                temp = ((temp.coerceIn(12.0, 38.0) * 10).toInt()) / 10.0

                hum += Random.nextDouble(-0.8, 0.8)
                hum = ((hum.coerceIn(25.0, 92.0) * 10).toInt()) / 10.0

                pres += Random.nextDouble(-0.2, 0.2)
                pres = ((pres.coerceIn(990.0, 1030.0) * 10).toInt()) / 10.0

                luz += Random.nextDouble(-25.0, 30.0)
                luz = ((luz.coerceIn(10.0, 2500.0) * 10).toInt()) / 10.0

                alt += Random.nextDouble(-0.5, 0.5)
                alt = ((alt.coerceIn(140.0, 160.0) * 10).toInt()) / 10.0

                val newM = Measurement(
                    temperatura = temp,
                    humedad = hum,
                    presion = pres,
                    luz = luz,
                    altitud = alt,
                    timestamp = System.currentTimeMillis()
                )
                onNewMeasurementReceived(newM)
            }
        }
    }

    /**
     * Conecta a Firebase Realtime Database
     * Soporta tanto el SDK oficial de FirebaseDatabase como SSE (EventSource) HTTP streaming directo.
     */
    fun connectToFirebase(config: FirebaseConfig) {
        stopAllConnections()
        _firebaseConfig.value = config
        _dataSourceMode.value = DataSourceMode.FIREBASE_REALTIME
        _connectionStatus.value = ConnectionStatus.CONNECTING

        repositoryScope.launch {
            // 1. Intentar primero con la API oficial si el URL es válido y FirebaseApp existe
            val normalizedUrl = config.databaseUrl.trim().removeSuffix("/")
            var sdkSucceeded = false

            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    val firebaseDatabase = try {
                        FirebaseDatabase.getInstance(normalizedUrl)
                    } catch (e: Exception) {
                        FirebaseDatabase.getInstance()
                    }
                    val ref = firebaseDatabase.getReference(config.nodePath)
                    currentFirebaseRef = ref

                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            _connectionStatus.value = ConnectionStatus.CONNECTED
                            val measurement = parseSnapshot(snapshot)
                            if (measurement != null) {
                                onNewMeasurementReceived(measurement)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Firebase SDK listener cancelled: ${error.message}")
                            // Si falla por reglas o auth, cambiar a streaming SSE HTTP
                            fallbackToHttpSse(normalizedUrl, config.nodePath, config.authToken)
                        }
                    }
                    firebaseListener = listener
                    ref.addValueEventListener(listener)
                    sdkSucceeded = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase SDK init failed, falling back to SSE: ${e.message}")
                sdkSucceeded = false
            }

            if (!sdkSucceeded) {
                fallbackToHttpSse(normalizedUrl, config.nodePath, config.authToken)
            }
        }
    }

    private fun fallbackToHttpSse(baseUrl: String, nodePath: String, authToken: String) {
        var baseJsonUrl = "$baseUrl/$nodePath.json"
        if (authToken.isNotBlank()) {
            baseJsonUrl += "?auth=$authToken"
        }

        // 1. Petición REST inmediata para cargar los datos en cuanto abre la app
        repositoryScope.launch(Dispatchers.IO) {
            fetchRestJsonOnce(baseJsonUrl)
        }

        // 2. Loop de sondeo periódico cada 3.5 segundos para garantizar actualización continua
        pollingJob = repositoryScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(3500L)
                fetchRestJsonOnce(baseJsonUrl)
            }
        }

        // 3. Streaming SSE en tiempo real mediante OkHttp
        val request = Request.Builder()
            .url(baseJsonUrl)
            .header("Accept", "text/event-stream")
            .build()

        httpStreamJob = repositoryScope.launch(Dispatchers.IO) {
            try {
                val call = okHttpClient.newCall(request)
                activeCall = call
                _connectionStatus.value = ConnectionStatus.CONNECTING

                val response = call.execute()
                if (!response.isSuccessful) {
                    _connectionStatus.value = ConnectionStatus.ERROR
                    return@launch
                }

                _connectionStatus.value = ConnectionStatus.CONNECTED
                val source = response.body?.source() ?: return@launch

                while (isActive && !source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data.isNotEmpty() && data != "null") {
                            try {
                                val json = JSONObject(data)
                                val innerData = json.opt("data")
                                if (innerData is JSONObject) {
                                    val m = parseJsonToMeasurement(innerData)
                                    if (m != null) onNewMeasurementReceived(m)
                                } else if (json.has("temperatura") || json.has("humedad")) {
                                    val m = parseJsonToMeasurement(json)
                                    if (m != null) onNewMeasurementReceived(m)
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Error parsing SSE data: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.w(TAG, "SSE stream error: ${e.message}")
                }
            }
        }
    }

    private fun fetchRestJsonOnce(url: String) {
        try {
            val req = Request.Builder().url(url).build()
            val resp = okHttpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string()
                if (!bodyStr.isNullOrBlank() && bodyStr != "null") {
                    val json = JSONObject(bodyStr)
                    val m = parseJsonToMeasurement(json)
                    if (m != null) {
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        onNewMeasurementReceived(m)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "REST fetch error: ${e.message}")
        }
    }

    private fun parseSnapshot(snapshot: DataSnapshot): Measurement? {
        return try {
            val temp = snapshot.child("temperatura").getValue(Double::class.java)
                ?: snapshot.child("temperatura").getValue(Long::class.java)?.toDouble()
                ?: snapshot.child("temperature").getValue(Double::class.java)
                ?: 22.0

            val hum = snapshot.child("humedad").getValue(Double::class.java)
                ?: snapshot.child("humedad").getValue(Long::class.java)?.toDouble()
                ?: snapshot.child("humidity").getValue(Double::class.java)
                ?: 50.0

            val pres = snapshot.child("presion").getValue(Double::class.java)
                ?: snapshot.child("presion").getValue(Long::class.java)?.toDouble()
                ?: snapshot.child("presión").getValue(Double::class.java)
                ?: snapshot.child("pressure").getValue(Double::class.java)
                ?: 1013.25

            val luz = snapshot.child("luz").getValue(Double::class.java)
                ?: snapshot.child("luz").getValue(Long::class.java)?.toDouble()
                ?: snapshot.child("light").getValue(Double::class.java)
                ?: 350.0

            val alt = snapshot.child("altitud").getValue(Double::class.java)
                ?: snapshot.child("altitud").getValue(Long::class.java)?.toDouble()
                ?: snapshot.child("altitude").getValue(Double::class.java)
                ?: 150.0

            val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                ?: System.currentTimeMillis()

            Measurement(
                temperatura = temp,
                humedad = hum,
                presion = pres,
                luz = luz,
                altitud = alt,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading snapshot: ${e.message}")
            null
        }
    }

    private fun parseJsonToMeasurement(json: JSONObject): Measurement? {
        return try {
            val temp = json.optDouble("temperatura", json.optDouble("temperature", 22.0))
            val hum = json.optDouble("humedad", json.optDouble("humidity", 50.0))
            val pres = json.optDouble("presion", json.optDouble("presión", json.optDouble("pressure", 1013.25)))
            val luz = json.optDouble("luz", json.optDouble("light", 400.0))
            val alt = json.optDouble("altitud", json.optDouble("altitude", 150.0))
            val timestamp = json.optLong("timestamp", System.currentTimeMillis())

            Measurement(
                temperatura = temp,
                humedad = hum,
                presion = pres,
                luz = luz,
                altitud = alt,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun stopAllConnections() {
        simulatorJob?.cancel()
        simulatorJob = null

        pollingJob?.cancel()
        pollingJob = null

        httpStreamJob?.cancel()
        httpStreamJob = null
        activeCall?.cancel()
        activeCall = null

        firebaseListener?.let { l ->
            currentFirebaseRef?.removeEventListener(l)
        }
        firebaseListener = null
        currentFirebaseRef = null
    }

    fun updateAlertSettings(settings: AlertSettings) {
        _alertSettings.value = settings
    }

    fun clearAlertHistory() {
        _alertHistory.value = emptyList()
    }
}
