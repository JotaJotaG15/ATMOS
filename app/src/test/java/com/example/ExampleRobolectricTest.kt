package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Measurement
import com.example.model.WeatherIconType
import com.example.model.WeatherPredictionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ATMOS", appName)
    }

    @Test
    fun `test measurement calculations`() {
        val m = Measurement(
            temperatura = 25.0,
            humedad = 60.0,
            presion = 1013.25,
            luz = 800.0,
            altitud = 150.0
        )

        assertTrue(m.feelsLike > 20.0)
        assertTrue(m.dewPoint > 10.0 && m.dewPoint < 25.0)
        assertNotNull(m.formattedTime)
        assertNotNull(m.formattedDate)
    }

    @Test
    fun `test weather prediction engine detects storm condition`() {
        val stormMeasurement = Measurement(
            temperatura = 19.0,
            humedad = 88.0,
            presion = 998.0,
            luz = 120.0,
            altitud = 150.0
        )

        val forecast = WeatherPredictionEngine.calculateForecast(stormMeasurement)
        assertEquals(WeatherIconType.THUNDERSTORM, forecast.iconType)
        assertTrue(forecast.headline.contains("Tormenta") || forecast.headline.contains("Lluvia"))
    }

    @Test
    fun `test alert settings has all five configurable variables`() {
        val settings = com.example.model.AlertSettings()
        // Verify all 5 variables exist with their units
        assertEquals("°C", settings.temperatureThreshold.unit)
        assertEquals("%", settings.humidityThreshold.unit)
        assertEquals("hPa", settings.pressureThreshold.unit)
        assertEquals("lux", settings.lightThreshold.unit)
        assertEquals("m", settings.altitudeThreshold.unit)

        // Verify independent toggling
        val modified = settings.copy(
            altitudeThreshold = settings.altitudeThreshold.copy(enabled = false),
            temperatureThreshold = settings.temperatureThreshold.copy(min = 15f, max = 28f)
        )
        assertTrue(modified.temperatureThreshold.enabled)
        org.junit.Assert.assertFalse(modified.altitudeThreshold.enabled)
        assertEquals(15f, modified.temperatureThreshold.min)
        assertEquals(28f, modified.temperatureThreshold.max)
    }

    @Test
    fun `test csv export generation and local saving`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val list = listOf(
            Measurement(
                temperatura = 21.5,
                humedad = 55.0,
                presion = 1014.2,
                luz = 350.0,
                altitud = 120.0,
                timestamp = 1791568000000L
            ),
            Measurement(
                temperatura = 22.0,
                humedad = 54.0,
                presion = 1014.0,
                luz = 380.0,
                altitud = 120.0,
                timestamp = 1791568005000L
            )
        )

        val csvString = com.example.util.CsvExporter.generateCsvString(list)
        assertTrue(csvString.contains("Timestamp_ms,Fecha,Hora,Temperatura_C,Humedad_Pct"))
        assertTrue(csvString.contains("21.50,55.00,1014.20,350.00,120.00"))
        assertTrue(csvString.contains("22.00,54.00,1014.00,380.00,120.00"))

        val file = com.example.util.CsvExporter.saveCsvLocally(context, list)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        assertTrue(file.name.endsWith(".csv"))
        assertTrue(file.readText().contains("Timestamp_ms"))
    }

    @Test
    fun `test dual temperature and humidity 24h metric`() {
        val dualMetric = com.example.model.ChartMetric.DUAL_TEMP_HUM
        assertTrue(dualMetric.label.contains("Temp + Humedad"))
        assertEquals("°C / %", dualMetric.unit)
    }

    @Test
    fun `test zambretti engine generates hourly and daily forecasts with friendly advice`() {
        val measurement = Measurement(
            temperatura = 24.0,
            humedad = 50.0,
            presion = 1018.0,
            luz = 650.0,
            altitud = 100.0
        )

        val extended = com.example.model.ZambrettiEngine.generateExtendedForecast(
            current = measurement,
            pressureDelta3h = 1.0
        )

        assertNotNull(extended.friendlyGreeting)
        assertNotNull(extended.friendlyTip)
        assertTrue(extended.friendlyGreeting.isNotBlank())
        assertEquals(24, extended.hourlyList.size)
        assertEquals(7, extended.dailyList.size)
        assertTrue(extended.zambrettiCode in 1..26)
        assertNotNull(extended.atmosphericEffect)
    }

    @Test
    fun `test dashboard navigation tabs exist and have correct labels`() {
        val tabs = com.example.ui.DashboardTab.values()
        assertEquals(3, tabs.size)
        assertEquals("Estación", tabs[0].label)
        assertEquals("Pronóstico", tabs[1].label)
        assertEquals("Gráficos", tabs[2].label)
    }

    @Test
    fun `test real station data predicts stable fine weather without false rain alarms`() {
        // Exact real measurement from station's Firebase RTDB
        val stationMeasurement = Measurement(
            altitud = -32.1,
            humedad = 51.0,
            luz = 325.8,
            presion = 1017.1,
            temperatura = 21.3
        )

        val forecast = com.example.model.WeatherPredictionEngine.calculateForecast(stationMeasurement)
        val extended = com.example.model.ZambrettiEngine.generateExtendedForecast(stationMeasurement, pressureDelta3h = 0.0)

        // 1017.1 hPa with 51% humidity in daylight must be pleasant / sunny, NEVER a storm or rain alarm!
        assertTrue(forecast.isStable)
        assertTrue(forecast.iconType == WeatherIconType.SUNNY || forecast.iconType == WeatherIconType.PARTLY_CLOUDY)
        assertTrue(extended.friendlyGreeting.contains("hermoso día") || extended.friendlyGreeting.contains("agradable"))
        assertTrue(extended.atmosphericEffect == com.example.model.AtmosphericEffectType.SUN_RAYS)
        assertTrue(extended.zambrettiCode in 1..8)
    }
}



