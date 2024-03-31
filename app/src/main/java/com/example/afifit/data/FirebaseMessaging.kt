package data

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessaging : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received message and show a notification
        // You can extract data from the message and create a notification here

        if (remoteMessage.data.isNotEmpty()) {
            // Handle data payload
            handleDataMessage(remoteMessage.data);
        }

        if (remoteMessage.notification != null) {
            // Handle notification payload
            handleNotificationMessage(remoteMessage.notification!!);
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Extract data from the message and handle accordingly
        val title = data["title"]
        val message = data["message"]

        // You can perform additional actions based on the data

        // Show a notification
        if (title != null) {
            if (message != null) {
                showNotification(title, message)
            }
        }
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        // Handle notification payload (if any)
        val title = notification.title
        val body = notification.body

        // You can perform additional actions based on the notification

        // Show a notification
        if (title != null) {
            if (body != null) {
                showNotification(title, body)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelName: CharSequence = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "default_channel_id")
                .setSmallIcon(R.drawable.ic_notification_overlay)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}