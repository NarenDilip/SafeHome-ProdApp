package com.salzerproduct.safehome

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
import com.salzerproduct.database.model.*
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.Utils.BaseActivity
import com.salzerproduct.safehome.adapter.ViewPagerAdapter
import com.salzerproduct.safehome.model.Device
import com.salzerproduct.safehome.model.LoginResponse
import com.salzerproduct.safehome.model.User
import com.salzerproduct.safehome.webservice.BaseRS
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.content_dashboard.*
import org.json.JSONObject

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class Dashboard : AppCompatActivity(), ResponseListener {

    private var mLoginUser: User? = null
    private lateinit var pagerAdapter: ViewPagerAdapter
    private var deviceGroups: ArrayList<Device> = ArrayList()
    private var strUser: String = ""
    private var DeviceGId: String? = ""

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
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        102
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        val l = object : AppDialogs.ConfirmListener {
            override fun yes() {
                AppPreference.clear(applicationContext, "Gw")
                finish()
                System.exit(0)
            }
        }
        AppDialogs.confirmAction(c = this, text = "Sure you want to Exit Home Security", l = l)
    }

    private fun initViews() = try {

        val Devicedetails = AppPreference.get(this, "DeviceSos", "")

        sos.setOnClickListener {
            if (Devicedetails!!.isNotEmpty()) {
                try {
                    val params = JSONObject()
                    params.put("trigger", "1")
                    val rpcJson = JSONObject()
                    rpcJson.put("params", params)
                    rpcJson.put("method", "sos")
                    rpcJson.put("timeout", 30000)

                    infoText.visibility = View.VISIBLE

                    val connectionDetails = BaseActivity.internetIsAvailable(this)
                    if (connectionDetails) {
                        ThingsManager.callRPCTwoWay(
                            c = this,
                            l = this,
                            deviceId = Devicedetails,
                            jsonObject = rpcJson
                        )
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please Check your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Add Gateway Details First",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

//            logout.setOnClickListener {
//                ThingsManager.logout(this, "")
//                AppPreference.clearAll(c = this)
//                finish()
//            }

        addGateWay.setOnClickListener {
            DeviceActivity.openDeviceActivity(c = this, isGateway = true)
        }

        clearbtn.setOnClickListener {

            val devicelist =
                DatabaseClient.getInstance(applicationContext).appDatabase.addDeviceDAO.devices

            if (devicelist!!.isNotEmpty()) {
                val databaseClient = DatabaseClient.getInstance(applicationContext).appDatabase
                databaseClient.allDevices!!.Deletedevices()
                databaseClient.deviceindex!!.Deletedeviceconfigdao()
                databaseClient.profileState!!.DeleteprofileState()
                databaseClient.getprofileDao()!!.Deletepr()
                databaseClient.deviceNameDAO!!.DeleteDevices()
                databaseClient.telemetryDAO!!.DeleteTelemetry()
                databaseClient.geAttributesDAO()!!.DeleteSensor()
                databaseClient.sosNumbersDAO!!.Deletesosnumbers()
                databaseClient.deviceDAO!!.Deletedevdevices()

                DatabaseClient.getInstance(applicationContext).appDatabase!!.deviceNameDAO.DeleteDevices()
                DatabaseClient.getInstance(applicationContext).appDatabase!!.addDeviceDAO.DeleteSensordevices()
                AppPreference.clearAll(c = this)
                finish()
                val intent = Intent(applicationContext, SplashScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("NewApp", "true")
                startActivity(intent)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Add Gateway Details First",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        initGateways()
//            AppDialogs.showProgressDialog(
//                context = this,
//                desc = resources.getString(R.string.loaddevice)
//            )

//        infoText.visibility = View.VISIBLE

        // gettings entity group device list details
//        ThingsManager.getEntityGroupsForDevice(c = this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun initGateways() {
        pagerAdapter.clear()
//        for (i in 0 until deviceGroups.size) {
//            val device = deviceGroups[i]

//            for (i in 0 until devicelist.size) {
//                if (device.name == devicelist[i].devicename) {
//                    pagerAdapter.addFragment(
//                        GatewayFragment.newInstance(device.id!!.id!!, pagerAdapter.count, strUser),
//                        device.name!!
//                    )
//                    DeviceGId = device!!.id!!.id!!
//                }
//            }
//            AppPreference.put(applicationContext, "Gw", "GW0016")

        var devUid = AppPreference.get(applicationContext, "DeviceUid", "")
        var devName = AppPreference.get(applicationContext, "DeviceName", "")

//            val ds = AppPreference.get(applicationContext, "Gw", "")
//            if (!ds.isNullOrEmpty()) {
//                if (device.name != "All") {
//                    if (device.name == ds) {
        if (devUid!!.isNotEmpty()) {
            pagerAdapter.addFragment(
                GatewayFragment.newInstance(
                    devUid!!,
                    pagerAdapter.count,
                    strUser,
                    devName!!
                ),
                devName!!
            )
            DeviceGId = devUid
//                    }
//                }
        } else {
            Toast.makeText(applicationContext, "No Gateway Details Found", Toast.LENGTH_SHORT)
                .show()
        }
//        }

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
            c = this,
            username = "hgss@schnellenergy.com",
            password = "ce1hg"+"$"+"s"
//            username = "hgss@gmail.com",
//            password = "schnell@321"
        )
    }

    private fun loadDashboard() {
        finish()
        val intent = Intent(this, Dashboard::class.java)
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
//            AppDialogs.hideProgressDialog()
            infoText.visibility = View.INVISIBLE
            if (r == null) {
                return
            }

            if (r.message == "Token has expired" || r.message == "Authentication failed"  || r.errorCode == 11 && r.status == 401 && r.status == 503) {
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
                        Snackbar.make(
                            tabs,
                            resources.getString(R.string.failload),
                            Snackbar.LENGTH_LONG
                        )
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
                        Snackbar.make(splashText, r.message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
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

                ThingsManager.API.rpcTwoWaygetStatus.hashCode() -> {
                    if (r.result == "ok") {
                        Toast.makeText(this, "Successfully Triggered", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Not Triggered", Toast.LENGTH_SHORT).show()
                    }
                }

                ThingsManager.API.logout.hashCode() -> {
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
