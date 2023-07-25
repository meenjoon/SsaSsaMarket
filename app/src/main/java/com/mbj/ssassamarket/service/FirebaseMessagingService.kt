package com.mbj.ssassamarket.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mbj.ssassamarket.BuildConfig
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.NotificationType

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val chatChannelName = getString(R.string.chat_channel_name)
            val chatChannelDescription = getString(R.string.chat_channel_description)
            val chatChannelImportance = NotificationManager.IMPORTANCE_DEFAULT
            val chatChannel = NotificationChannel(BuildConfig.CHAT_NOTIFICATION_CHANNEL_ID, chatChannelName, chatChannelImportance)
            chatChannel.description = chatChannelDescription
            notificationManager.createNotificationChannel(chatChannel)

            val sellChannelName = getString(R.string.sell_channel_name)
            val sellChannelDescription = getString(R.string.sell_channel_description)
            val sellChannelImportance = NotificationManager.IMPORTANCE_DEFAULT
            val sellChannel = NotificationChannel(BuildConfig.SELL_NOTIFICATION_CHANNEL_ID, sellChannelName, sellChannelImportance)
            sellChannel.description = sellChannelDescription
            notificationManager.createNotificationChannel(sellChannel)

            if (message.data["type"] == NotificationType.CHAT.label) {
                val body = message.notification?.body
                val chatNotificationBuilder = NotificationCompat.Builder(applicationContext, BuildConfig.CHAT_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.chat_icon)
                    .setContentTitle(getString(R.string.chat_channel_name))
                    .setContentText(body)

                notificationManager.notify(0, chatNotificationBuilder.build())
            } else if (message.data["type"] == NotificationType.SELL.label) {
                val body = message.notification?.body
                val buyNotificationBuilder = NotificationCompat.Builder(applicationContext, BuildConfig.SELL_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.sell_icon)
                    .setContentTitle(getString(R.string.sell_channel_name))
                    .setContentText(body)

                notificationManager.notify(0, buyNotificationBuilder.build())
            }
        } else {
            val notificationBuilder = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.chat_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message.data["body"])

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
