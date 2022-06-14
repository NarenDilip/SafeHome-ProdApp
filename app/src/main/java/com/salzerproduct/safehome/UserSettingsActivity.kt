package com.salzerproduct.safehome

import android.Manifest
import android.app.Activity
import android.arch.persistence.room.Room
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.salzerproduct.database.model.*
import kotlinx.android.synthetic.main.activity_user_settings.*
import kotlinx.android.synthetic.main.activity_user_settings.toolbar

// AddingSecondaryUser , we need to add the secondary user numbers with new numbers and update to
// server, edit the secondary user, delete the secondary user number, even we need to upate in
// server too, server updated details will be updated in the ui, we need to fetch the user details
// from the server and update in the ui fields.

class UserSettingsActivity : AppCompatActivity() {

    private var mSosNumbersDAO: SosNumbersDAO? = null
    private var Usernumberlist: List<SosNumbers>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
        setSupportActionBar(toolbar)
        checkSendSmsPermission(activity = this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mSosNumbersDAO =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .build()
                .sosNumbersDAO

        Usernumberlist = mSosNumbersDAO!!.getusernumbers()
        if (!Usernumberlist.isNullOrEmpty()) {
            firstno.setText(Usernumberlist!![0].firstnumber)
            secondno.setText(Usernumberlist!![0].secondnumber)
            thirdno.setText(Usernumberlist!![0].thirdnumber)
            fourthno.setText(Usernumberlist!![0].fourthnumber)
            fifthno.setText(Usernumberlist!![0].fifthnumber)
        }

        submitbtn.setOnClickListener {
            AddUsernumbertodb()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * The build is with or after Marshmallow then this method call is must before using the permission the we need
     */
    private fun checkSendSmsPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.SEND_SMS
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("Home Secure", "Permission request for Sending Sms")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 102)
                }
            }
        }
    }


    private fun AddUsernumbertodb() {

        val sosnumbers = SosNumbers()
        sosnumbers.firstnumber = firstno.text.toString()
        sosnumbers.secondnumber = secondno.text.toString()
        sosnumbers.thirdnumber = thirdno.text.toString()
        sosnumbers.fourthnumber = fourthno.text.toString()
        sosnumbers.fifthnumber = fifthno.text.toString()

        try {
            if (Usernumberlist.isNullOrEmpty()) {
                mSosNumbersDAO!!.insert(sosnumbers)
            } else {
                mSosNumbersDAO!!.update(sosnumbers)
            }
        } catch (e: SQLiteConstraintException) {
            System.out.println(e)
        }
        finish()
    }
}