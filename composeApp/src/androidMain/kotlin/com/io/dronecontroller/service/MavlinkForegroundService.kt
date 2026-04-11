package com.io.dronecontroller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.io.dronecontroller.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MavlinkForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val droneStateHolder: DroneStateHolder by inject()
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        startForeground(NOTIFICATION_ID, buildNotification(DroneNotificationState()))
        scope.launch {
            droneStateHolder.state.collect { state ->
                notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "MAVLink接続",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "ドローンとのMAVLink接続を維持します"
                setShowBadge(false)
            }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(state: DroneNotificationState): Notification {
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
        val connectionText = if (state.isConnected) "接続中" else "切断"
        val batteryText = if (state.isConnected) "バッテリー: ${state.batteryPercent}%" else "--"

        return android.app.Notification
            .Builder(this, CHANNEL_ID)
            .setContentTitle("ドローンコントローラー: $connectionText")
            .setContentText(batteryText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "mavlink_connection"

        fun start(context: Context) {
            val intent = Intent(context, MavlinkForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MavlinkForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
