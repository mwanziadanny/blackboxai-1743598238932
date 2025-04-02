package com.example.danzygram.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.danzygram.R
import com.example.danzygram.activities.MainActivity
import com.example.danzygram.util.Constants.NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.danzygram.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.danzygram.util.Constants.NOTIFICATION_CHANNEL_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object NotificationUtil {
    private const val REQUEST_CODE = 0
    private const val FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                importance
            ).apply {
                description = NOTIFICATION_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getBasicBuilder(
        context: Context,
        title: String,
        message: String,
        intent: Intent? = null
    ): NotificationCompat.Builder {
        val defaultIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent ?: defaultIntent,
            FLAGS
        )

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
    }

    fun showBasicNotification(
        context: Context,
        title: String,
        message: String,
        intent: Intent? = null
    ) {
        if (!PreferenceUtil.areNotificationsEnabled()) return

        val builder = getBasicBuilder(context, title, message, intent)
        val notificationId = PreferenceUtil.getLastNotificationId()
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
        
        PreferenceUtil.incrementNotificationId()
    }

    fun showBigTextNotification(
        context: Context,
        title: String,
        message: String,
        bigText: String,
        intent: Intent? = null
    ) {
        if (!PreferenceUtil.areNotificationsEnabled()) return

        val builder = getBasicBuilder(context, title, message, intent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        val notificationId = PreferenceUtil.getLastNotificationId()
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
        
        PreferenceUtil.incrementNotificationId()
    }

    suspend fun showBigPictureNotification(
        context: Context,
        title: String,
        message: String,
        imageUrl: String,
        intent: Intent? = null
    ) {
        if (!PreferenceUtil.areNotificationsEnabled()) return

        val bitmap = withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: Exception) {
                null
            }
        }

        val builder = getBasicBuilder(context, title, message, intent)
        
        bitmap?.let {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .bigLargeIcon(null)
            )
        }

        val notificationId = PreferenceUtil.getLastNotificationId()
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
        
        PreferenceUtil.incrementNotificationId()
    }

    fun showProgressNotification(
        context: Context,
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean = false
    ): Int {
        val builder = getBasicBuilder(context, title, message)
            .setProgress(maxProgress, progress, indeterminate)
            .setOngoing(true)
            .setAutoCancel(false)

        val notificationId = PreferenceUtil.getLastNotificationId()
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
        
        PreferenceUtil.incrementNotificationId()
        return notificationId
    }

    fun updateProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int
    ) {
        val builder = getBasicBuilder(context, title, message)
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .setAutoCancel(false)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun completeProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val builder = getBasicBuilder(context, title, message)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }

    fun cancelAllNotifications(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
}