package com.schnell.safehome

import android.Manifest
import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteConstraintException
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.schnell.database.model.AddDevice
import com.schnell.database.model.AddDeviceDAO
import com.schnell.database.model.AppDatabase
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.model.Device
import com.schnell.safehome.model.DeviceCredential
import com.schnell.safehome.model.Entity
import com.schnell.safehome.webservice.ThingsManager
import com.schnell.util.Utility
import com.schnell.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.content_device.*
import org.json.JSONObject
import org.json.JSONTokener

class DeviceActivity : AppCompatActivity(), ResponseListener, QRCodeReaderView.OnQRCodeReadListener {
    private var device: Device? = null
    private var isGateway = false
    private var deviceId = ""
    private var AddStatus: Boolean = false
    private var entityGroupId = ""
    private var gatewayDeviceId = ""
    private var deviceIndex = 0
    private var randomString: String? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null
    private var DeviceSetup: Boolean? = false
    private var Addset: Int? = 0

    companion object {
        var resultCodeGateway = 100
        var resultCodeDevice = 101
        fun openDeviceActivity(
            c: FragmentActivity,
            isGateway: Boolean = false,
            entityGroupId: String = "",
            deviceId: String = "",
            gatewayDeviceId: String? = "",
            deviceIndex: Int = 0,
            devicename: String? = ""
        ) {
            val intent = Intent(c, DeviceActivity::class.java)
            intent.putExtra("isGateway", isGateway)
            intent.putExtra("entityGroupId", entityGroupId.trim())
            intent.putExtra("gatewayDeviceId", gatewayDeviceId)
            intent.putExtra("deviceId", deviceId.trim())
            intent.putExtra("deviceIndex", deviceIndex)
            c.startActivityForResult(intent, if (isGateway) resultCodeGateway else resultCodeDevice)
        }
    }

    /**
     * The build is with or after Marshmallow then this method call is must before using the permission the we need
     */
    private fun checkCameraPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.CAMERA
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSERP", "Permission request for writing in internal storage")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 102)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        setSupportActionBar(toolbar)
        checkCameraPermission(activity = this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        isGateway = intent.getBooleanExtra("isGateway", false)
        entityGroupId = intent.getStringExtra("entityGroupId")
        gatewayDeviceId = intent.getStringExtra("gatewayDeviceId")
        deviceId = intent.getStringExtra("deviceId")
        deviceIndex = intent.getIntExtra("deviceIndex", 0)
        gatewayLayout.visibility = if (isGateway) View.VISIBLE else View.GONE

        mAddDeviceDAO =
            Room.databaseBuilder(applicationContext!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .build()
                .addDeviceDAO

        if (deviceId.isNotEmpty()) {
            supportActionBar!!.title = if (isGateway) "Gateway Device Info" else "Device Info"
            deviceTypeView.isEnabled = false
            deviceAccessTokenView.isEnabled = false
            qrScan.hide()
            ThingsManager.getDevice(c = this, l = this, deviceId = deviceId)
        } else {
            supportActionBar!!.title = if (isGateway) "Add Gateway Device" else "Add Device"
            deviceTypeView.isEnabled = true
            deviceAccessTokenView.isEnabled = true
            qrScan.show()
            qrScan.setOnClickListener {
                qrView.visibility = View.VISIBLE
                qrCodeReaderView.visibility = View.VISIBLE
                qrCodeReaderView.setOnQRCodeReadListener(this)

                // Use this function to enable/disable decoding
                qrCodeReaderView.setQRDecodingEnabled(true)

                // Use this function to change the autofocus interval (default is 5 secs)
                qrCodeReaderView.setAutofocusInterval(2000L)

                // Use this function to enable/disable Torch
                qrCodeReaderView.setTorchEnabled(true)

                // Use this function to set front camera preview
                qrCodeReaderView.setFrontCamera()

                // Use this function to set back camera preview
                qrCodeReaderView.setBackCamera()
            }
        }

        addDevice.setOnClickListener {
            if (deviceNameView.text.toString().isNotEmpty() && deviceTypeView.text.toString().isNotEmpty() && deviceAccessTokenView.text.toString().isNotEmpty()) {
                if (isGateway) {
                    if (entityGroupId.isEmpty()) {
                        AppDialogs.showProgressDialog(context = this, desc = "Adding Gateway configuration...")
                        DeviceSetup = true

                        ThingsManager.saveEntityGroup(
                            c = this,
                            groupName = deviceAccessTokenView.text.toString(),
                            description = "Security Gateway",
                            displayName = deviceNameView.text.toString()
                        )
                    } else {
                        DeviceSetup = false
                        addDeviceToServer()
                    }
                } else {
//                    if (entityGroupId.isEmpty()) {
                    AppDialogs.showProgressDialog(
                        context = this!!,
                        desc = "Please wait adding device from gateway"
                    )
                    addDeviceToGateway()
//                    } else {
//                    addDeviceToServer()
//                    }
                }
            } else {
                Log.e("Add Device", "Validation error")
            }
        }
    }

    override fun onQRCodeRead(text: String, points: Array<PointF>) {
        try {
            if (text.startsWith("{")) {
                val json = JSONTokener(text).nextValue() as JSONObject
                deviceAccessTokenView.setText(json.get("uid").toString())
                deviceTypeView.setText(json.get("type").toString())
                deviceNameView.setText(json.get("name").toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                deviceAccessTokenView,
                "Could not detect device information from this QR code",
                Snackbar.LENGTH_LONG
            ).show()
        }
        hideQRScanning()
    }

    override fun onResume() {
        super.onResume()
        qrCodeReaderView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        qrCodeReaderView.stopCamera()
    }

    override fun onBackPressed() {
        if (qrCodeReaderView.visibility == View.VISIBLE) {
            hideQRScanning()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun hideQRScanning() {
        qrCodeReaderView.stopCamera()
        qrCodeReaderView.visibility = View.GONE
        qrView.visibility = View.GONE
    }

    fun addDeviceToGateway() {
        val rpcJson = JSONObject()
        val params = JSONObject()
        params.put("devIndex", "$deviceIndex".padStart(2, '0'))
        params.put("devId", deviceAccessTokenView.text.toString().trim())
        params.put("devType", deviceTypeView.text.toString().trim())
        rpcJson.put("params", params)
        rpcJson.put("method", "addDevice")
        rpcJson.put("timeout", 35000)
//        AppDialogs.showProgressDialog(context = this, desc = "Adding device to gateway...")
        ThingsManager.callRPCTwoWay(
            c = this, l = this, deviceId = gatewayDeviceId, jsonObject = rpcJson
        )
    }

    private fun addDeviceToServer() {
        if (device == null
            || device!!.additionalInfo!!.displayName != deviceNameView.text.toString()
            || (isGateway && device!!.additionalInfo!!.gatewaySimNumber != gatewaySimNumber.text.toString())
        ) {
            var newDevice = device
            /* By default update device */
            if (device == null) {
                /* Save new device*/
                newDevice = Device()
                newDevice.name = deviceAccessTokenView.text.toString().trim()
                newDevice.type = deviceTypeView.text.toString().trim()
            }

            if (newDevice!!.additionalInfo!!.displayName != deviceNameView.text.toString()) {
            }

            newDevice.additionalInfo!!.displayName = deviceNameView.text.toString()
            newDevice.additionalInfo!!.deviceIndex = deviceIndex
            if (isGateway) {
                newDevice.additionalInfo!!.gateway = true
                val simNumber = gatewaySimNumber.text.toString().trim()
                if (simNumber.length == 10)
                    newDevice.additionalInfo!!.gatewaySimNumber = simNumber
            }
            if (device == null) {
                AppDialogs.showProgressDialog(context = this, desc = "Adding Device...")
            } else {
                AppDialogs.showProgressDialog(context = this, desc = "Updating Device...")
            }
            ThingsManager.saveDevice(c = this, l = this, entityGroupId = entityGroupId, device = newDevice)
        } else {
            /* Avoid unnecessary update call */
            onBackPressed()
        }
    }

    private fun initDeviceView() {
        if (device != null) {
            deviceTypeView.setText(device!!.type)
            deviceAccessTokenView.setText(device!!.name)
            if (device!!.additionalInfo != null) {
                deviceNameView.setText(device!!.getDisplayName())
                if (device!!.additionalInfo!!.gatewaySimNumber != null) {
                    gatewaySimNumber.setText(device!!.additionalInfo!!.gatewaySimNumber!!)
                }
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
                ThingsManager.API.saveEntityGroup.hashCode() -> {
                    if (r is Device) {
                        entityGroupId = r.id!!.id!!
                        Snackbar.make(
                            deviceNameView,
                            "Device Gateway group has been added",
                            Snackbar.LENGTH_LONG
                        ).show()

                        if (isGateway) {
                            Handler().postDelayed({
                                if (Utility.isInternetAvailable(this)) {
                                    addDeviceToServer()
                                } else {
                                    Snackbar.make(
                                        splashText,
                                        "Please Wait device is configuring....",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .show()
                                }
                            }, 1000)
                        } else {
                            addDeviceToServer()
                        }

                    } else {
                        Snackbar.make(
                            deviceNameView,
                            "Save gateway group failed. Please try again later",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                ThingsManager.API.device.hashCode() -> {
                    if (r is Device) {
                        val isNewDeviceSaved = device == null
                        device = r
                        if (r.requestMethod == Request.Method.GET) {
                            initDeviceView()
                        } else {
                            if (isGateway) {
                                gatewayDeviceId = device!!.id!!.id!!
                                ThingsManager.addAttribute(c = this, l = this, deviceId = device!!.id!!.id!!)
                            } else
                                ThingsManager.saveRelation(
                                    c = this,
                                    gatewayDeviceId = gatewayDeviceId,
                                    device = device!!
                                )
                            Snackbar.make(
                                deviceNameView,
                                "Device has been added",
                                Snackbar.LENGTH_LONG
                            ).show()

                            //ADD DETAILS TO LOCAL DATABASE
                            val addDevice = AddDevice()
                            addDevice.devicename = r.additionalInfo!!.displayName!!
                            addDevice.deviceuid = r.name
                            addDevice.devicetype = r.type
                            addDevice.deviceid = r.id!!.id!!
                            addDevice.deviceindex = r.additionalInfo!!.deviceIndex!!.toString()
                            addDevice.entitygroupid = entityGroupId
                            addDevice.gatewayDeviceId = gatewayDeviceId

                            try {
                                mAddDeviceDAO!!.insert(addDevice)
                            } catch (e: SQLiteConstraintException) {
                                System.out.println(e)
                            }

                            finish();

                            if (isNewDeviceSaved) {
                                ThingsManager.getDeviceCredentialsByDeviceId(c = this, deviceId = device!!.id!!.id!!)
                            } else {
                                /* Device update is done */
                                if (isGateway) {
                                    setResult(Activity.RESULT_CANCELED)
                                } else {
                                    setResult(RESULT_OK)
                                }
                                finish()
                            }

                            if (isNewDeviceSaved) {
                                ThingsManager.addAttributedeviceindex(
                                    c = this, l = this,
                                    deviceId = device!!.id!!.id!!,
                                    deviceindex = deviceIndex.toString() + "/" + r.name
                                )

                                ThingsManager.addAttributedevicename(
                                    c = this, l = this,
                                    deviceId = device!!.id!!.id!!,
                                    devicename = deviceNameView.text.toString()
                                )
                            }
                        }
                    } else {
                        if (r.requestMethod == Request.Method.GET) {
                            Snackbar.make(deviceNameView, "Could not load device information", Snackbar.LENGTH_LONG)
                                .show()
                        } else {
                            Snackbar.make(deviceNameView, r.message.toString(), Snackbar.LENGTH_LONG)
                                .show()
                        }
                    }
                }
                ThingsManager.API.getDeviceCredentialsByDeviceId.hashCode() -> {
                    if (r is DeviceCredential) {
                        val deviceCredential = DeviceCredential()
                        deviceCredential.id = Entity()
                        deviceCredential.id!!.id = r.id!!.id
                        deviceCredential.deviceId = device!!.id
                        deviceCredential.credentialsId = device!!.name
                        deviceCredential.credentialsType = "ACCESS_TOKEN"
//                        AppDialogs.showProgressDialog(context = this, desc = "Updating device configuration...")
                        ThingsManager.saveDeviceCredential(c = this, deviceCredential = deviceCredential)
                    } else {
                        Snackbar.make(deviceNameView, "Could not load device configuration", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
                ThingsManager.API.saveDeviceCredential.hashCode() -> {
                    if (r is DeviceCredential) {
                        Snackbar.make(
                            deviceNameView,
                            "Device token has been updated",
                            Snackbar.LENGTH_LONG
                        ).show()
                        setResult(RESULT_OK)
                        if (isGateway) {
                            addDeviceToGateway()
                        }
                    } else {
                        Snackbar.make(
                            deviceNameView,
                            "Saving device token failed. Please try again later",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                ThingsManager.API.rpcTwoWay.hashCode() -> {
                    if (r.result == "ok") {
                        this.finish()
                        if (r.devType == "gw") {
                        }
                        addDeviceToServer()
                    } else if (r.statusMessage == "Failure") {

                        if (DeviceSetup == true) {
                            Snackbar.make(deviceNameView, "Could not add device information", Snackbar.LENGTH_LONG)
                                .show()
                            ThingsManager.deleteDevice(c = applicationContext, l = this, deviceId = gatewayDeviceId!!)
                            if (isGateway) {
                                ThingsManager.deleteentityGroup(
                                    c = applicationContext,
                                    l = this,
                                    entityGroupId = entityGroupId!!
                                )
                            }

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);

                        }
                    } else {
                        if (r.result == "fail") {
                            System.out.println("Device Fail By Gateway...----------------->>>>>>>>>>>>>>>>>>>>")
                            if (DeviceSetup == true)
                                if (isGateway) {
                                    ThingsManager.deleteentityGroup(
                                        c = applicationContext,
                                        l = this,
                                        entityGroupId = entityGroupId!!
                                    )
                                } else {
                                }

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                        }
                        Snackbar.make(
                            deviceNameView,
                            "Failed to add device to gateway",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                ThingsManager.API.saveRelation.hashCode() -> {

                    Snackbar.make(
                        deviceNameView,
                        "Added gateway->device relation",
                        Snackbar.LENGTH_LONG
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                ThingsManager.API.device.hashCode() -> {
                    if (r.requestMethod == Request.Method.DELETE) {
                        if (r.status == 200) {
                        } else {
                            Snackbar.make(
                                deviceNameView!!,
                                "Could not delete device. Please try again",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    } else if (r is Device) {
                    }
                }

                ThingsManager.API.enityGroup.hashCode() -> {
                    if (r.requestMethod == Request.Method.DELETE) {
                        if (r.status == 200) {
                        } else {
                            Snackbar.make(
                                deviceNameView!!,
                                "Could not delete device. Please try again",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    } else if (r is Device) {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


