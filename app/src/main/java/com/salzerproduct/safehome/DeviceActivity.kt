package com.salzerproduct.safehome

import android.Manifest
import android.app.Activity
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
import android.widget.Toast
import com.android.volley.Request
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.salzerproduct.database.model.*
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.model.Device
import com.salzerproduct.safehome.model.DeviceCredential
import com.salzerproduct.safehome.model.Entity
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.util.Utility
import com.salzerproduct.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.content_device.*
import kotlinx.android.synthetic.main.custom_dialog_layout.*
import org.json.JSONObject
import org.json.JSONTokener

class DeviceActivity : AppCompatActivity(), ResponseListener,
    QRCodeReaderView.OnQRCodeReadListener {
    private var device: Device? = null
    private var isGateway = false
    private var deviceId = ""
    private var entityGroupId = ""
    private var gatewayDeviceId = ""
    private var deviceIndex = 0
    private var DeviceSetup: Boolean? = false

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
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CAMERA),
                        102
                    )
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
            if (deviceNameView.text.toString().isNotEmpty() && deviceTypeView.text.toString()
                    .isNotEmpty() && deviceAccessTokenView.text.toString().isNotEmpty()
            ) {
                if (isGateway) {
                    if (entityGroupId.isEmpty()) {
                        AppDialogs.showProgressDialog(
                            context = this,
                            desc = "Adding Gateway configuration..."
                        )
                        DeviceSetup = true

                        ThingsManager.saveEntityGroup(
                            c = this,
                            groupName = deviceNameView.text.toString(),
                            description = "Security Gateway",
                            displayName = deviceNameView.text.toString()
                        )
                        AppPreference.put(applicationContext, "Gw", deviceNameView.text.toString())

                    } else {
                        DeviceSetup = false
                        addDeviceToServer()
                    }
                } else {
                    AppDialogs.showProgressDialog(
                        context = this!!,
                        desc = "Please wait adding device from gateway"
                    )
                    addDeviceToGateway()
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
        rpcJson.put("timeout", 45000)
        ThingsManager.callRPCTwoWay(
            c = this, l = this, deviceId = gatewayDeviceId, jsonObject = rpcJson
        )
    }

    private fun addDeviceToServer() {
        if (device == null
            || device!!.additionalInfo!!.displayName != deviceNameView.text.toString()
        ) {
            var newDevice = device
            if (device == null) {
                newDevice = Device()
                newDevice.name = deviceNameView.text.toString().trim()
                newDevice.type = deviceTypeView.text.toString().trim()
            }

            if (newDevice!!.additionalInfo!!.displayName != deviceNameView.text.toString()) {
            }
            newDevice.additionalInfo!!.displayName = deviceNameView.text.toString()
            newDevice.additionalInfo!!.deviceIndex = deviceIndex

            if (isGateway) {
                newDevice.additionalInfo!!.gateway = true
            }
            ThingsManager.saveDevice(
                c = this,
                l = this,
                entityGroupId = entityGroupId,
                device = newDevice
            )
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
                        Toast.makeText(
                            applicationContext,
                            "Device Gateway group has been added",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (Utility.isInternetAvailable(this)) {
                            if (isGateway) {
                                Handler().postDelayed({
                                    if (Utility.isInternetAvailable(this)) {
                                        addDeviceToServer()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "No Internet connection",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, 2500)
                            } else {
                                addDeviceToServer()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "No Internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
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

                                var Devicedetails = AppPreference.get(this, "DeviceSos", "")
                                if (Devicedetails!!.isEmpty()) {
                                    AppPreference.put(this, "DeviceSos", gatewayDeviceId)
                                }

                                val gatewaydetails =
                                    DatabaseClient.getInstance(applicationContext).appDatabase.addDeviceDAO!!.getDeviceid(
                                        r.id!!.id!!
                                    )
                                if (gatewaydetails == null) {
                                    val addDevice = AddDevice()
                                    addDevice.devicename = r.additionalInfo!!.displayName!!
                                    addDevice.deviceuid = deviceAccessTokenView.text.toString()
                                    addDevice.devicetype = r.type
                                    addDevice.deviceid = r.id!!.id!!
                                    addDevice.deviceindex =
                                        r.additionalInfo!!.deviceIndex!!.toString()
                                    addDevice.entitygroupid = entityGroupId
                                    addDevice.gatewayDeviceId = gatewayDeviceId

                                    try {
                                        DatabaseClient.getInstance(applicationContext).appDatabase.addDeviceDAO!!.insert(
                                            addDevice
                                        )
                                    } catch (e: SQLiteConstraintException) {
                                        System.out.println(e)
                                    }
                                }
                            } else {
                                ThingsManager.saveRelation(
                                    c = this,
                                    gatewayDeviceId = gatewayDeviceId,
                                    device = device!!
                                )
                                //ADD DETAILS TO LOCAL DATABASE
                                val addDevice = AddDevice()
                                addDevice.devicename = r.additionalInfo!!.displayName!!
                                addDevice.deviceuid = deviceAccessTokenView.text.toString()
                                addDevice.devicetype = r.type
                                addDevice.deviceid = r.id!!.id!!
                                addDevice.deviceindex = r.additionalInfo!!.deviceIndex!!.toString()
                                addDevice.entitygroupid = entityGroupId
                                addDevice.gatewayDeviceId = gatewayDeviceId

                                try {
                                    DatabaseClient.getInstance(applicationContext).appDatabase.addDeviceDAO!!.insert(
                                        addDevice
                                    )
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }
                            }
                            if (isNewDeviceSaved) {
                                ThingsManager.getDeviceCredentialsByDeviceId(
                                    c = this,
                                    deviceId = device!!.id!!.id!!
                                )
                            } else {
                                /* Device update is done */
                                if (isGateway) {
                                    setResult(Activity.RESULT_CANCELED)
                                } else {
                                    setResult(RESULT_OK)
                                }
//                                finish()
                            }

                            if (isNewDeviceSaved) {
                                ThingsManager.addAttributedeviceindex(
                                    c = this, l = this,
                                    deviceId = device!!.id!!.id!!,
                                    deviceindex = deviceIndex.toString() + "/" + deviceAccessTokenView.text.toString(),
                                    devicename = deviceNameView.text.toString(),
                                    devLabel = deviceNameView.text.toString(),
                                    devicetoken = deviceAccessTokenView.text.toString(),
                                    devicetype = deviceTypeView.text.toString(),
                                    devicestate = "ARMED"
                                )

//                                ThingsManager.addAttributedevicename(
//                                    c = this, l = this,
//                                    deviceId = device!!.id!!.id!!,
//                                    devicename = deviceNameView.text.toString()
//                                )
//
//                                ThingsManager.addAttributeEditdevicename(
//                                    c = this, l = this,
//                                    deviceId = device!!.id!!.id!!,
//                                    devLabel = deviceNameView.text.toString()
//                                )
//
//                                ThingsManager.addAttributedeviceuid(
//                                    c = this, l = this,
//                                    deviceId = device!!.id!!.id!!,
//                                    devicename = deviceAccessTokenView.text.toString()
//                                )
                            }
                        }
                    } else {
                        if (r.requestMethod == Request.Method.GET) {
                            Snackbar.make(
                                deviceNameView,
                                "Could not load device information",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Snackbar.make(
                                deviceNameView,
                                r.message.toString(),
                                Snackbar.LENGTH_LONG
                            )
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
                        deviceCredential.credentialsId = deviceAccessTokenView.text.toString()
                        deviceCredential.credentialsType = "ACCESS_TOKEN"
                        ThingsManager.saveDeviceCredential(
                            c = this,
                            deviceCredential = deviceCredential
                        )
                    } else {
                        Snackbar.make(
                            deviceNameView,
                            "Could not load device configuration",
                            Snackbar.LENGTH_LONG
                        )
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
                        } else {
                            var intent = Intent(applicationContext, Dashboard::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.putExtra("NewApp", "true")
                            startActivity(intent)
                            this.finish()
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
                        if (isGateway) {
                            var devnName = deviceNameView.text.toString()
                            var devicename = DeviceName()
                            devicename.devicename = devnName
                            DatabaseClient.getInstance(applicationContext).appDatabase!!.deviceNameDAO.insert(
                                devicename
                            )
                        }

                        if (r.devType == "gw") {
                            var intent = Intent(applicationContext, Dashboard::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.putExtra("NewApp", "true")
                            startActivity(intent)
                            this.finish()
                        } else {
                            addDeviceToServer()
                        }

                    } else if (r.statusMessage == "Failure") {

                        if (DeviceSetup == true) {

                            Snackbar.make(
                                deviceNameView,
                                "Could not add device information",
                                Snackbar.LENGTH_LONG
                            ).show()

                            ThingsManager.deleteDevice(
                                c = applicationContext,
                                l = this,
                                deviceId = gatewayDeviceId!!
                            )

                            if (isGateway) {
                                ThingsManager.deleteentityGroup(
                                    c = applicationContext,
                                    l = this,
                                    entityGroupId = entityGroupId!!
                                )
                            }

//                            finish();
//                            overridePendingTransition(0, 0);
//                            startActivity(getIntent());
//                            overridePendingTransition(0, 0);

                            val i =
                                packageManager.getLaunchIntentForPackage(packageName)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(i)
                            finish()
                        }

                    } else {
                        if (r.result == "fail" || r.result == "exists") {
                            System.out.println("Device Fail By Gateway...----------------->>>>>>>>>>>>>>>>>>>>")
                            if (DeviceSetup == true)
                                if (isGateway) {
                                    ThingsManager.deleteentityGroup(
                                        c = applicationContext,
                                        l = this,
                                        entityGroupId = entityGroupId!!
                                    )
                                } else {
//                                    ThingsManager.deleteDevice(
//                                        c = applicationContext,
//                                        l = this,
//                                        deviceId = gatewayDeviceId!!
//                                    )
                                }
//                            Toast.makeText(
//                                applicationContext,
//                                "Device Already Exists,Try with Other Device",
//                                Toast.LENGTH_SHORT
//                            ).show()

//                            val i =
//                                packageManager.getLaunchIntentForPackage(packageName)
//                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(i)
//                            finish()
                        }
                        Snackbar.make(
                            deviceNameView,
                            " Device Already Exists,Try with Other Device",
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
                            ThingsManager.deleteDevice(this, this, gatewayDeviceId)
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


