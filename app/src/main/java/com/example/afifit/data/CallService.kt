package com.example.afifit.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.afifit.R

class CallService : Service() {
    private val handler = Handler()
    private var phoneNumbers: List<String> = listOf()
    private var currentCallIndex = 0

    companion object {
        const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        phoneNumbers = intent?.getStringArrayListExtra("phoneNumbers") ?: listOf()
        currentCallIndex = 0
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        startForeground(NOTIFICATION_ID, createNotification())
        makeCall()
    }

    private fun makeCall() {
        if (currentCallIndex >= phoneNumbers.size) {
            stopSelf()
            return
        }

        val phoneNumber = phoneNumbers[currentCallIndex]
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this line

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        try {
            startActivity(callIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        handler.postDelayed({
            endCall()
        }, 5000) // Call duration is 5 seconds
    }

    private fun endCall() {
        currentCallIndex++
        makeCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("call_service", "Call Service")
            } else {
                "" // If earlier than Oreo, no channel ID is needed
            }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Call Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)

        return channelId
    }
}
