package com.salzerproduct.safehome

import android.arch.persistence.room.Room
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import com.salzerproduct.database.model.*
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.model.Device
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_get_device.*
import kotlinx.android.synthetic.main.activity_get_device.deviceAccessTokenView
import kotlinx.android.synthetic.main.activity_get_device.deviceNameView
import kotlinx.android.synthetic.main.activity_get_device.deviceTypeView
import java.lang.Exception

//  GetDeviceInfo, Showing device related informations to the user like gateway number, gateway uid
//  and gateway type , we need to fetch the device details from the server and update with the ui


class GetDeviceInfoActivity : AppCompatActivity(), ResponseListener {

    private var device: Device? = null
    private var isGateway = false
    private var deviceId = ""
    private var entityGroupId = ""
    private var gatewayDeviceId = ""
    private var deviceIndex = 0
    private var devicename = ""
    private var devicetype = ""
    private var deviceuid = ""
    private var randomString: String? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null
    private var mLatestAttributesDAO: LatestAttributesDAO? = null

    companion object {
        var resultCodeGateway = 100
        var resultCodeDevice = 101
        fun GetDeviceActivity(
            c: FragmentActivity,
            isGateway: Boolean = false,
            entityGroupId: String = "",
            deviceId: String = "",
            gatewayDeviceId: String? = "",
            deviceIndex: Int = 0,
            devicename: String? = "",
            devicetype: String? = "",
            deviceuid: String? = ""

        ) {

            val intent = Intent(c, GetDeviceInfoActivity::class.java)
            intent.putExtra("isGateway", isGateway)
            intent.putExtra("entityGroupId", entityGroupId.trim())
            intent.putExtra("gatewayDeviceId", gatewayDeviceId)
            intent.putExtra("deviceId", deviceId.trim())
            intent.putExtra("deviceIndex", deviceIndex)
            intent.putExtra("devicename", devicename)
            intent.putExtra("devicetype", devicetype)
            intent.putExtra("deviceuid", deviceuid)
            c.startActivityForResult(intent, if (isGateway) resultCodeGateway else resultCodeDevice)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_device)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mAddDeviceDAO =
            Room.databaseBuilder(applicationContext!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .build()
                .addDeviceDAO

        mLatestAttributesDAO =
            Room.databaseBuilder(applicationContext!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .build()
                .geAttributesDAO()

        isGateway = intent.getBooleanExtra("isGateway", false)
        entityGroupId = intent.getStringExtra("entityGroupId")
        gatewayDeviceId = intent.getStringExtra("gatewayDeviceId")
        deviceId = intent.getStringExtra("deviceId")
        deviceIndex = intent.getIntExtra("deviceIndex", 0)
        devicename = intent.getStringExtra("devicename")
        devicetype = intent.getStringExtra("devicetype")
        deviceuid = intent.getStringExtra("deviceuid")

        deviceNameView.setText(devicename!!)
        deviceAccessTokenView.setText(deviceuid!!)
        deviceTypeView.setText(devicetype!!)

        deviceTypeView.isEnabled = false
        deviceAccessTokenView.isEnabled = false


        addDevice.setOnClickListener {
            if (deviceNameView.text.toString().isNotEmpty() && deviceTypeView.text.toString().isNotEmpty() && deviceAccessTokenView.text.toString().isNotEmpty()) {
                if (isGateway) {

                    ThingsManager.addAttributedevicename(
                        c = this, l = this,
                        deviceId = deviceId,
                        devicename = deviceNameView.text.toString() + "/*/" + deviceuid
                    )
                    finish()

                } else {

                    ThingsManager.addAttributedevicename(
                        c = this, l = this,
                        deviceId = deviceId,
                        devicename = deviceNameView.text.toString() + "/*/" + deviceuid
                    )
                    finish()
                }

                mLatestAttributesDAO!!.DeletedeviceSensor(deviceId)

                val latestAttribute = LatestAttribute()
                latestAttribute.deviceid = deviceId
                latestAttribute.devicename = deviceNameView.text.toString()

                try {
                    mLatestAttributesDAO!!.insert(latestAttribute)
                } catch (e: SQLiteConstraintException) {
                    System.out.println(e)
                }

//                if (device == null
//                    || device!!.additionalInfo!!.displayName != deviceNameView.text.toString()
//                    || (isGateway && device!!.additionalInfo!!.gatewaySimNumber != gatewaySimNumber.text.toString())
//                ) {
//                    var newDevice = device
//                    /* By default update device */
//                    if (device == null) {
//                        /* Save new device*/
//                        newDevice = Device()
//                        newDevice.name = deviceAccessTokenView.text.toString().trim()
//                        newDevice.type = deviceTypeView.text.toString().trim()
//                    }
//
//                    if (newDevice!!.additionalInfo!!.displayName != deviceNameView.text.toString()) {
//
//                    }
//                    newDevice.additionalInfo!!.displayName = deviceNameView.text.toString()
//                    newDevice.additionalInfo!!.deviceIndex = deviceIndex
//                    if (isGateway) {
//                        newDevice.additionalInfo!!.gateway = true
//                        val simNumber = gatewaySimNumber.text.toString().trim()
//                        if (simNumber.length == 10)
//                            newDevice.additionalInfo!!.gatewaySimNumber = simNumber
//                    }
//                    if (device == null) {
//                        AppDialogs.showProgressDialog(context = this, desc = "Adding Device...")
//                    } else {
//                        AppDialogs.showProgressDialog(context = this, desc = "Updating Device...")
//                    }
//                    ThingsManager.saveDevice(c = this, l = this, entityGroupId = entityGroupId, device = newDevice)
//                }

            }
        }
    }


    override fun onResponse(r: Response?) {
        try {
            AppDialogs.hideProgressDialog()
            if (r == null) {
                return
            }
            when (r.requestType) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

