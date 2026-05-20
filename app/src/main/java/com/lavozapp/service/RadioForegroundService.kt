package com.lavozapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lavozapp.MainActivity

class RadioForegroundService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(STREAM_URL))
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY -> player?.play()
            ACTION_PAUSE -> player?.pause()
            ACTION_STOP -> { stopSelf(); player?.stop() }
        }

        when (action) {
            ACTION_PLAY, ACTION_PAUSE -> {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player?.release()
            release()
            mediaSession = null
            player = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Radio La Voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Reproducción de radio en segundo plano" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val playPauseIntent = Intent(this, RadioForegroundService::class.java).apply {
            action = if (player?.isPlaying == true) ACTION_PAUSE else ACTION_PLAY
        }
        val stopIntent = Intent(this, RadioForegroundService::class.java).apply { action = ACTION_STOP }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("La Voz de Pucón")
            .setContentText("Radio en vivo")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(
                if (player?.isPlaying == true) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (player?.isPlaying == true) "Pausar" else "Reproducir",
                PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)
            )
            .addAction(
                android.R.drawable.ic_media_play, "Detener",
                PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            )
            .build()
    }

    companion object {
        const val STREAM_URL = "https://live.mtna.tv/hls/lvp/lvp.m3u8"
        const val CHANNEL_ID = "radio_lavoz"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "com.lavozapp.PLAY"
        const val ACTION_PAUSE = "com.lavozapp.PAUSE"
        const val ACTION_STOP = "com.lavozapp.STOP"
    }
}
