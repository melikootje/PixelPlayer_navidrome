package com.theveloper.pixelplay.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Manages sync progress notifications for music library synchronization
 */
class SyncNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "music_sync"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Library Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of music library synchronization"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSyncStarted(source: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Syncing $source Library")
            .setContentText("Starting sync...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun updateProgress(
        source: String,
        current: Int,
        total: Int,
        itemType: String
    ) {
        val progress = if (total > 0) (current * 100) / total else 0

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Syncing $source Library")
            .setContentText("$current/$total $itemType ($progress%)")
            .setProgress(total, current, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showSuccess(source: String, songCount: Int, albumCount: Int, artistCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("$source Sync Complete")
            .setContentText("✓ $songCount songs, $albumCount albums, $artistCount artists")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showError(source: String, error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("$source Sync Failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

