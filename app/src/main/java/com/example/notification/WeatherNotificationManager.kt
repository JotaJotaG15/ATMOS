package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.AlertEvent
import com.example.model.SensorType
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de notificaciones push locales para alertas de la estación meteorológica.
 */
class WeatherNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "weather_station_alerts_channel"
        const val CHANNEL_NAME = "Alertas Meteorológicas"
        const val CHANNEL_DESCRIPTION = "Notificaciones de umbrales críticos de temperatura, humedad, presión y luz"
        private const val ALERT_COOLDOWN_MS = 60_000L // 1 minuto de enfriamiento entre alertas del mismo sensor
    }

    private val lastAlertTimestamps = ConcurrentHashMap<SensorType, Long>()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Verifica si se tiene el permiso POST_NOTIFICATIONS (Android 13+).
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Emite una notificación push local si el permiso está concedido y se respetó el cooldown.
     */
    fun postAlertNotification(alert: AlertEvent, soundEnabled: Boolean = true, vibrationEnabled: Boolean = true): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val now = System.currentTimeMillis()
        val lastSent = lastAlertTimestamps[alert.sensorType] ?: 0L
        if (now - lastSent < ALERT_COOLDOWN_MS) {
            // Enfriamiento activo para evitar saturar al usuario
            return false
        }

        lastAlertTimestamps[alert.sensorType] = now

        val notificationId = alert.sensorType.ordinal + 1000

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ ${alert.title}")
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${alert.message}\nValor medido: ${alert.value} ${alert.sensorType.unit}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (!vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Notificación de prueba manual disparada desde los ajustes.
     */
    fun sendTestNotification(): Boolean {
        if (!hasNotificationPermission()) return false
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 Prueba de Alerta Meteorológica")
            .setContentText("El sistema de notificaciones push locales está activo y listo para alertar cambios en tus sensores.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(9999, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
