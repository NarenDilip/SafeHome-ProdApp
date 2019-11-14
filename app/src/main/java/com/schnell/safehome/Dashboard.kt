package com.schnell.safehome

import android.Manifest
import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.schnell.database.model.AppDatabase
import com.schnell.database.model.SosNumbers
import com.schnell.database.model.SosNumbersDAO
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.adapter.ViewPagerAdapter
import com.schnell.safehome.model.Device
import com.schnell.safehome.model.LoginResponse
import com.schnell.safehome.model.User
import com.schnell.safehome.webservice.ThingsManager
import com.schnell.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.content_dashboard.*

class Dashboard : AppCompatActivity(), ResponseListener {

    private var mLoginUser: User? = null
    private lateinit var pagerAdapter: ViewPagerAdapter
    private var deviceGroups: ArrayList<Device> = ArrayList()
    val TAG = Dashboard::class.java!!.getSimpleName()
    private var mSosNumbersDAO: SosNumbersDAO? = null
    private var Usernumberlist: List<SosNumbers>? = null
    private var strUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_dashboard)
        WriteStoragePermission(activity = this)
        ReadStoragePermission(activity = this)

        strUser = intent.getStringExtra("NewApp")

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //do stuff here
                Toast.makeText(applicationContext, tab.text, Toast.LENGTH_SHORT).show()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Toast.makeText(applicationContext, tab.text, Toast.LENGTH_SHORT).show()
            }
        })

        try {
            mLoginUser =
                AppPreference.getGson(
                    c = this,
                    key = AppPreference.Key.loginUser,
                    type = User::class.java
                ) as User
            if (mLoginUser != null && mLoginUser!!.firstName != null) {
                appTitle.text = resources.getString(R.string.ssecure)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initViews()
    }

    private fun WriteStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSERP", "Permission request for writing in internal storage")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        102
                    )
                }
            }
        }
    }

    private fun ReadStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSERP", "Permission request for writing in internal storage")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        102
                    )
                }
            }
        }
    }

    private fun initViews() {
        try {
            sos.setOnClickListener {
                checkSendSmsPermission(activity = this)
                try {
                    mSosNumbersDAO =
                        Room.databaseBuilder(getApplicationContext(), AppDatabase::class.java, "db-devices")
                            .allowMainThreadQueries() //Allows room to do operation on main thread
                            .build()
                            .sosNumbersDAO

                    Usernumberlist = mSosNumbersDAO!!.getusernumbers()
                    if (!Usernumberlist.isNullOrEmpty()) {

                        if (!Usernumberlist!!.get(0).firstnumber.isEmpty()) {
                            sendingSms(Usernumberlist!!.get(0).firstnumber)
                        }

                        if (!Usernumberlist!!.get(0).secondnumber.isEmpty()) {
                            sendingSms(Usernumberlist!!.get(0).secondnumber)
                        }

                        if (!Usernumberlist!!.get(0).thirdnumber.isEmpty()) {
                            sendingSms(Usernumberlist!!.get(0).thirdnumber)
                        }

                        if (!Usernumberlist!!.get(0).fourthnumber.isEmpty()) {
                            sendingSms(Usernumberlist!!.get(0).fourthnumber)
                        }

                        if (!Usernumberlist!!.get(0).fifthnumber.isEmpty()) {
                            sendingSms(Usernumberlist!!.get(0).fifthnumber)
                        }

                    } else {
                        Snackbar.make(appTitle, resources.getString(R.string.sosnumber), Snackbar.LENGTH_SHORT)
                            .show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            logout.setOnClickListener {
                AppPreference.clearAll(c = this)
                finish()
            }

            addGateWay.setOnClickListener {
                DeviceActivity.openDeviceActivity(c = this, isGateway = true)
            }

            pagerAdapter = ViewPagerAdapter(supportFragmentManager)
            initGateways()
            AppDialogs.showProgressDialog(context = this, desc = resources.getString(R.string.loaddevice))

            // gettings entity group device list details
            ThingsManager.getEntityGroupsForDevice(c = this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendingSms(number: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            number,
            null,
            R.string.sosalert.toString(),
            null,
            null
        )
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
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 102)
                }
            }
        }
    }

    private fun initGateways() {
        pagerAdapter.clear()
        for (i in 0 until deviceGroups.size) {
            val device = deviceGroups[i]
            if (device.name != "All")
                if (device.name != "ALL CCMS")
                    if (device.name != "866039042091026")
                        pagerAdapter.addFragment(
                            GatewayFragment.newInstance(device.id!!.id!!, pagerAdapter.count, strUser),
                            device.name!!
                        )
        }
        if (pagerAdapter.getAll().isEmpty()) {
            noData.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
        } else {
            noData.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
            viewPager.adapter = pagerAdapter
            tabs.setupWithViewPager(viewPager)
            pagerAdapter.notifyDataSetChanged()
        }
    }

    private fun callloginmethod() {
        ThingsManager.login(
            c = this!!,
            username = "boopathi.schnell@gmail.com",
            password = "aa123"
        )
    }

    private fun loadDashboard() {
        finish()
        val intent = Intent(this, Dashboard::class.java);
        intent.putExtra("NewApp", "true")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (pagerAdapter.count > 0 && requestCode == DeviceActivity.resultCodeDevice) {
                    (pagerAdapter.getItem(viewPager.currentItem) as GatewayFragment).onActivityResult(
                        requestCode,
                        resultCode,
                        data
                    )
                }
            } else {
                ThingsManager.getEntityGroupsForDevice(c = this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true
    }

    override fun onResponse(r: Response?) {
        try {
            AppDialogs.hideProgressDialog()
            if (r == null) {
                return
            }
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
                callloginmethod()
            }

            when (r.requestType) {
                ThingsManager.API.getEntityGroupsForDevice.hashCode() -> {
                    if (r is Device) {
                        deviceGroups.clear()
                        deviceGroups.addAll(r.deviceList!!)
                        initGateways()
                    } else {
                        initGateways()
                        Snackbar.make(tabs, resources.getString(R.string.failload), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
                ThingsManager.API.login.hashCode() -> {
                    if (r is LoginResponse) {
                        AppPreference.put(
                            this,
                            AppPreference.Key.accessToken,
                            r.token.toString()
                        )
                        AppPreference.put(
                            this,
                            AppPreference.Key.refreshToken,
                            r.refreshToken.toString()
                        )
                        ThingsManager.getUser(c = this)
                    } else {
                        Snackbar.make(splashText, r.message.toString(), Snackbar.LENGTH_SHORT).show()
                    }
                }
                ThingsManager.API.user.hashCode() -> {
                    if (r is User) {
                        AppPreference.storeGson(
                            c = this,
                            key = AppPreference.Key.loginUser,
                            data = r
                        )
                        loadDashboard()
                    } else {
                        ThingsManager.login(
                            c = this,
                            username = "boopathi.schnell@gmail.com",
                            password = "aa123"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
