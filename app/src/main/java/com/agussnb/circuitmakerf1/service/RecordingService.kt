package com.agussnb.circuitmakerf1.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.agussnb.circuitmakerf1.MainActivity
import com.agussnb.circuitmakerf1.R

/**
 * Foreground Service: le avisa a Android "esta app está haciendo algo que el usuario ve"
 * (la notificación fija). Así el sistema no la frena cuando apagás la pantalla.
 *
 * El servicio no graba nada: la grabación la hacen [com.agussnb.circuitmakerf1.domain.service.TrackRecorder]
 * y [com.agussnb.circuitmakerf1.domain.service.LapTimer] desde el `AppContainer`. El servicio solo
 * mantiene vivo el proceso. Lo arranca y lo para `AppContainer.startTrackingServiceSync()`.
 */
class RecordingService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Usando el GPS"
        createChannel()

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(message),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            // Por ejemplo, si falta el permiso de ubicación (Android 14+ lo exige).
            Log.w(TAG, "No se pudo iniciar el servicio en primer plano", e)
            stopSelf()
        }

        // NOT_STICKY: si Android mata el proceso, no lo recreamos solo (la grabación ya se perdió).
        return START_NOT_STICKY
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Grabación con GPS",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Aviso mientras se graba un circuito o se cronometran vueltas"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(message: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_tracking)
            .setContentTitle("Circuit Maker")
            .setContentText(message)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

    companion object {
        private const val TAG = "RecordingService"
        private const val CHANNEL_ID = "tracking"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_MESSAGE = "message"

        fun start(context: Context, message: String) {
            val intent = Intent(context, RecordingService::class.java)
                .putExtra(EXTRA_MESSAGE, message)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                // Android 12+ no deja iniciarlo si la app está en segundo plano.
                Log.w(TAG, "No se pudo iniciar el servicio", e)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RecordingService::class.java))
        }
    }
}
