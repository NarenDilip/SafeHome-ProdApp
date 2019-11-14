package com.schnell.safehome.attention

import android.app.ActivityManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.schnell.database.model.AppDatabase
import com.schnell.database.model.DeviceDAO
import com.schnell.database.model.Devices
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent.getIntent
import android.support.v4.content.ContextCompat.startActivity


/**
 * @since 08/08/16.
 * Modified from samples
 */
class FcmService : FirebaseMessagingService(), ResponseListener {

    private var mDeviceDAO: DeviceDAO? = null
    private var Bdata: Boolean = false
    private val TAG = FirebaseMessagingService::class.java!!.getSimpleName()
    private var mContactRecyclerAdapter: DeviceNotificationRecyclerAdapter? = null
    var notify: NotificationActivity? = NotificationActivity()
    private var devicelistLiveData: LiveData<List<Devices>>? = null

    private fun log(message: String) {
        Log.d(this::class.java.simpleName, message)
    }

    private val pushNotificationAction = "pushNotification"
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        try {
            // Check if message contains a notification payload.
            if (remoteMessage!!.notification != null) {
                handleNotification(remoteMessage.notification!!.body.toString())
            }

            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty()) {
                Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())

                try {
                    val json = JSONObject(remoteMessage.data.toString())
                    handleDataMessage(json)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception: " + e.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleNotification(message: String) {

        if (!NotificationUtils.isAppInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent(pushNotificationAction)
            pushNotification.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
            // play notification sound
            val notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()

            System.out.println("Notification Message------------>$message")

            //Fetching Current Date
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            System.out.println(" C DATE is  $currentDate")

            val jresponse = JSONObject(message)
            var Name = jresponse.getString("Name")
            var type = jresponse.getString("Type")
            var alert = jresponse.getString("Alert")

            mDeviceDAO =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db-devices")
                    .allowMainThreadQueries() //Allows room to do operation on main thread
                    .build()
                    .deviceDAO

            val device = Devices()
            device.setName(Name)
            device.setType(type)
            device.setAlert(alert)
            device.setCreatedDate(Date())

            try {
                mDeviceDAO!!.insert(device)
            } catch (e: SQLiteConstraintException) {
                System.out.println(e)
            }
        }

        val resultIntent = Intent(applicationContext, Dashboard::class.java)
        resultIntent.putExtra("message", message)
        NotificationUtils(applicationContext)
            .showNotificationMessage("Safe Home", message)
    }

    private fun handleDataMessage(json: JSONObject) {
        Log.e(TAG, "push json: $json")
        try {

            val message = json.getJSONObject("message")
            val today = Date()
            val format = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val dateToStr = format.format(today)
            println(dateToStr)

            var date1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateToStr)

            var Name = message.getString("Name")
            var type = message.getString("Type")
            var alert = message.getString("Alert")

            mDeviceDAO =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db-devices")
                    .allowMainThreadQueries() //Allows room to do operation on main thread
                    .build()
                    .deviceDAO

            val device = Devices()
            device.name = Name
            device.type = type
            device.alert = alert
            device.createdDate = date1

            try {
                mDeviceDAO!!.insert(device)
            } catch (e: SQLiteConstraintException) {
                System.out.println(e)
            }

            if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
                // app is in foreground, broadcast the push message
                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                pushNotification.putExtra("message", message.toString())
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
                // play notification sound
                val notificationUtils = NotificationUtils(applicationContext)
//                notificationUtils.playNotificationSound()

            } else {
                // app is in background, show the notification in notification tray
                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                pushNotification.putExtra("message", message.toString())
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
                // play notification sound
                val notificationUtils = NotificationUtils(applicationContext)
                notificationUtils.playNotificationSound()
            }

            val mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val RunningTask = mActivityManager.getRunningTasks(1)
            val ar = RunningTask[0]
            if (ar.topActivity.className.toString().equals("com.schnell.safehome.NotificationActivity")) {

                val intent = Intent(applicationContext, NotificationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                applicationContext.startActivity(intent)

            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }
        return
    }

    override fun onResponse(r: Response?) {
        try {
            if (r != null && r.requestType == 0) { // 0 should be request type
                AppPreference.clearAll(c = applicationContext)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

