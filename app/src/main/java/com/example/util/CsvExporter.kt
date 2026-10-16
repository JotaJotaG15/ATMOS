package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.Measurement
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    /**
     * Genera el contenido formateado en formato CSV estándar para las mediciones meteorológicas.
     */
    fun generateCsvString(measurements: List<Measurement>): String {
        val sb = StringBuilder()
        // Encabezados con nombres descriptivos y unidades
        sb.append("Timestamp_ms,Fecha,Hora,Temperatura_C,Humedad_Pct,Presion_hPa,Luz_lux,Altitud_m,Sensacion_Termica_C,Punto_Rocio_C\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        for (m in measurements) {
            val date = Date(m.timestamp)
            sb.append(m.timestamp).append(",")
            sb.append("\"").append(dateFormat.format(date)).append("\",")
            sb.append("\"").append(timeFormat.format(date)).append("\",")
            sb.append(String.format(Locale.US, "%.2f", m.temperatura)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.humedad)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.presion)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.luz)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.altitud)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.feelsLike)).append(",")
            sb.append(String.format(Locale.US, "%.2f", m.dewPoint)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Guarda el contenido CSV en un archivo local dentro del directorio seguro de la app.
     */
    fun saveCsvLocally(context: Context, measurements: List<Measurement>): File {
        val exportsDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "atmos_mediciones_$timeStamp.csv"
        val csvFile = File(exportsDir, fileName)

        FileWriter(csvFile).use { writer ->
            writer.write(generateCsvString(measurements))
        }
        return csvFile
    }

    /**
     * Lanza el selector del sistema Android (ACTION_SEND) para compartir o guardar el archivo CSV generado.
     */
    fun shareCsvFile(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ATMOS - Exportación de Mediciones Meteorológicas")
            putExtra(
                Intent.EXTRA_TEXT,
                "Se adjunta el reporte de las últimas mediciones meteorológicas registradas por la estación ATMOS."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Exportar / Guardar mediciones CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Copia el texto CSV al portapapeles para facilitar su pegado en hojas de cálculo u otras aplicaciones.
     */
    fun copyToClipboard(context: Context, csvContent: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("ATMOS Mediciones CSV", csvContent)
        clipboard?.setPrimaryClip(clip)
    }
}
