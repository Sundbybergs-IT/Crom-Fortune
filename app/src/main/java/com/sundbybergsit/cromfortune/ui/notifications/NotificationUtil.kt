package com.sundbybergsit.cromfortune.ui.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sundbybergsit.cromfortune.MainActivity
import com.sundbybergsit.cromfortune.R

object NotificationUtil {

    private const val TAG = "NotificationUtil"

    private const val NOTIFICATION_CHANNEL_ID_REGULAR: String = "$TAG.NOTIFICATION_CHANNEL_REGULAR"
    private const val NOTIFICATION_ID_REGULAR = 7717

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID_REGULAR,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = ""
        channel.enableLights(true)
        getNotificationManager(context).createNotificationChannel(channel)
    }

    fun doPostRegularNotification(context: Context, title: String, shortText: String, text: String) {
        getNotificationManager(context).notify(TAG, NOTIFICATION_ID_REGULAR,
                createRegularNotification(context, title, shortText, text))
    }

    private fun createRegularNotification(context: Context, title: String, shortText: String, text: String): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_REGULAR)
                .setSmallIcon(R.drawable.ic_cromfortune)
                .setContentTitle(title)
                .setContentText(shortText)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setShowWhen(true)
                .setLocalOnly(true)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
        notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE)
        return notificationBuilder.build()
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}
