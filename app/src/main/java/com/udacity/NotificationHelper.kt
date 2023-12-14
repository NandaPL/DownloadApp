package com.udacity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationHelper (
    private val context: Context,
    private val fileName: String
) {

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mFileName: String

    fun sendNotification(status: String) {
        mFileName = fileName
        createChannel()

        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(Constants.STATUS_KEY, status)
        intent.putExtra(Constants.FILE_NAME_KEY, mFileName)
        mPendingIntent = PendingIntent.getActivity(
            context,
            Constants.NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            context,
            Constants.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.loading_completed))
            .setAutoCancel(true)
            .addAction(
                R.drawable.abc_vector_test,
                context.getString(R.string.see_result),
                mPendingIntent
            )

        mNotificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        mNotificationManager.notify(Constants.NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.enableVibration(true)

            channel.description = context.getString(R.string.loading_completed)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}