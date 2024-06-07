package com.example.ScreenRecorder.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.demo.R

class NotificationHelper {

    private var notification: Notification? = null

    private var builder: NotificationCompat.Builder? = null

    private var notificationManager: NotificationManager? = null


    companion object {
        fun getInstance(): NotificationHelper {
            return NotificationHelper()
        }
    }


    fun createNotification(context: Context, channelID: String, channelName: String, importance: Int): Notification {
         notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        //如果安卓版本为8.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel(channelID,channelName,importance)
            channel.setShowBadge(true)
            notificationManager!!.createNotificationChannel(channel)
        }

        var builder = NotificationCompat.Builder(context,channelID)
            .setContentTitle("服务已经启动")
            .setContentText("服务已经启动")
            .setSmallIcon(R.drawable.nature)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        return builder.build()
    }

    fun notify(context: Context, channelID: String, channelName: String, importance: Int) {
        var notification = createNotification(context,channelID,channelName,importance)

        if (notificationManager != null) {
            notificationManager!!.notify(123,notification)
        }
    }


}