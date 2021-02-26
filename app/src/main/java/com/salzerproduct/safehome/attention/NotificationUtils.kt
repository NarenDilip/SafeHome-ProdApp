package com.salzerproduct.safehome.attention

import android.annotation.SuppressLint
import android.app.ActivityManager
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
import android.support.v4.app.NotificationCompat
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.salzerproduct.safehome.NotificationActivity
import com.salzerproduct.safehome.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NotificationUtils(private val mContext: Context) {
    private var notificationId = 10001
    private val notificationChannelId = 10010
    private fun log(message: String) {
        Log.d(this::class.java.simpleName, message)
    }

    private fun getNotificationId(groupKey: String, summaryId: Int = notificationChannelId): Int {
        val sharedPreferences = mContext.applicationContext.getSharedPreferences("SUMMARY", Context.MODE_PRIVATE)!!
        try {
            var id = sharedPreferences.getInt(groupKey, summaryId)
            val editor = sharedPreferences.edit()
            if (id == summaryId) {
                id = sharedPreferences.getInt("LATEST_NOTIFICATION_ID", summaryId)
                id++
                editor.putInt("LATEST_NOTIFICATION_ID", id)
            }
            editor.putInt(groupKey, id)
            editor.apply()
            return id
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getPendingIntent(context: Context, message: String, notification_id: Int): PendingIntent {
        try {
            val intent = Intent(context, NotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("message", message)
            return PendingIntent.getActivity(
                context, notification_id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        } catch (e: Exception) {
            throw e
        }
    }

    @JvmOverloads
    fun showNotificationMessage(title: String, message: String, imageUrl: String? = null, collapseKey: String? = null) {
        try {
            if (collapseKey != null) {
                notificationId = getNotificationId(groupKey = collapseKey)
            }
            // Check for empty push message
            if (TextUtils.isEmpty(message))
                return
            val pendingIntent = getPendingIntent(mContext, message, notificationChannelId)
            if (!TextUtils.isEmpty(imageUrl) && imageUrl!!.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                buildNotification(title, message, pendingIntent, getBitmapFromURL(imageUrl))
            } else {
                buildNotification(title, message, pendingIntent)
            }
//            ShortcutBadger.applyCount(mContext, AppPreference[mContext, AppPreference.Key.NOTIFICATION_COUNT, 0])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    private fun buildNotification(
        title: String,
        message: String,
        pendingIntent: PendingIntent,
        bitmap: Bitmap? = null
    ) {
        val style = if (bitmap != null) {
            NotificationCompat.BigPictureStyle()
                .setBigContentTitle(title)
                .setSummaryText(Html.fromHtml(message).toString())
                .bigPicture(bitmap)
        } else {
            NotificationCompat.BigTextStyle().bigText(message)
        }

        val notification = NotificationCompat.Builder(mContext, notificationChannelId.toString())
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setStyle(style)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher))
            .setVibrate(LongArray(0))
            .setAutoCancel(true)
            .setGroupSummary(true)
            .setGroup("XSerp says")
            .build()
        val mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId.toString(), "SafeHome says",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mNotificationManager.createNotificationChannel(channel)
        }

        mNotificationManager.notify(notificationId, notification)
        log("Updated XSerp notification.")
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Playing notification sound
    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse("android.resource://" + mContext.packageName + "/" + R.raw.siren);
            RingtoneManager.getRingtone(mContext, alarmSound).play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    companion object {
        /**
         * Method checks if the app is in background or not
         */
        fun isAppInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val runningProcesses = am.runningAppProcesses
            runningProcesses
                .filter { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
                .forEach {
                    it.pkgList
                        .filter { packageName -> packageName == context.packageName }
                        .forEach { isInBackground = false }
                }
            return isInBackground
        }

        fun clearNotifications(context: Context?) {
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.getRunningAppProcesses()
                for (processInfo in runningProcesses) {
                    if (processInfo.importance === ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.getPackageName()) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo.get(0).topActivity
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false
                }
            }
            return isInBackground
        }
    }
}