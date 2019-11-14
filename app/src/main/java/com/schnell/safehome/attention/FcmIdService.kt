package com.schnell.safehome.attention

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.schnell.safehome.AppPreference


class FcmIdService : FirebaseInstanceIdService() {
    private val registrationCompletedAction = "registrationComplete"
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        try {
            val refreshedToken = FirebaseInstanceId.getInstance().token!!
            // Saving reg id to shared preferences
            AppPreference.put(this, "fcm_id", refreshedToken)
            Log.v("SH-FCMID", refreshedToken)
//            UserService.registerForNotification(applicationContext, refreshedToken)

            val registrationComplete = Intent(registrationCompletedAction)
            registrationComplete.putExtra("token", refreshedToken)
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
