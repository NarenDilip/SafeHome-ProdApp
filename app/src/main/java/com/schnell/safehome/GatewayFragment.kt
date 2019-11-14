package com.schnell.safehome

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.github.ybq.android.spinkit.style.Circle
import com.schnell.database.model.*
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.model.*
import com.schnell.safehome.webservice.ThingsManager
import com.schnell.widget.AppDialogs
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [GatewayFragment.OnDeviceActionTriggered] interface.
 */

class GatewayFragment : Fragment(), ResponseListener {

    private var swipeView: android.support.v4.widget.SwipeRefreshLayout? = null
    private var list: RecyclerView? = null
    private var noDataView: TextView? = null
    private var armDevices: View? = null
    private var profile1: View? = null
    private var profile1Selector: View? = null
    private var profile2Selector: View? = null
    private var profile3Selector: View? = null
    private var UserSettingsSelector: View? = null
    private var profile2: View? = null
    private var profile3: View? = null
    private var disArmDevices: View? = null
    private var ApplyChanges: View? = null

    private var entityGroupId: String? = null
    private var DeviceId: String? = null
    private var ArmStatus: Boolean? = null
    private var StatusSetup: String? = "profile"
    private var CallState: String? = "Schnell"
    private var StatusId: String? = null
    private var jsonresponse: String? = null
    private var GetState: Boolean? = true
    private var deviceIdTobeModified: String? = null
    private var devices: ArrayList<Device> = arrayListOf()
    private var devicesMap: HashMap<String, Device> = HashMap()
    private var gatewayDevice: Device? = null
    private var position: Int = 0
    private var UserState: String? = null
    private var thisFragment = this
    private var randomval: Int? = null
    private var DvalId: String? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null
    private var mLatestAttributesDAO: LatestAttributesDAO? = null
    private var mLatestTelemetryDAO: LatestTelemetryDAO? = null
    private val doubleBounce = Circle()
    private var gatewayDataDeviceId: String? = null
    private var AttributeData: String? = null
    private var DName: String? = null
    private var UserGetStatus: Boolean? = false

    private val datamessage: String? = "{\"result\":\"okP1\",\"00\":0,\"56\":0,\"65\":0,\"98\":1,\"72\":1}"
    //    private var datamessage: String? = null
    private var profileDataStatus: String? = "Schnell"

    private var listener: OnDeviceActionTriggered = object : OnDeviceActionTriggered {
        override fun onDeviceButtonSwitched(device: Device?, arm: Boolean) {
            if (StatusSetup == "profile") {
                device!!.additionalInfo!!.armState = arm
                DeviceId = gatewayDevice!!.id!!.id!!
                deviceIdTobeModified = device?.id!!.id
            }
        }

        override fun onDeviceSelected(device: Device?) {
            if (StatusSetup == "profile") {
                val devicedao = mAddDeviceDAO!!.getDevicebyUid(device!!.name)
                val atrributedevice = mLatestAttributesDAO!!.getDevicebyUid(devicedao.deviceid)
                if (atrributedevice == null) {
                    DName = devicedao.devicename
                } else {
                    DName = atrributedevice.devicename
                }
                GetDeviceInfoActivity.GetDeviceActivity(
                    c = activity!!,
                    isGateway = false,
                    entityGroupId = entityGroupId!!,
                    gatewayDeviceId = devicedao!!.gatewayDeviceId,
                    deviceId = devicedao!!.deviceid,
                    deviceIndex = devicedao!!.deviceindex.toInt(),
                    devicename = DName,
                    devicetype = devicedao.devicetype,
                    deviceuid = devicedao.deviceuid
                )
            }
        }

        override fun onDeviceLongPressed(device: Device?) {
            val devicedao = mAddDeviceDAO!!.getDevicebyUid(device!!.name)
            val devicedata = mAddDeviceDAO!!.getOnlyEntityGroup(devicedao.entitygroupid)
            val l = object : AppDialogs.ConfirmListener {
                override fun yes() {
                    val deviceIndex = devicedao!!.deviceindex.toInt()
                    val rpcJson = JSONObject()
                    val params = JSONObject()
                    params.put("devIndex", "$deviceIndex".padStart(2, '0'))
                    params.put("devId", device.name)
                    params.put("devType", device.type)
                    rpcJson.put("params", params)
                    rpcJson.put("method", "remDevice")
                    rpcJson.put("timeout", 25000)
                    profileDataStatus = "removeDevice"
                    ThingsManager.callRPCTwoWay(
                        c = activity!!,
                        l = thisFragment,
                        deviceId = devicedata.deviceid,
                        jsonObject = rpcJson
                    )

                    AppDialogs.showProgressDialog(
                        context = activity!!,
                        desc = "Please wait removing device from gateway"
                    )
                    deviceIdTobeModified = device.id!!.id
                }
            }
            AppDialogs.confirmAction(c = activity!!, text = "Sure!! you want to remove this device?", l = l)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(entityGroupId: String, position: Int = 0, strUser: String) =
            GatewayFragment().apply {
                arguments = Bundle().apply {
                    putString("entityGroupId", entityGroupId)
                    putInt("position", position)
                    putString("state", strUser)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entityGroupId = it.getString("entityGroupId")
            position = it.getInt("position")
            UserState = it.getString("state")
        }

        mAddDeviceDAO =
            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .fallbackToDestructiveMigration()
                .build()
                .addDeviceDAO

        mLatestAttributesDAO =
            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .fallbackToDestructiveMigration()
                .build()
                .geAttributesDAO()

        mLatestTelemetryDAO =
            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .fallbackToDestructiveMigration()
                .build()
                .getTelemetryDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.content_device_fragment, container, false)
        try {
            // Set the adapter
            val fab = view.findViewById<FloatingActionButton>(R.id.fab)
            val settings = view.findViewById<ImageView>(R.id.settings)
            val gatewayInfo = view.findViewById<FloatingActionButton>(R.id.gatewayInfo)
            val notifications = view.findViewById<FloatingActionButton>(R.id.notifications)
            val history = view.findViewById<FloatingActionButton>(R.id.history)
            val factoryreset = view.findViewById<FloatingActionButton>(R.id.factoryreset)

            Handler().postDelayed({
                val myentity = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
                if (myentity.isEmpty()) {
                    AppDialogs.showProgressDialog(context = activity!!, desc = "Please wait fetching device details")
                    GetDetails(context!!, entityGroupId!!, thisFragment!!).execute()
                }
            }, 1000)

            fab.setOnClickListener {
                if (gatewayDevice == null) {
                    Snackbar.make(fab, "Please configure gateway before adding devices", Snackbar.LENGTH_SHORT).show()
                } else {

                    val indexvalue = getRandomNumber()

                    if (indexvalue != null) {
                        DeviceActivity.openDeviceActivity(
                            c = activity!!,
                            isGateway = false,
                            entityGroupId = entityGroupId!!,
                            gatewayDeviceId = gatewayDevice!!.id!!.id,
                            deviceId = "",
                            deviceIndex = indexvalue!!
                        )
                    }

//                    val list = ArrayList<Int>()
//                    for (i in 10..100) {
//                        list.add(i)
//                    }
//                    Collections.shuffle(list)
//                    for (i in 0..4) {
//                        System.out.println(list[i])
//                        randomval = list[i]
//                    }
//
//                    val counters = mAddDeviceDAO!!.getDeviceindex(randomval.toString()!!)
//
//                    if (counters == null) {
//
//                        DeviceActivity.openDeviceActivity(
//                            c = activity!!,
//                            isGateway = false,
//                            entityGroupId = entityGroupId!!,
//                            gatewayDeviceId = gatewayDevice!!.id!!.id,
//                            deviceId = "",
//                            deviceIndex = randomval!!
//                        )
//                    } else {
//                        val list = ArrayList<Int>()
//                        for (i in 10..99) {
//                            list.add(i)
//                        }
//                        Collections.shuffle(list)
//                        for (i in 0..4) {
//                            System.out.println(list[i])
//                            randomval = list[i]
//                        }
//
//                        DeviceActivity.openDeviceActivity(
//                            c = activity!!,
//                            isGateway = false,
//                            entityGroupId = entityGroupId!!,
//                            gatewayDeviceId = gatewayDevice!!.id!!.id,
//                            deviceId = "",
//                            deviceIndex = randomval!!
//                        )
//                    }
                }
            }

            history.setOnClickListener {
                //                getLatestTelemetry()
            }

            settings.setOnClickListener {
                val intent = Intent(getActivity(), UserSettingsActivity::class.java)
                getActivity()?.startActivity(intent)
            }

            factoryreset.setOnClickListener {
                val l = object : AppDialogs.ConfirmListener {
                    override fun yes() {

                        var filtervalues = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
                        if (filtervalues.isNotEmpty()) {

                            for (i in 0 until filtervalues!!.size) {
                                ThingsManager.deleteDevice(
                                    c = activity!!,
                                    l = thisFragment,
                                    deviceId = filtervalues[i].deviceid
                                )
                            }

                            ThingsManager.deleteentityGroup(
                                c = activity!!,
                                l = thisFragment,
                                entityGroupId = entityGroupId!!
                            )
                        }
                    }
                }
                AppDialogs.confirmAction(c = activity!!, text = "Do you want to Factory Rest?", l = l)
            }

            notifications.setOnClickListener {
                val intent = Intent(getActivity(), NotificationActivity::class.java)
                getActivity()?.startActivity(intent)
            }

            gatewayInfo.setOnClickListener {
                val deviceId = if (gatewayDevice != null) gatewayDevice!!.id!!.id!! else ""
                DeviceActivity.openDeviceActivity(
                    c = activity!!,
                    isGateway = true,
                    entityGroupId = entityGroupId!!,
                    gatewayDeviceId = deviceId,
                    deviceId = deviceId,
                    deviceIndex = 0
                )
            }
            list = view.findViewById(R.id.list)

            with(list!!) {
                layoutManager = LinearLayoutManager(context)
                adapter = DeviceRecyclerViewAdapter(devices, listener)
            }

            noDataView = view.findViewById(R.id.noData)
            swipeView = view.findViewById(R.id.swipeView)
            swipeView!!.setColorSchemeColors(*resources.getIntArray(R.array.chart_colors))
            swipeView!!.setOnRefreshListener {

                if (UserGetStatus == true) {
                    loadDevices()
                } else {
                    callGetState()
                }
            }

//            if (entityGroupId != null) {
//                ThingsManager.getDevices(c = activity!!, l = this, entityGroupId = entityGroupId!!)
//            }
            armDevices = view.findViewById(R.id.armDevices)
            profile1 = view.findViewById(R.id.profile1)
            profile1Selector = view.findViewById(R.id.profile1_selector)
            profile2Selector = view.findViewById(R.id.profile2_selector)
            profile3Selector = view.findViewById(R.id.profile3_selector)
            UserSettingsSelector = view.findViewById(R.id.settings_selector)
            profile2 = view.findViewById(R.id.profile2)
            profile3 = view.findViewById(R.id.profile3)
            disArmDevices = view.findViewById(R.id.disArmDevices)
            ApplyChanges = view.findViewById(R.id.apply_change)
            setProfileListeners(context)

            if (UserState == "true") {
                callGetState()
            } else {
                loadDevices()
            }
//

        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Checking the response
//        profile1Selector!!.setBackgroundResource(R.color.litegreen)
//        StatusId = "1"
        ApplyChanges!!.visibility = View.GONE

        ApplyChanges?.setOnClickListener {
            if (StatusId == "1") {
                applyProfile(
                    profileButton = it,
                    profileKey = "1",
                    profile = gatewayDevice!!.additionalInfo!!.profile1
                )
            } else if (StatusId == "2") {
                applyProfile(
                    profileButton = it,
                    profileKey = "2",
                    profile = gatewayDevice!!.additionalInfo!!.profile2
                )
            } else if (StatusId == "3") {
                applyProfile(
                    profileButton = it,
                    profileKey = "3",
                    profile = gatewayDevice!!.additionalInfo!!.profile3
                )
            }
        }

//        callGetState()

        return view
    }

    private fun getRandomNumber(): Int {

        val list = ArrayList<Int>()
        for (i in 10..99) {
            list.add(i)
        }
        Collections.shuffle(list)
        for (i in 0..4) {
            System.out.println(list[i])
            randomval = list[i]
        }

        val counters = mAddDeviceDAO!!.getDeviceindex(randomval.toString()!!)

        if (counters == null) {
            return randomval!!
        } else {
            getRandomNumber()
        }
        return randomval!!
    }

    private fun callGetState() {
//        StatusSetup = "GetState"
        CallState = "Getstate"
        StatusSetup = "GetState"
        val params = JSONObject()
        params.put("profilestate", "get")

        val rpcJson = JSONObject()
        rpcJson.put("params", params)
        rpcJson.put("method", "armState")
        rpcJson.put("timeout", 25000)

        val myentity = mAddDeviceDAO!!.getOnlyEntityGroup(entityGroupId)
        if (myentity.deviceid != null) {

            AppDialogs.showProgressDialog(
                context = activity!!,
                desc = "Please wait fetching state from server"
            )

            if (myentity.deviceindex.equals("0")) {
                profileDataStatus = "Schnell"
                ThingsManager.callRPCTwoWayGetState(
                    c = context!!, l = thisFragment, deviceId = myentity.deviceid, jsonObject = rpcJson
                )
            }
        }
    }

    private fun getLatestAttributes() {

        mLatestAttributesDAO!!.DeleteSensor()
        val myentity = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
        if (!myentity.isEmpty()) {
            for (i in 0 until myentity.size) {

                AppDialogs.showProgressDialog(
                    context = activity!!,
                    desc = "Please wait fetching state from server"
                )

                ThingsManager.getDeviceLatestAttributes(
                    c = activity!!,
                    l = thisFragment,
                    deviceId = myentity[i].deviceid,
                    entityType = "DEVICE",
                    Keys = "devicename"
                )
                if (myentity.size.equals(i + 1)) {
                    getLatestTelemetry()
                }
            }
        }
    }

    private fun getLatestTelemetry() {

        mLatestTelemetryDAO!!.DeleteTelemetry()
        val myentity = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
        if (!myentity.isEmpty()) {
            for (i in 0 until myentity.size) {

                AppDialogs.showProgressDialog(
                    context = activity!!,
                    desc = "Please wait fetching state from server"
                )
                ThingsManager.getDeviceLatestTelemetry(
                    c = activity!!,
                    l = thisFragment,
                    deviceId = myentity[i].deviceid,
                    entityType = "DEVICE",
                    Keys = "alert"
                )
                if (myentity.size.equals(i + 1)) {
                    loadDevices()
                }
            }
        }
    }


    private fun elevateProfile(view: View) {
        armDevices!!.alpha = 1f
        profile1!!.alpha = 1f
        profile2!!.alpha = 1f
        profile3!!.alpha = 1f
        disArmDevices!!.alpha = 1f

        view.alpha = .5f
    }

    private fun setProfileListeners(context: Context?) {
        try {
            disArmDevices!!.setOnClickListener { it ->

                val l = object : AppDialogs.ConfirmListener {
                    override fun yes() {

                        StatusSetup = "ArmAll"
                        if (devices.size > 0) {

                            val params = JSONObject()
                            params.put("profilestate", "arm")

                            val rpcJson = JSONObject()
                            rpcJson.put("params", params)
                            rpcJson.put("method", "armState")
                            rpcJson.put("timeout", 25000)

                            AppDialogs.showProgressDialog(context = activity!!, desc = "Applying Arm All devices")
                            profileDataStatus = "Schnell"

                            // Call gateway to arm all device
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = rpcJson
                            )

                            for (device in devices) {
                                device.additionalInfo!!.armState = false
                            }
                            list?.adapter?.notifyDataSetChanged()

                        } else {
                            Snackbar.make(view!!, "Please add at least one device", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                AppDialogs.confirmAction(c = activity!!, text = "Do you want to Arm all this device?", l = l)
            }
            armDevices!!.setOnClickListener { it ->

                val l = object : AppDialogs.ConfirmListener {
                    override fun yes() {

                        ArmStatus = false
                        StatusSetup = "DisArmAll"
                        if (devices.size > 0) {
                            val params = JSONObject()
                            params.put("profilestate", "disarm")

                            val rpcJson = JSONObject()
                            rpcJson.put("params", params)
                            rpcJson.put("method", "armState")
                            rpcJson.put("timeout", 25000)

                            AppDialogs.showProgressDialog(context = activity!!, desc = "Applying Dis-Arm All devices")
                            profileDataStatus = "Schnell"
                            // Call gateway to dis arm all device
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = rpcJson
                            )
                            for (device in devices) {
                                device.additionalInfo!!.armState = true
                            }
                        } else {
                            Snackbar.make(view!!, "Please add at least one device", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                AppDialogs.confirmAction(c = activity!!, text = "Do you want to DisArm all this device?", l = l)
            }

            profile1!!.setOnClickListener { it ->
                StatusSetup = "profile"
                StatusId = "1"
                profileDataStatus = "Schnell"
                profile1Selector!!.setBackgroundResource(R.color.litegreen)
                profile2Selector!!.setBackgroundResource(R.color.whiter)
                profile3Selector!!.setBackgroundResource(R.color.whiter)
                disArmDevices!!.setBackgroundResource(R.color.whiter)
                armDevices!!.setBackgroundResource(R.color.whiter)
                ApplyChanges!!.visibility = View.VISIBLE

                elevateProfile(it)
                loadDevices()
            }

            profile2!!.setOnClickListener { it ->
                StatusSetup = "profile"
                StatusId = "2"
                profileDataStatus = "Schnell"
                profile2Selector!!.setBackgroundResource(R.color.litegreen)
                profile1Selector!!.setBackgroundResource(R.color.whiter)
                profile3Selector!!.setBackgroundResource(R.color.whiter)
                disArmDevices!!.setBackgroundResource(R.color.whiter)
                armDevices!!.setBackgroundResource(R.color.whiter)

                elevateProfile(it)
                loadDevices()
            }

            profile3!!.setOnClickListener { it ->
                StatusSetup = "profile"
                StatusId = "3"
                profileDataStatus = "Schnell"
                profile3Selector!!.setBackgroundResource(R.color.litegreen)
                profile1Selector!!.setBackgroundResource(R.color.whiter)
                profile2Selector!!.setBackgroundResource(R.color.whiter)
                disArmDevices!!.setBackgroundResource(R.color.whiter)
                armDevices!!.setBackgroundResource(R.color.whiter)

                elevateProfile(it)
                loadDevices()
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun applyProfile(profileButton: View, profileKey: String, profile: String?) {
        try {
            if (devices.size > 0) {
                val rpcJson = if (profile == null || profile.isEmpty() || profile != null) {
                    val params = JSONObject()
                    for (device in devices) {
                        params.put(
                            "${device.additionalInfo!!.deviceIndex}".padStart(2, '0'),
                            if (device.additionalInfo!!.armState != null && device.additionalInfo!!.armState!!) 1 else 0
                        )
                    }
                    val jsonObject = JSONObject()
                    jsonObject.put("params", params)
                    jsonObject.put("method", "p$profileKey")
                    jsonObject.put("timeout", 25000)
                    when (profileKey) {
                        "1" -> gatewayDevice!!.additionalInfo!!.profile1 = jsonObject.toString()
                        "2" -> gatewayDevice!!.additionalInfo!!.profile2 = jsonObject.toString()
                        "3" -> gatewayDevice!!.additionalInfo!!.profile3 = jsonObject.toString()

                    }
                    jsonObject
                } else {
                    JSONTokener(profile).nextValue() as JSONObject
                }

                jsonresponse = rpcJson.toString()
                StatusSetup = "ApplyAll"

                AppDialogs.showProgressDialog(context = activity!!, desc = "Applying Profile $profileKey...")

                // Apply selected profile
                ThingsManager.callRPCTwoWay(
                    c = activity!!, l = thisFragment, deviceId = gatewayDevice!!.id!!.id!!, jsonObject = rpcJson
                )
            } else {
                Snackbar.make(view!!, "Please add at least one device", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDevices() {
        if (entityGroupId != null) {
            devices.clear()
            devicesMap.clear()
            list!!.adapter!!.notifyDataSetChanged()

            AppDialogs.showProgressDialog(
                context = activity!!,
                desc = "Please wait fetching state from server"
            )
            ThingsManager.getDevices(c = activity!!, l = this, entityGroupId = entityGroupId!!)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        activity!!.menuInflater.inflate(R.menu.add_device_option, menu)
    }

    interface OnDeviceActionTriggered {
        fun onDeviceSelected(device: Device?)
        fun onDeviceLongPressed(device: Device?)
        fun onDeviceButtonSwitched(device: Device?, arm: Boolean = true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        loadDevices()
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun triggerSOSAction() {
        Snackbar.make(view!!, "Replace with Trigger SOS SMS action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    override fun onResponse(r: Response?) {
        if (r == null) {
            return
        }
        try {
            AppDialogs.hideProgressDialog()
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
                AppPreference.clearAll(c = activity!!)
                Snackbar.make(view!!, "Session expired. Please login again", Snackbar.LENGTH_LONG).show()
                Handler().postDelayed({
                    val i = context!!.getPackageManager().getLaunchIntentForPackage(context!!.getPackageName())
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    activity!!.finish()
                }, 2000)

            } else if (r.statusMessage == "Failure") {

                if (StatusSetup == "ArmAll") {
                    StatusId = "1"
                    ApplyChanges!!.visibility = View.VISIBLE
//                    loadDevices()

                    disArmDevices!!.setBackgroundResource(R.color.litegreen)
                    armDevices!!.visibility = View.GONE
                    disArmDevices!!.visibility = View.VISIBLE
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    UserSettingsSelector!!.setBackgroundResource(R.color.whiter)
                    !swipeView!!.isFocusableInTouchMode
                    ApplyChanges!!.visibility = View.GONE

                } else if (StatusSetup == "DisArmAll") {

                    StatusId = "1"
                    ApplyChanges!!.visibility = View.VISIBLE
//                    loadDevices()

                    armDevices!!.visibility = View.VISIBLE
                    disArmDevices!!.visibility = View.GONE
                    armDevices!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    UserSettingsSelector!!.setBackgroundResource(R.color.whiter)

                    ApplyChanges!!.visibility = View.GONE

                } else if (StatusSetup == "Profile") {
//                    loadDevices()
                } else if (StatusSetup == "GetState") {
                    loadDevices()
                }
                Snackbar.make(view!!, "Unable to connect to Gateway, Please try again Later.", Snackbar.LENGTH_LONG)
                    .show()

//                loadDevices()

            } else when (r.requestType) {
                ThingsManager.API.entitiesUnderGroup.hashCode() -> {
                    swipeView!!.isRefreshing = false
                    if (r is Device) {
                        devices.clear()
                        devicesMap.clear()
                        list!!.adapter!!.notifyDataSetChanged()
                        for (device in r.deviceList!!) {
                            GetState = true
                            ThingsManager.getDevice(c = activity!!, l = this, deviceId = device.id!!.id!!)

                        }
                        if (noDataView != null) {
                            if (r.deviceList!!.size <= 1) {
                                noDataView!!.visibility = View.VISIBLE
                                list!!.visibility = View.GONE
                            } else {
                                noDataView!!.visibility = View.GONE
                                list!!.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        if (view != null)
                            Snackbar.make(view!!, "Could not load devices", Snackbar.LENGTH_LONG).show()
                    }
                }

                ThingsManager.API.getDeviceAttribute.hashCode() -> {
                    if (r is LatestTelemetryData) {
                        if (!r.telemetrylist.isNullOrEmpty()) {
                            for (i in 0 until r.telemetrylist!!.size) {
                                AttributeData
                                r.telemetrylist!![i].telkey

                                val splitters = r.telemetrylist!![i].telvalue!!.split("/*/")
                                val attdevice = mAddDeviceDAO!!.getDevicebyUid(splitters[1])

                                val latestAttribute = LatestAttribute()
                                latestAttribute.deviceid = attdevice.deviceid
                                latestAttribute.devicename = splitters[0]

                                try {
                                    mLatestAttributesDAO!!.insert(latestAttribute)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }
                            }
                        }
                    } else if (r is History) {
                        if (r is History) {
                            if (r.value == null) {
                            } else {
                                var teledata = LatestTelemetry()
                                teledata.deviceid = ""
                                teledata.devicetelemetry = r.name

                                try {
                                    mLatestTelemetryDAO!!.insert(teledata)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }
                            }
                        }
                    } else {
                        loadDevices()
                    }
                }

                ThingsManager.API.device.hashCode() -> {

                    if (GetState == true) {
                        if (r.requestMethod == Request.Method.DELETE) {
                            if (r.status == 200) {

                                //DELETE DEVICE IN LOCAL DATABASE
                                val resultststae = mAddDeviceDAO!!.DeleteSensor(r.devid!!)
                                loadDevices()

                            } else {
                                Snackbar.make(view!!, "Could not delete device. Please try again", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                            loadDevices()
                        } else if (r is Device) {
                            if (r.requestMethod == Request.Method.GET) {
                                if (!devicesMap.keys.contains(r.id!!.id)) {
                                    if (r.additionalInfo != null && r.additionalInfo!!.gateway == true) {
                                        gatewayDevice = r
//                                    TODO selected profile should be highlighted
                                    } else {
//                                        datamessage = r.result
                                        if (!r.devIndex.equals("0")) {

                                            if (profileDataStatus == "Arm") {
                                                r.additionalInfo!!.armState = true
                                                StatusSetup = "Arm"

                                                val attributedatas = mLatestAttributesDAO!!.getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName = attributedatas.devicename
                                                    }
                                                }


                                            } else if (profileDataStatus == "DisArm") {
                                                r.additionalInfo!!.armState = false
                                                StatusSetup = "DisArm"

                                                val attributedatas = mLatestAttributesDAO!!.getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName = attributedatas.devicename
                                                    }
                                                }


                                            } else if (profileDataStatus == "p1") {

//                                            var datamessage = "{\\\"result\\\":\\\"ok\\\",\\\"00\\\":0,\\\"45\\\":1,\\\"88\\\":0,\\\"84\\\":1,\\\"40\\\":,\\\"method\\\":\\\"p1\\\"}"
                                                val devicedao = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
                                                val jresponse = JSONObject(datamessage)
                                                for (i in 0 until devicedao.size) {

                                                    if (r.additionalInfo!!.deviceIndex!!.equals(devicedao[i].deviceindex.toInt())) {
                                                        r.additionalInfo!!.armState =
                                                            jresponse.getString(devicedao[i].deviceindex) == "0"
                                                    }
                                                }

                                                val attributedatas = mLatestAttributesDAO!!.getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName = attributedatas.devicename
                                                    }
                                                }

                                                ApplyChanges!!.visibility = View.VISIBLE

                                                StatusSetup = "profile"
                                            } else if (profileDataStatus == "okP2") {

                                                val devicedao = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
                                                val jresponse = JSONObject(datamessage)
                                                for (i in 0 until devicedao.size) {

                                                    if (r.additionalInfo!!.deviceIndex!!.equals(devicedao[i].deviceindex.toInt())) {
                                                        r.additionalInfo!!.armState =
                                                            jresponse.getString(devicedao[i].deviceindex) == "0"
                                                    }
                                                }
                                                StatusSetup = "profile"
                                            } else if (profileDataStatus == "okP3") {

                                                val devicedao = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
                                                val jresponse = JSONObject(datamessage)
                                                for (i in 0 until devicedao.size) {

                                                    if (r.additionalInfo!!.deviceIndex!!.equals(devicedao[i].deviceindex.toInt())) {
                                                        r.additionalInfo!!.armState =
                                                            jresponse.getString(devicedao[i].deviceindex) == "0"
                                                    }
                                                }
                                                StatusSetup = "profile"
                                            } else if (profileDataStatus == "Schnell") {

                                                val attributedata = mLatestAttributesDAO!!.devices
                                                val attributedatas = mLatestAttributesDAO!!.getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName = attributedatas.devicename
                                                    }
                                                }
                                            }

                                            list!!.visibility = View.VISIBLE
                                            list!!.adapter!!.notifyDataSetChanged()
                                        }
                                        devices.add(r)
                                        devicesMap[r.id!!.id!!] = r


                                    }
                                }
                            } else {
                                if (r.additionalInfo!!.gateway == true) {
                                    gatewayDevice = r
                                } else {
                                    devicesMap[r.id!!.id]!!.additionalInfo = r.additionalInfo
                                    list!!.adapter!!.notifyDataSetChanged()
                                }
                                if (r.extraOutput != null) {
                                    Snackbar.make(view!!, r.extraOutput!!, Snackbar.LENGTH_LONG).show()
                                } else {
                                    Snackbar.make(view!!, "Updated device ${r.getDisplayName()}", Snackbar.LENGTH_LONG)
                                        .show()
                                    loadDevices()
                                }
                            }
                        }
                    } else {
                        if (GetState == false) {
                            if (r is ThingsBoardResponse) {
                                r.additionalInfo!!.deviceIndex!!

                                val devicedetails = mAddDeviceDAO!!.getDeviceid(r.id!!.id!!)

                                val addDevice = AddDevice()
                                addDevice.devicename = r.additionalInfo!!.displayName
                                addDevice.deviceuid = devicedetails.deviceuid
                                addDevice.devicetype = devicedetails.devicetype
                                addDevice.deviceid = r.id!!.id!!
                                addDevice.deviceindex = r.additionalInfo!!.deviceIndex!!.toString()
                                addDevice.entitygroupid = devicedetails.entitygroupid

                                try {
                                    mAddDeviceDAO!!.updateall(
                                        r.additionalInfo!!.displayName,
                                        r.additionalInfo!!.deviceIndex!!.toString(),
                                        r.id!!.id!!
                                    )
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

                            } else {
                            }
                            callGetState()
                        }
                    }
                }
                ThingsManager.API.rpcTwoWaygetStatus.hashCode() -> {
                    if (profileDataStatus == "Schnell") {
                        if (r.result == "ok" || r.result == "okP1" || r.result == "okP2" || r.result == "okP3") {
                            UserGetStatus = true
                            if (r.profilestate == "arm") {
//                                if (CallState == "Getstate") {
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.GONE
                                armDevices!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "Arm"
                                loadDevices()

                            } else if (r.profilestate == "disarm") {

                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.visibility = View.GONE
                                disArmDevices!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "DisArm"
                                loadDevices()
                            } else if (r.profilestate == "p1") {

                                profile1Selector!!.setBackgroundResource(R.color.litegreen)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.GONE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "p1"

                                ApplyChanges!!.visibility = View.VISIBLE

                                loadDevices()
//                                getLatestAttributes()

                            } else if (r.result == "okP2") {

                                profile2Selector!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.GONE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP2"
                                ApplyChanges!!.visibility = View.VISIBLE

                                loadDevices()

                            } else if (r.result == "okP3") {

                                profile3Selector!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.GONE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP3"
                                ApplyChanges!!.visibility = View.VISIBLE

                                loadDevices()
                            }

                        } else if (r.statusMessage == "Failure") {
                            Snackbar.make(
                                view!!,
                                r.statusMessage.toString() + " " + "Unable to get the state",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    } else if (profileDataStatus == "removeDevice") {
                        ThingsManager.deleteDevice(
                            c = activity!!,
                            l = this,
                            deviceId = deviceIdTobeModified!!
                        )

                        mAddDeviceDAO!!.DeleteSensor(deviceIdTobeModified)
                        profileDataStatus = "Schnell"
                        loadDevices()
                    }
                    getLatestAttributes()
                }

                ThingsManager.API.rpcTwoWay.hashCode() -> {
                    if (r.result == "ok" || r.result == "okp1" || r.result == "okp2" || r.result == "okp3") {
                        if (r.profilestate == "arm") {
                        } else if (r.profilestate == "disarm") {
                        } else if (r.profilestate == "p1") {
                        } else if (r.profilestate == "p2") {
                        } else if (r.profilestate == "p3") {
                        }

                        if (StatusSetup == "ArmAll") {

//                            // Save arm state in gateway
                            gatewayDevice!!.additionalInfo!!.armState = true
                            gatewayDevice!!.additionalInfo!!.selectedProfile = "ad"

                            // Update all devices either arm
                            AppDialogs.showProgressDialog(context = activity!!, desc = "Arming all devices...")


                            for (device in devices) {
                                ThingsManager.saveDevice(
                                    c = activity!!, l = thisFragment, entityGroupId = entityGroupId!!, device = device
                                )
                            }

                            armDevices!!.visibility = View.GONE
                            disArmDevices!!.visibility = View.VISIBLE
                            armDevices!!.setBackgroundResource(R.drawable.selected_circle_accent)


                        } else if (StatusSetup == "DisArmAll") {

                            // Save arm state in gateway
                            gatewayDevice!!.additionalInfo!!.armState = false
                            gatewayDevice!!.additionalInfo!!.selectedProfile = "ad"

                            // Update all devices either dis arm
                            AppDialogs.showProgressDialog(context = activity!!, desc = "Dis arming all devices...")


                            for (device in devices) {
                                device.additionalInfo!!.armState = false
                                ThingsManager.saveDevice(
                                    c = activity!!, l = thisFragment, entityGroupId = entityGroupId!!, device = device
                                )
                            }

                            disArmDevices!!.visibility = View.GONE
                            armDevices!!.visibility = View.VISIBLE
                            disArmDevices!!.setBackgroundResource(R.drawable.selected_circle_accent)

                        } else if (StatusSetup == "ApplyAll") {

//                             Save profile in gateway
                            gatewayDevice!!.additionalInfo!!.armState = null
                            gatewayDevice!!.additionalInfo!!.selectedProfile = "p$StatusId"

                            val jresponse = JSONObject(jsonresponse)

//                            Update all devices either dis arm based on profile
                            AppDialogs.showProgressDialog(
                                context = activity!!,
                                desc = "Updating devices with Profile $StatusSetup..."
                            )


                            val params = jresponse.get("params") as JSONObject
                            for (device in devices) {
                                device.additionalInfo!!.armState = params.get(
                                    "${device.additionalInfo!!.deviceIndex}".padStart(
                                        2,
                                        '0'
                                    )
                                ) == 1
                                ThingsManager.saveDevice(
                                    c = activity!!, l = thisFragment, entityGroupId = entityGroupId!!, device = device
                                )
                            }
                        } else {
                            val request = JSONTokener(r.extraOutput).nextValue() as JSONObject
                            when (request["method"]) {
                                "ad" -> {
                                    // arm / disarm Device
                                    val params = request["params"] as JSONObject
                                    when {
                                        deviceIdTobeModified != null -> {
                                            val device = devicesMap[deviceIdTobeModified!!]!!
                                            device.additionalInfo!!.armState =
                                                params["${device.additionalInfo!!.deviceIndex!!}".padStart(
                                                    2,
                                                    '0'
                                                )] == 1
                                            ThingsManager.saveDevice(
                                                c = activity!!,
                                                l = thisFragment,
                                                entityGroupId = entityGroupId!!,
                                                device = device,
                                                extraOutput = "Updated device arm status for ${device.getDisplayName()}"
                                            )
                                        }
                                    }
                                }
                                "remDevice" -> {
//                                TODO remove indexes from each gateway profiles p1, p2, p3
                                    ThingsManager.deleteDevice(
                                        c = activity!!,
                                        l = this,
                                        deviceId = deviceIdTobeModified!!
                                    )
                                    loadDevices()
                                }
                                "p1", "p2", "p3" -> {
                                    // TODO UPDATE PROFILE
                                }
                            }
                        }
                    } else if (r.statusMessage == "Failure") {

                    }
                    deviceIdTobeModified = null
                    profileDataStatus = "Schnell"
                }


                ThingsManager.API.telemetryData.hashCode() -> {
                    if (r is History) {
                        r.historyList?.size
                    }
                    System.out.println("response---->$")
                }

                ThingsManager.API.getDeviceFromenityGroup.hashCode() -> {
                    if (r is Device)
                        for (devsensor in r.deviceList!!) {

                            val addDevice = AddDevice()
                            addDevice.devicename = ""
                            addDevice.deviceuid = devsensor.name!!
                            addDevice.devicetype = devsensor.type!!
                            DvalId = devsensor!!.id!!.id!!
                            addDevice.deviceid = devsensor!!.id!!.id!!
                            addDevice.deviceindex = ""
                            addDevice.entitygroupid = entityGroupId
                            addDevice.gatewayDeviceId = devsensor!!.id!!.id!!
                            gatewayDataDeviceId = devsensor!!.id!!.id!!

                            try {
                                mAddDeviceDAO!!.insert(addDevice)
                            } catch (e: SQLiteConstraintException) {
                                System.out.println(e)
                            }

                            GetState = false
                            AppDialogs.showProgressDialog(context = activity!!, desc = "Fetching Datas From Server")
                            ThingsManager.getdevicedetails(
                                c = activity!!,
                                l = this,
                                deviceId = DvalId!!
                            )
                        }
                    ThingsManager.addAttribute(c = activity!!, l = thisFragment, deviceId = gatewayDataDeviceId!!)
                }
                ThingsManager.API.enityGroup.hashCode() -> {
                    if (r.result == "ok") {
                        Handler().postDelayed({
                            val i = context!!.getPackageManager().getLaunchIntentForPackage(context!!.getPackageName())
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(i)
                            activity!!.finish()
                        }, 2000)
                    } else if (r.statusMessage == "Failure") {
                        Snackbar.make(view!!, r.statusMessage.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        loadDevices()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    class GetDetails(
        context: Context,
        entityGroupId: String,
        thisFragment: GatewayFragment
    ) : AsyncTask<Unit, Unit, String>() {

        var context = context;
        var entityGroupId = entityGroupId
        var thisFragment = thisFragment

        override fun doInBackground(vararg params: Unit?): String? {
            System.out.println("Testing            ->>>>>>>>>>>>>>>>>>>>>>>>>>>")
            ThingsManager.getdevicelist(
                c = context!!,
                l = thisFragment,
                entityGroupId = entityGroupId!!
            )
            return null
        }
    }

}



