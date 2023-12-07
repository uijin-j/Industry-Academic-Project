package com.hongmu.samplereceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hongmu.samplereceiver.db.LogDatabase
import com.hongmu.samplereceiver.db.MyLog


class MyService : Service() {

    companion object {
        const val CHANNEL_ID = "app_channel"
        const val CHANNEL_NAME = "낙상 방지 솔루션 수신 앱"
        var isRunning = false
    }

    private lateinit var logDatabase: LogDatabase

    private val receiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getIntExtra("key_type", -1)
            if(type == 4) {
                ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    .startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300)
            }
            logDatabase.logDao().insert(MyLog(0, type, System.currentTimeMillis()))
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        logDatabase = LogDatabase.getInstance(this)

        val filter = IntentFilter().apply {
            addAction("com.stlinkproject.action.DANGER_ALERT")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(CHANNEL_NAME)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        return super.onStartCommand(contentIntent, flags, startId)
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            NotificationManagerCompat.from(this).apply {
                createNotificationChannel(channel)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterReceiver(receiver)
    }
}