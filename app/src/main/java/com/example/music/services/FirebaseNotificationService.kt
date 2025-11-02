package com.example.music.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.music.R
import com.example.music.activities.MainActivity
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FirebaseNotificationService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "6969"
        private const val CHANNEL_NAME = "Melotune-Firebase-Cloud-Messaging"
        private const val TAG = "FCM"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        var title: String? = "Notification"
        var body: String? = ""
        var clickUrl: String? = null
        var imageUrl: String? = null

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            title = data["title"]
            body = data["body"]
            clickUrl = data["click_url"]
            imageUrl = data["image"]
        }

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            if (title == null) title = notification.title
            if (body == null) body = notification.body
            if (clickUrl == null) clickUrl = notification.clickAction
            if (imageUrl == null) imageUrl = notification.imageUrl?.toString()
        }

        showNotification(title, body, clickUrl, imageUrl)
    }

    private fun showNotification(
        title: String?,
        message: String?,
        clickUrl: String?,
        imageUrl: String?
    ) {
        val intent = if (!clickUrl.isNullOrEmpty() && clickUrl.startsWith("http")) {
            Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
        } else {
            Intent(this, MainActivity::class.java)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            @Suppress("DEPRECATION")
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        }

        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Usa tu ícono aquí
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("http")) {
            val bitmap = getBitmapFromURL(imageUrl)
            if (bitmap != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setSummaryText(message)
                )
            } else {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "App Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun getBitmapFromURL(strUrl: String): Bitmap? {
        return try {
            val url = URL(strUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "getBitmapFromURL: ", e)
            null
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Enviar token a tu servidor si es necesario
    }
}
