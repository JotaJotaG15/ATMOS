package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

object ZambrettiEngine {

    /**
     * Sanitiza la altitud de la estación. Muchos sensores barométricos (e.g. BMP280 en placas IoT)
     * calculan altitud por firmware asumiendo 1013.25 hPa estándar, produciendo valores negativos
     * cuando la presión local es alta (e.g. -32m con 1017 hPa).
     */
    fun sanitizeAltitude(altitudeMeters: Double): Double {
        if (altitudeMeters.isNaN() || altitudeMeters < -150.0 || altitudeMeters > 9000.0) {
            return 0.0
        }
        return altitudeMeters.coerceAtLeast(0.0)
    }

    /**
     * Calcula la presión atmosférica reducida a nivel del mar (QNH)
     * según la fórmula barométrica estándar internacional.
     */
    fun calculateSeaLevelPressure(stationPressure: Double, altitudeMeters: Double, tempCelsius: Double): Double {
        val cleanAlt = sanitizeAltitude(altitudeMeters)
        if (cleanAlt == 0.0) return stationPressure
        val tempKelvin = (tempCelsius + 273.15).coerceIn(230.0, 330.0)
        val factor = 1.0 - (0.0065 * cleanAlt) / tempKelvin
        return stationPressure * factor.pow(-5.257)
    }

    /**
     * Resuelve el algoritmo de Zambretti (Negretti & Zambra) de forma matemática estricta y monótona:
     * - Alta presión (>=1020 hPa): Z=1..4 (Tiempo muy estable, soleado y despejado)
     * - Presión media-alta (1014..1019 hPa): Z=5..8 (Buen tiempo, agradable con sol o nubes leves)
     * - Presión normal (1008..1013 hPa): Z=9..13 (Tiempo variable, templado)
     * - Presión baja (1000..1007 hPa): Z=14..18 (Inestable, probabilidad de lluvias/chubascos)
     * - Muy baja presión (<1000 hPa): Z=19..26 (Temporal, precipitaciones continuas o tormentas)
     */
    fun calculateZambretti(
        pSea: Double,
        pressureDelta3h: Double,
        isSummer: Boolean = true
    ): Pair<Int, String> {
        // Mapeo base para presión normalizada a nivel del mar (1030 hPa = 1, 970 hPa = 26)
        val normalizedZ = (1030.0 - pSea) * (25.0 / 60.0) + 1.0

        val zCalculated = when {
            // Barómetro en caída rápida (empeora el pronóstico)
            pressureDelta3h < -0.8 -> {
                normalizedZ + 3.0 + (abs(pressureDelta3h) * 1.2)
            }
            // Barómetro en ascenso rápido (mejora el pronóstico)
            pressureDelta3h > 0.8 -> {
                normalizedZ - 3.0 - (pressureDelta3h * 1.2)
            }
            // Barómetro estable
            else -> {
                normalizedZ
            }
        }

        val zNum = zCalculated.roundToInt().coerceIn(1, 26)

        val forecastText = when (zNum) {
            1 -> "Tiempo muy estable y despejado continuo"
            2 -> "Buen tiempo continuo y agradable"
            3 -> "Tiempo mejorando sensiblemente, cielos claros"
            4 -> "Buen tiempo con cielos despejados"
            5 -> "Tiempo agradable con sol y brisas ligeras"
            6 -> "Tiempo agradable con intervalos nubosos"
            7 -> "Variable con tendencia a despejarse"
            8 -> "Nubosidad dispersa, tiempo favorable"
            9 -> "Cielos con intervalos de sol"
            10 -> "Tiempo templado, nubosidad variable"
            11 -> "Algo inestable con vientos leves"
            12 -> "Inestable con nubosidad en aumento"
            13 -> "Nublado con probabilidad de lloviznas"
            14 -> "Chubascos matinales o aislados"
            15 -> "Lluvia ligera a intervalos"
            16 -> "Inestable con precipitaciones dispersas"
            17 -> "Lluvias intermitentes y ambiente fresco"
            18 -> "Lluvias frecuentes con cielo cubierto"
            19 -> "Inestable con probabilidad de tormenta"
            20 -> "Cielo cubierto con lluvias persistentes"
            21 -> "Nublado con precipitaciones continuas"
            22 -> "Lluvias intensas con ráfagas de viento"
            23 -> "Frente tormentoso con precipitaciones"
            24 -> "Temporal de lluvia y viento fresco"
            25 -> "Temporal severo con fuertes lluvias"
            else -> "Tormenta severa con ráfagas de viento"
        }

        return Pair(zNum, forecastText)
    }

    /**
     * Calcula la probabilidad de lluvia (0% a 100%) combinando la física de Zambretti,
     * la humedad relativa de los sensores, el déficit de punto de rocío y la tendencia barométrica.
     */
    fun calculateRainProbability(
        zCode: Int,
        pSea: Double,
        humedad: Double,
        dewPointSpread: Double,
        pressureDelta: Double
    ): Int {
        var prob = (zCode - 1) * (85.0 / 25.0) + 5.0

        // Factor de humedad relativa (55% es neutro)
        val humFactor = (humedad - 55.0) * 0.35
        prob += humFactor

        // Factor de déficit de punto de rocío (T - Td)
        if (dewPointSpread < 3.0) {
            prob += (3.0 - dewPointSpread) * 6.0
        } else if (dewPointSpread > 7.0) {
            prob -= (dewPointSpread - 7.0) * 2.5
        }

        // Factor de presión absoluta
        if (pSea >= 1018.0) {
            prob -= 10.0
        } else if (pSea <= 1005.0) {
            prob += 15.0
        }

        // Tendencia barométrica
        if (pressureDelta < -1.0) prob += 10.0
        else if (pressureDelta > 1.0) prob -= 10.0

        return prob.roundToInt().coerceIn(0, 100)
    }

    /**
     * Genera un informe unificado para la tarjeta principal (Hero Card)
     * perfectamente sincronizado con el motor físico de Zambretti.
     */
    fun calculateUnifiedForecast(
        current: Measurement,
        pressureDelta: Double = 0.0
    ): WeatherForecastInfo {
        val pSea = calculateSeaLevelPressure(current.presion, current.altitud, current.temperatura)
        val (zCode, zForecast) = calculateZambretti(pSea, pressureDelta)
        val dewPointSpread = current.temperatura - current.dewPoint
        val rainProb = calculateRainProbability(zCode, pSea, current.humedad, dewPointSpread, pressureDelta)
        val isDay = current.luz > 40.0

        val trend = when {
            pressureDelta > 1.5 -> "Presión en rápido ascenso (+${String.format(Locale.US, "%.1f", pressureDelta)} hPa/3h)"
            pressureDelta > 0.4 -> "Presión en leve ascenso (+${String.format(Locale.US, "%.1f", pressureDelta)} hPa/3h)"
            pressureDelta < -1.5 -> "Presión en rápida caída (${String.format(Locale.US, "%.1f", pressureDelta)} hPa/3h)"
            pressureDelta < -0.4 -> "Presión descendiendo (${String.format(Locale.US, "%.1f", pressureDelta)} hPa/3h)"
            else -> "Presión atmosférica estable (~${String.format(Locale.US, "%.1f", current.presion)} hPa)"
        }

        val (headline, iconType, isStable) = when {
            (current.presion < 1005.0 && current.humedad > 80.0) || rainProb >= 65 || zCode >= 22 -> {
                Triple("Tormenta en Camino", WeatherIconType.THUNDERSTORM, false)
            }
            (current.presion < 1010.0 && current.humedad > 70.0) || rainProb >= 40 || zCode >= 15 -> {
                Triple("Probabilidad de Lluvia", WeatherIconType.RAINY, false)
            }
            current.humedad >= 75.0 && current.luz < 200.0 && isDay -> {
                Triple("Cielo Cubierto y Húmedo", WeatherIconType.CLOUDY, true)
            }
            !isDay -> {
                if (current.humedad > 80.0) {
                    Triple("Noche Nublada", WeatherIconType.CLOUDY, true)
                } else {
                    Triple("Noche Despejada y Fresca", WeatherIconType.CLEAR_NIGHT, true)
                }
            }
            rainProb < 25 && (zCode in 1..8 || current.presion >= 1014.0) -> {
                Triple("Día Soleado y Agradable", WeatherIconType.SUNNY, true)
            }
            else -> {
                Triple("Parcialmente Nublado", WeatherIconType.PARTLY_CLOUDY, true)
            }
        }

        val description = "$zForecast con $trend. Humedad al ${current.humedad.roundToInt()}% y punto de rocío en ${String.format(Locale.US, "%.1f", current.dewPoint)}°C (probabilidad de lluvia: $rainProb%)."

        return WeatherForecastInfo(
            headline = headline,
            description = description,
            iconType = iconType,
            barometricTrend = trend,
            isStable = isStable
        )
    }

    /**
     * Genera la predicción extendida completa (horaria a 24 horas y diaria a 7 días)
     * utilizando la física de Zambretti y las lecturas en tiempo real de los sensores.
     */
    fun generateExtendedForecast(
        current: Measurement,
        pressureDelta3h: Double = 0.0
    ): ExtendedForecastData {
        val pSea = calculateSeaLevelPressure(current.presion, current.altitud, current.temperatura)
        val (zCode, zForecast) = calculateZambretti(pSea, pressureDelta3h)
        val dewPointSpread = current.temperatura - current.dewPoint
        val rainProb = calculateRainProbability(zCode, pSea, current.humedad, dewPointSpread, pressureDelta3h)
        val isDay = current.luz > 40.0

        // 1. Mensaje de bienvenida amigable y consejo según el clima
        val (friendlyGreeting, friendlyTip, effectType) = generateFriendlyAdvice(current, zCode, rainProb, isDay)

        // 2. Descripción detallada del pronóstico
        val barometricTrendDesc = when {
            pressureDelta3h > 1.2 -> "Presión en rápido ascenso (+${String.format(Locale.US, "%.1f", pressureDelta3h)} hPa/3h)"
            pressureDelta3h > 0.4 -> "Presión en leve ascenso (+${String.format(Locale.US, "%.1f", pressureDelta3h)} hPa/3h)"
            pressureDelta3h < -1.2 -> "Presión en rápida caída (${String.format(Locale.US, "%.1f", pressureDelta3h)} hPa/3h)"
            pressureDelta3h < -0.4 -> "Presión descendiendo (${String.format(Locale.US, "%.1f", pressureDelta3h)} hPa/3h)"
            else -> "Presión atmosférica estable (~${String.format(Locale.US, "%.1f", current.presion)} hPa)"
        }

        val detailedDescription = "Ecuación de Zambretti (Z=$zCode): $zForecast. $barometricTrendDesc. " +
                "Nivel de humedad al ${current.humedad.roundToInt()}% y punto de rocío de ${String.format(Locale.US, "%.1f", current.dewPoint)}°C."

        // 3. Generación de Pronóstico por Hora (Próximas 24 horas)
        val hourlyList = generateHourlyForecast(current, zCode, rainProb)

        // 4. Generación de Pronóstico Extendido por Día (Próximos 7 días)
        val dailyList = generateDailyForecast(current, zCode, rainProb)

        return ExtendedForecastData(
            friendlyGreeting = friendlyGreeting,
            friendlyTip = friendlyTip,
            zambrettiCode = zCode,
            zambrettiForecast = zForecast,
            barometricTrendDesc = barometricTrendDesc,
            detailedDescription = detailedDescription,
            hourlyList = hourlyList,
            dailyList = dailyList,
            atmosphericEffect = effectType
        )
    }

    private fun generateFriendlyAdvice(
        current: Measurement,
        zCode: Int,
        rainProb: Int,
        isDay: Boolean
    ): Triple<String, String, AtmosphericEffectType> {
        val isCold = current.temperatura < 17.0
        val isHot = current.temperatura >= 28.0
        val isRainy = rainProb >= 45 || zCode in 15..26

        return when {
            isCold -> {
                Triple(
                    "🧥 ¿Por qué no llevás un abrigo hoy? Está bastante fresco afuera.",
                    "La temperatura registrada es de ${String.format(Locale.US, "%.1f", current.temperatura)}°C. Una campera o bufanda te mantendrán cómodo.",
                    AtmosphericEffectType.DRIFTING_LEAVES
                )
            }
            isRainy -> {
                Triple(
                    "🌧️ Parece que se vienen lluvias. ¿Por qué no llevás un paraguas hoy?",
                    "El barómetro y la humedad (${current.humedad.roundToInt()}%) indican probabilidad de precipitaciones. Tené precaución al salir.",
                    AtmosphericEffectType.RAIN_DROPS
                )
            }
            isHot -> {
                Triple(
                    "🥤 ¡Hoy va a ser un día muy caluroso! Recordá mantenerte bien hidratado.",
                    "La temperatura alcanza los ${String.format(Locale.US, "%.1f", current.temperatura)}°C. Llevá agua fresca y evitá la exposición solar prolongada.",
                    AtmosphericEffectType.SUN_RAYS
                )
            }
            !isDay -> {
                Triple(
                    "🌙 Buenas noches. El tiempo se mantiene estable y la noche está tranquila.",
                    "Temperatura agradable de ${String.format(Locale.US, "%.1f", current.temperatura)}°C con atmósfera calmada para descansar.",
                    AtmosphericEffectType.CLEAR_STARS
                )
            }
            rainProb < 25 && (zCode in 1..8 || current.presion >= 1014.0) -> {
                Triple(
                    "☀️ ¡Hoy va a ser un hermoso día para disfrutar al aire libre!",
                    "Condiciones óptimas: cielo abierto, excelente luminosidad (${current.luz.roundToInt()} lux) y temperatura agradable de ${String.format(Locale.US, "%.1f", current.temperatura)}°C.",
                    AtmosphericEffectType.SUN_RAYS
                )
            }
            else -> {
                Triple(
                    "🍃 Buen día. El tiempo está agradable y variable, ideal para tus actividades.",
                    "Atmósfera templada con ${current.humedad.roundToInt()}% de humedad relativa y presión de ${String.format(Locale.US, "%.1f", current.presion)} hPa.",
                    AtmosphericEffectType.DRIFTING_LEAVES
                )
            }
        }
    }

    private fun generateHourlyForecast(
        current: Measurement,
        zCode: Int,
        baseRainProb: Int
    ): List<HourlyForecastItem> {
        val list = mutableListOf<HourlyForecastItem>()
        val calendar = Calendar.getInstance().apply { timeInMillis = current.timestamp }
        val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val baseTemp = current.temperatura
        val baseHum = current.humedad

        // Curva diurna suave calculada como desfase respecto a la hora actual de la estación
        val currentRad = (currentHour - 5) * 2.0 * PI / 24.0
        val currentTempOffset = 3.5 * sin(currentRad)
        val currentHumOffset = -8.0 * sin(currentRad)
        val normalizedBaseTemp = baseTemp - currentTempOffset
        val normalizedBaseHum = baseHum - currentHumOffset

        for (i in 1..24) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val isHourDay = hourOfDay in 7..19

            val rad = (hourOfDay - 5) * 2.0 * PI / 24.0
            val tempOffset = 3.5 * sin(rad)
            val humOffset = -8.0 * sin(rad)

            val temp = ((normalizedBaseTemp + tempOffset) * 10).roundToInt() / 10.0
            val hum = ((normalizedBaseHum + humOffset).coerceIn(15.0, 95.0) * 10).roundToInt() / 10.0

            // Modulación de probabilidad de lluvia a lo largo del día
            val hourlyRainMod = if (hourOfDay in 14..18) 6 else if (hourOfDay in 2..6) -4 else 0
            val rainProb = (baseRainProb + hourlyRainMod).coerceIn(0, 100)

            val iconType = when {
                rainProb >= 70 -> WeatherIconType.THUNDERSTORM
                rainProb >= 40 -> WeatherIconType.RAINY
                hum >= 75 -> WeatherIconType.CLOUDY
                !isHourDay -> WeatherIconType.CLEAR_NIGHT
                hum >= 50 -> WeatherIconType.PARTLY_CLOUDY
                else -> WeatherIconType.SUNNY
            }

            val condition = when (iconType) {
                WeatherIconType.THUNDERSTORM -> "Tormentas"
                WeatherIconType.RAINY -> "Lluvia ligera"
                WeatherIconType.CLOUDY -> "Nublado"
                WeatherIconType.CLEAR_NIGHT -> "Despejado"
                WeatherIconType.PARTLY_CLOUDY -> "Parcialmente nublado"
                WeatherIconType.SUNNY -> "Soleado"
            }

            list.add(
                HourlyForecastItem(
                    hourLabel = hourFormat.format(calendar.time),
                    timestamp = calendar.timeInMillis,
                    temperature = temp,
                    humidity = hum,
                    rainProbability = rainProb,
                    condition = condition,
                    iconType = iconType
                )
            )
        }
        return list
    }

    private fun generateDailyForecast(
        current: Measurement,
        zCode: Int,
        baseRainProb: Int
    ): List<DailyForecastItem> {
        val list = mutableListOf<DailyForecastItem>()
        val calendar = Calendar.getInstance().apply { timeInMillis = current.timestamp }
        val dayNameFormat = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES"))
        val dateFormat = SimpleDateFormat("dd MMM", Locale.forLanguageTag("es-ES"))

        val baseTemp = current.temperatura
        val dayNamesSpanish = mapOf(
            "monday" to "Lunes", "tuesday" to "Martes", "wednesday" to "Miércoles",
            "thursday" to "Jueves", "friday" to "Viernes", "saturday" to "Sábado", "sunday" to "Domingo"
        )

        for (i in 0..6) {
            val isToday = i == 0
            val rawDayName = SimpleDateFormat("EEEE", Locale.US).format(calendar.time).lowercase()
            val dayName = if (isToday) "Hoy" else (dayNamesSpanish[rawDayName] ?: dayNameFormat.format(calendar.time))
            val dateLabel = dateFormat.format(calendar.time)

            // Onda sinóptica de presión atmosférica para los próximos días
            val synopticShift = 1.8 * sin((i + 1) * 0.7)
            val maxTemp = ((baseTemp + 3.2 + synopticShift) * 10).roundToInt() / 10.0
            val minTemp = ((baseTemp - 3.5 + synopticShift) * 10).roundToInt() / 10.0

            val rainProb = (baseRainProb + (i * 2 - 3)).coerceIn(0, 100)

            val iconType = when {
                rainProb >= 65 -> WeatherIconType.THUNDERSTORM
                rainProb >= 40 -> WeatherIconType.RAINY
                rainProb < 20 && maxTemp >= 20.0 -> WeatherIconType.SUNNY
                else -> WeatherIconType.PARTLY_CLOUDY
            }

            val condition = when (iconType) {
                WeatherIconType.THUNDERSTORM -> "Tormentas aisladas"
                WeatherIconType.RAINY -> "Chubascos dispersos"
                WeatherIconType.SUNNY -> "Soleado y templado"
                else -> "Nubosidad variable"
            }

            val summary = if (isToday) {
                "Máx: ${maxTemp}°C • Mín: ${minTemp}°C • Lluvia: ${rainProb}%"
            } else {
                "Templado con ${condition.lowercase()}"
            }

            list.add(
                DailyForecastItem(
                    dayLabel = dayName.replaceFirstChar { it.uppercase() },
                    dateLabel = dateLabel,
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    rainProbability = rainProb,
                    condition = condition,
                    iconType = iconType,
                    summary = summary
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }
}
