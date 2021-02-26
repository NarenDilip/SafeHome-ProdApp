package com.salzerproduct.safehome

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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.github.ybq.android.spinkit.style.Circle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import com.salzerproduct.database.model.*
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.Utils.BaseActivity
import com.salzerproduct.safehome.model.*
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.widget.AppDialogs
import org.json.JSONObject
import org.json.JSONTokener
import java.lang.Integer.parseInt
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
//    private var ApplyChanges: View? = null
    private var ArmFlow: FrameLayout? = null
    private var pro1: FrameLayout? = null
    private var pro2: FrameLayout? = null
    private var pro3: FrameLayout? = null
    private var uset: FrameLayout? = null
    private var DialogTexter: pl.droidsonroids.gif.GifImageView? = null

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

//    private var mAddDeviceDAO: AddDeviceDAO? = null
//    private var mLatestAttributesDAO: LatestAttributesDAO? = null
//    private var mLatestTelemetryDAO: LatestTelemetryDAO? = null

    private val doubleBounce = Circle()
    private var gatewayDataDeviceId: String? = null
    private var AttributeData: String? = null
    private var DName: String? = null
    private var UserGetStatus: Boolean? = false

    private var SelectDeviceSelectedCount: Int? = 0
    private var SelectDeviceUnSelectedCount: Int? = 0
    private var DeviceSelectedCount: Int? = 0
    private var DeviceUnSelectedCount: Int? = 0
    private var P1HashMap: HashMap<String, Int> = HashMap()
    private val datamessage: String =
        "{\"result\":\"okP1\",\"00\":0,\"56\":0,\"65\":0,\"98\":1,\"72\":1}"

    //    private var datamessage: String? = null
    private var profileDataStatus: String? = "Schnell"
    private val SHOWCASE_ID = "secure_simpli production"
    private var RemovedDeviceindex: String? = ""
    private var entityGWName: String? = null


    private var listener: OnDeviceActionTriggered = object : OnDeviceActionTriggered {
        override fun onDeviceButtonSwitched(device: Device?, arm: Boolean) {
            if (StatusSetup == "profile") {
                if (profileDataStatus == "okP1") {
                } else {
                    device!!.additionalInfo!!.armState = arm
                    DeviceId = gatewayDevice!!.id!!.id!!
                    deviceIdTobeModified = device.id!!.id
//                    ApplyChanges!!.visibility = View.VISIBLE
                    StatusSetup = "profile"
                }
            } else {
                if (arm) {
                    device!!.additionalInfo!!.armState = false
                } else if (!arm) {
                    device!!.additionalInfo!!.armState = true
                }
            }
        }

        override fun onDeviceSelected(device: Device?) {
            if (StatusSetup == "profile") {
                val devicedao =
                    DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO.getDevicebyUid(
                        device!!.name
                    )
                val devices =
                    DatabaseClient.getInstance(context).appDatabase.deviceindex.getSDevice(
                        devicedao.deviceid
                    )
                val atrributedevice =
                    DatabaseClient.getInstance(context!!).appDatabase.geAttributesDAO()
                        .getDevicebyUid(devicedao.deviceid)
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
                    deviceId = devicedao.deviceid,
                    deviceIndex = parseInt(devices.deviceindex),
                    devicename = device.additionalInfo!!.displayName,
                    devicetype = devicedao.devicetype,
                    deviceuid = devicedao.deviceuid
                )
            }
        }

        override fun onDeviceLongPressed(device: Device?) {
            val devicedao =
                DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getDeviceindex(
                    device!!.additionalInfo!!.deviceIndex.toString()
                )
            val devicedata =
                DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getOnlyEntityGroup(
                    devicedao.entitygroupid
                )
            val l = object : AppDialogs.ConfirmListener {
                override fun yes() {
                    RemovedDeviceindex = devicedao.deviceindex
                    val deviceIndex = devicedao!!.deviceindex.toInt()
                    val rpcJson = JSONObject()
                    val params = JSONObject()
                    params.put("devIndex", "$deviceIndex".padStart(2, '0'))
                    params.put("devId", devicedao.deviceuid)
                    params.put("devType", devicedao.devicetype)
                    rpcJson.put("params", params)
                    rpcJson.put("method", "remDevice")
                    rpcJson.put("timeout", 25000)
                    profileDataStatus = "removeDevice"
                    val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                    if (connectionDetails) {
                        ThingsManager.callRPCTwoWay(
                            c = activity!!,
                            l = thisFragment,
                            deviceId = devicedata.deviceid,
                            jsonObject = rpcJson
                        )
//                        AppDialogs.showProgressDialog(
//                            context = activity!!,
//                            desc = "Please wait removing device from gateway"
//                        )
                        DialogTexter!!.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT)
                            .show()
                    }
                    deviceIdTobeModified = device.id!!.id
                }
            }
            AppDialogs.confirmAction(
                c = activity!!,
                text = "Sure!! you want to remove this device?",
                l = l
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            entityGroupId: String,
            position: Int = 0,
            strUser: String,
            displayName: String
        ) =
            GatewayFragment().apply {
                arguments = Bundle().apply {
                    putString("entityGroupId", entityGroupId)
                    putInt("position", position)
                    putString("state", strUser)
                    putString("GwName", displayName)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entityGroupId = it.getString("entityGroupId")
            position = it.getInt("position")
            UserState = it.getString("state")
            entityGWName = it.getString("GwName")
        }

//        mAddDeviceDAO =
//            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
//                .allowMainThreadQueries() //Allows room to do operation on main thread
//                .fallbackToDestructiveMigration()
//                .build()
//                .addDeviceDAO

//        mLatestAttributesDAO =
//            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
//                .allowMainThreadQueries() //Allows room to do operation on main thread
//                .fallbackToDestructiveMigration()
//                .build()
//                .geAttributesDAO()

//        mLatestTelemetryDAO =
//            Room.databaseBuilder(context!!, AppDatabase::class.java, "db-devices")
//                .allowMainThreadQueries() //Allows room to do operation on main thread
//                .fallbackToDestructiveMigration()
//                .build()
//                .telemetryDAO
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.content_device_fragment, container, false)
        try {
            // Set the adapter
            val fab = view.findViewById<FloatingActionButton>(R.id.fab)

            ArmFlow = view.findViewById<FrameLayout>(R.id.armDevices)
            pro1 = view.findViewById<FrameLayout>(R.id.profile1_selector)
            pro2 = view.findViewById<FrameLayout>(R.id.profile2_selector)
            pro3 = view.findViewById<FrameLayout>(R.id.profile3_selector)
            uset = view.findViewById<FrameLayout>(R.id.settings_selector)
            DialogTexter =
                view.findViewById<pl.droidsonroids.gif.GifImageView>(R.id.diaogdisplayText)

            val settings = view.findViewById<ImageView>(R.id.sett)
            val gatewayInfo = view.findViewById<FloatingActionButton>(R.id.gatewayInfo)
            val notifications = view.findViewById<FloatingActionButton>(R.id.notifications)
            val history = view.findViewById<FloatingActionButton>(R.id.history)

//            val factoryreset = view.findViewById<FloatingActionButton>(R.id.factoryreset)
//            presentShowcaseSequence()

            Handler().postDelayed({
                var dss = DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.devices
                val myentity =
                    DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getEntityGroup(
                        entityGroupId
                    )
                if (myentity.isEmpty()) {
//                    AppDialogs.showProgressDialog(
//                        context = activity!!,
//                        desc = "Please wait fetching device details"
//                    )
                    GetDetails(context!!, entityGroupId!!, thisFragment).execute()
                }
            }, 1000)

            fab.setOnClickListener {
                if (gatewayDevice == null) {
                    Snackbar.make(
                        fab,
                        "Please configure gateway before adding devices",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {

                    val indexvalue = getRandomNumber()
                    if (indexvalue != null) {
                        DeviceActivity.openDeviceActivity(
                            c = activity!!,
                            isGateway = false,
                            entityGroupId = entityGroupId!!,
                            gatewayDeviceId = gatewayDevice!!.id!!.id,
                            deviceId = "",
                            deviceIndex = indexvalue
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
                val intent = Intent(activity, HistoryActivity::class.java)
                activity?.startActivity(intent)
            }

            settings.setOnClickListener {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity?.startActivity(intent)
            }

//            factoryreset.setOnClickListener {
//                val l = object : AppDialogs.ConfirmListener {
//                    override fun yes() {
//
//                        var filtervalues = mAddDeviceDAO!!.getEntityGroup(entityGroupId)
//                        if (filtervalues.isNotEmpty()) {
//
//                            for (i in 0 until filtervalues!!.size) {
//                                ThingsManager.deleteDevice(
//                                    c = activity!!,
//                                    l = thisFragment,
//                                    deviceId = filtervalues[i].deviceid
//                                )
//                            }
//
//                            ThingsManager.deleteentityGroup(
//                                c = activity!!,
//                                l = thisFragment,
//                                entityGroupId = entityGroupId!!
//                            )
//                        }
//                    }
//                }
//                AppDialogs.confirmAction(c = activity!!, text = "Do you want to Factory Rest?", l = l)
//            }

            notifications.setOnClickListener {
                val intent = Intent(activity, NotificationActivity::class.java)
                activity?.startActivity(intent)
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
                //                var mState = mCurrentStateDAO!!.cState
//                if (mState.isEmpty()) {
                swipeView!!.isRefreshing = false
                val states = BaseActivity.internetIsAvailable(context!!)
                if (states) {
                    callGetState()
                } else {
                    swipeView!!.isRefreshing = false
                    Toast.makeText(
                        context!!,
                        "Please check your network connectivity",
                        Toast.LENGTH_SHORT
                    ).show()
                }
//                } else {
//                    GetStateTelemetry()
//                }
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
//            ApplyChanges = view.findViewById(R.id.apply_change)
            setProfileListeners(context)
            DialogTexter!!.visibility = View.VISIBLE
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
//        ApplyChanges!!.visibility = View.GONE

//        ApplyChanges?.setOnClickListener {
//            if (StatusId == "1") {
//                applyProfile(
//                    profileButton = it,
//                    profileKey = "1",
//                    profile = gatewayDevice!!.additionalInfo!!.profile1
//                )
//            } else if (StatusId == "2") {
//                applyProfile(
//                    profileButton = it,
//                    profileKey = "2",
//                    profile = gatewayDevice!!.additionalInfo!!.profile2
//                )
//            } else if (StatusId == "3") {
//                applyProfile(
//                    profileButton = it,
//                    profileKey = "3",
//                    profile = gatewayDevice!!.additionalInfo!!.profile3
//                )
//            }
//        }
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

        val counters =
            DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getDeviceindex(
                randomval.toString()
            )

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
        rpcJson.put("timeout", 45000)


//        var usd = DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.devices
//        val myentity =
//            DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getOnlyEntityGroup(
//                entityGroupId
//            )
//        if (myentity != null) {
//
        val connectionDetails = BaseActivity.internetIsAvailable(context!!)
        if (connectionDetails) {

            val myentity =
                DatabaseClient.getInstance(context).appDatabase.addDeviceDAO.getEntityGroup(
                    entityGroupId
                )
            if (myentity.isNotEmpty()) {
                for (i in 0 until myentity.size) {
                    if (myentity[i].deviceindex == "0") {
//                        UserLayer!!.visibility = View.GONE
                        ThingsManager.getdevicecurrentState(
                            context!!,
                            thisFragment,
                            myentity[i].gatewayDeviceId,
                            devicename = "Account"
                        )
//                        callTimerCondition()
                    }
                }
            }
//
//                if (myentity.deviceindex == "0") {
//                    profileDataStatus = "Schnell"

//                    AppDialogs.showProgressDialog(
//                        context = activity!!,
//                        desc = "Please wait fetching state from server"
//                    )

//                    ThingsManager.callRPCTwoWayGetState(
//                        c = context!!,
//                        l = thisFragment,
//                        deviceId = myentity.deviceid,
//                        jsonObject = rpcJson
//                    )

//                    ThingsManager.getdevicecurrentState(
//                        context!!,
//                        thisFragment,
//                        myentity.gatewayDeviceId,
//                        devicename = "Account"
//                    )
//                }
        } else {
            Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT).show()
        }
//        }
    }

    private fun getLatestAttributes() {
        DatabaseClient.getInstance(context!!).appDatabase.geAttributesDAO()!!.DeleteSensor()
        val myentity =
            DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getEntityGroup(
                entityGroupId
            )
        if (myentity.isNotEmpty()) {
            for (i in 0 until myentity.size) {
                val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                if (connectionDetails) {
//                    AppDialogs.showProgressDialog(
//                        context = activity!!,
//                        desc = "Please wait fetching state from server"
//                    )

                    ThingsManager.getDeviceLatestAttributes(
                        c = activity!!,
                        l = thisFragment,
                        deviceId = myentity[i].deviceid,
                        entityType = "DEVICE",
                        Keys = "devicename"
                    )
                    if (myentity.size == i + 1) {
                        getLatestTelemetry()
                    }
                } else {
                    Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getLatestTelemetry() {
        DatabaseClient.getInstance(context!!).appDatabase.telemetryDAO!!.DeleteTelemetry()
        val myentity =
            DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getEntityGroup(
                entityGroupId
            )
        if (myentity.isNotEmpty()) {
            for (i in 0 until myentity.size) {
                val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                if (connectionDetails) {
//                    AppDialogs.showProgressDialog(
//                        context = activity!!,
//                        desc = "Please wait fetching state from server"
//                    )
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
                } else {
                    Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

//    private fun elevateProfile(view: View) {
//        armDevices!!.alpha = 1f
//        profile1!!.alpha = 1f
//        profile2!!.alpha = 1f
//        profile3!!.alpha = 1f
//        disArmDevices!!.alpha = 1f
//
//        view.alpha = .5f
//    }

    private fun setProfileListeners(context: Context?) {
        try {
            armDevices!!.setOnClickListener { it ->
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

                            AppPreference.put(context!!.applicationContext, "State", "Arm")
//                            ApplyChanges!!.visibility = View.GONE
                            val connectionDetails = BaseActivity.internetIsAvailable(context)
                            if (connectionDetails) {

                                DialogTexter!!.visibility = View.VISIBLE
                                profileDataStatus = "Schnell"

                                // Call gateway to arm all device
                                ThingsManager.callRPCTwoWay(
                                    c = activity!!,
                                    l = thisFragment,
                                    deviceId = gatewayDevice!!.id!!.id!!,
                                    jsonObject = rpcJson
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(R.string.nonetwor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            for (device in devices) {
                                device.additionalInfo!!.armState = false
                            }
//                            list?.adapter?.notifyDataSetChanged()

                        } else {
                            Snackbar.make(
                                view!!,
                                "Please add at least one device",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                AppDialogs.confirmAction(
                    c = activity!!,
                    text = "Do you want to Arm all this device?",
                    l = l
                )
            }
            disArmDevices!!.setOnClickListener { it ->
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
//                            ApplyChanges!!.visibility = View.GONE
                            AppPreference.put(context!!.applicationContext, "State", "DisArm")
                            val connectionDetails = BaseActivity.internetIsAvailable(context)
                            if (connectionDetails) {
                                DialogTexter!!.visibility = View.VISIBLE
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
                                Toast.makeText(
                                    context,
                                    getString(R.string.nonetwor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Snackbar.make(
                                view!!,
                                "Please add at least one device",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                AppDialogs.confirmAction(
                    c = activity!!,
                    text = "Do you want to DisArm all this device?",
                    l = l
                )
            }

            profile1!!.setOnClickListener { it ->
                val profile1details = AppPreference[context!!, "FirstProfile", ""]
                if (!profile1details.isNullOrEmpty()) {

                    StatusSetup = "profile"
                    StatusId = "1"
                    profileDataStatus = "Schnell"
                    profile1Selector!!.setBackgroundResource(R.color.litegreen)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)

                    val obj = JSONObject(profile1details)
                    if (gatewayDevice != null) {
                        DialogTexter!!.visibility = View.VISIBLE
                        ThingsManager.callRPCTwoWay(
                            c = activity!!,
                            l = thisFragment,
                            deviceId = gatewayDevice!!.id!!.id!!,
                            jsonObject = obj
                        )
                        AppPreference.put(context, "CurrentState", "okP1")
                        list?.adapter?.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Data Error", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {

                    StatusSetup = "profile"
                    StatusId = "1"
                    profileDataStatus = "okP1"
                    profile1Selector!!.setBackgroundResource(R.color.litegreen)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)
//                    ApplyChanges!!.visibility = View.GONE
                    DialogTexter!!.visibility = View.VISIBLE
                    AppPreference.put(context, "Apply", "Profile")
                    AppPreference.put(context, "CurrentState", "okP1")
                    loadDevices()
                }
            }

            profile2!!.setOnClickListener { it ->

//                StatusSetup = "profile"
//                StatusId = "2"
//                profileDataStatus = "Schnell"
//                profile2Selector!!.setBackgroundResource(R.color.litegreen)
//                profile1Selector!!.setBackgroundResource(R.color.whiter)
//                profile3Selector!!.setBackgroundResource(R.color.whiter)
//                disArmDevices!!.setBackgroundResource(R.color.whiter)
//                armDevices!!.setBackgroundResource(R.color.whiter)
//                AppPreference.put(context!!.applicationContext, "State", "Home")
////                elevateProfile(it)
//                ApplyChanges!!.visibility = View.GONE
//                loadDevices()


                val profile2details = AppPreference[context!!, "SecondProfile", ""]
                if (!profile2details.isNullOrEmpty()) {
                    StatusSetup = "profile"
                    StatusId = "2"
                    profileDataStatus = "Schnell"
//                            profile2Selector!!.setBackgroundResource(R.color.litegreen)
//                            profile1Selector!!.setBackgroundResource(R.color.whiter)
//                            profile3Selector!!.setBackgroundResource(R.color.whiter)
//                            disArmDevices!!.setBackgroundResource(R.color.whiter)
//                            armDevices!!.setBackgroundResource(R.color.whiter)

                    profile2Selector!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)
//
//                            AppNewDialogs.showProgressDialog(
//                                context = activity!!,
//                                desc = "Applying Custom Profile on your Gateway..."
//                            )
                    val obj = JSONObject(profile2details)
                    if (obj.toString().contains("p2")) {
                        if (gatewayDevice != null) {
                            DialogTexter!!.visibility = View.VISIBLE
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = obj
                            )
                            list?.adapter?.notifyDataSetChanged()
                            AppPreference.put(context, "CurrentState", "okP2")
                        } else {
                            AppPreference.put(context, "CurrentState", "okP2")
//                                        Toast.makeText(context, "Data Error", Toast.LENGTH_SHORT)
//                                            .show()
                            loadDevices()
                        }
                    } else {
                        AppPreference.put(context, "CurrentState", "okP2")
//                                    Toast.makeText(context, "Data Error", Toast.LENGTH_SHORT)
//                                        .show()
                        loadDevices()
                    }
                } else {
                    StatusSetup = "profile"
                    StatusId = "2"
                    profileDataStatus = "okP2"

                    profile2Selector!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)
                    DialogTexter!!.visibility = View.VISIBLE

//                                profile2Selector!!.setBackgroundResource(R.color.litegreen)
//                                profile1Selector!!.setBackgroundResource(R.color.whiter)
//                                profile3Selector!!.setBackgroundResource(R.color.whiter)
//                                disArmDevices!!.setBackgroundResource(R.color.whiter)
//                                armDevices!!.setBackgroundResource(R.color.whiter)
//                    ApplyChanges!!.visibility = View.GONE

//                                elevateProfile(it)
                    AppPreference.put(context, "CurrentState", "okP2")
                    loadDevices()

                }

            }

            profile3!!.setOnClickListener { it ->

//                StatusSetup = "profile"
//                StatusId = "3"
//                profileDataStatus = "Schnell"
//                profile3Selector!!.setBackgroundResource(R.color.litegreen)
//                profile1Selector!!.setBackgroundResource(R.color.whiter)
//                profile2Selector!!.setBackgroundResource(R.color.whiter)
//                disArmDevices!!.setBackgroundResource(R.color.whiter)
//                armDevices!!.setBackgroundResource(R.color.whiter)
//                AppPreference.put(context!!.applicationContext, "State", "Home")
////                elevateProfile(it)
//                ApplyChanges!!.visibility = View.GONE
//                loadDevices()

                val profile3details = AppPreference[context!!, "ThirdProfile", ""]
                if (!profile3details.isNullOrEmpty()) {

                    StatusSetup = "profile"
                    StatusId = "3"
                    profileDataStatus = "Schnell"

                    profile3Selector!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)

//                            AppNewDialogs.showProgressDialog(
//                                context = activity!!,
//                                desc = "Applying Custom Profile on your Gateway..."
//                            )

                    val obj = JSONObject(profile3details)
                    if (obj.toString().contains("p3")) {
                        if (gatewayDevice != null) {
                            DialogTexter!!.visibility = View.VISIBLE
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = obj
                            )
                            list?.adapter?.notifyDataSetChanged()
                            AppPreference.put(context, "CurrentState", "okP3")
                        } else {
//                                        Toast.makeText(context, "Data Error", Toast.LENGTH_SHORT)
//                                            .show()
                            loadDevices()
                        }
                    } else {
//                                    Toast.makeText(context, "Data Error", Toast.LENGTH_SHORT)
//                                        .show()
                        loadDevices()
                    }
                } else {

                    StatusSetup = "profile"
                    StatusId = "3"
                    profileDataStatus = "okP3"
                    DialogTexter!!.visibility = View.VISIBLE
                    profile3Selector!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    disArmDevices!!.setBackgroundResource(R.color.whiter)
                    armDevices!!.setBackgroundResource(R.color.whiter)

//                    ApplyChanges!!.visibility = View.GONE
//                                elevateProfile(it)
                    AppPreference.put(context, "CurrentState", "okP3")
                    loadDevices()
                }
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun applyProfile(profileButton: View, profileKey: String, profile: String?) {
        val connectionDetails = BaseActivity.internetIsAvailable(context!!)
        if (connectionDetails) {
            try {
                if (devices.size > 0) {
                    val rpcJson = if (profile == null || profile.isEmpty() || profile != null) {
                        val params = JSONObject()
                        for (device in devices) {
                            val selectedDeviceindex = DatabaseClient.getInstance(context)
                                .appDatabase.addDeviceDAO.getDeviceid(device.id!!.id!!)
                            params.put(
                                "${selectedDeviceindex.deviceindex}".padStart(2, '0'),
                                if (device.additionalInfo!!.armState != null && device.additionalInfo!!.armState!!) 1 else 0
                            )
                        }
                        val jsonObject = JSONObject()
                        jsonObject.put("params", params)
                        jsonObject.put("method", "p$profileKey")
                        jsonObject.put("timeout", 40000)
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

//                    AppNewDialogs.showProgressDialog(
//                        context = activity!!,
//                        desc = "Applying Profile $profileKey on your Gateway..."
//                    )

                    if (profileKey.equals("1")) {
                        if (rpcJson.toString().contains("p1")) {
                            AppPreference.put(context!!, "FirstProfile", rpcJson.toString())
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = rpcJson
                            )
                        }
                    } else if (profileKey.equals("2")) {
                        if (rpcJson.toString().contains("p2")) {
                            AppPreference.put(context!!, "SecondProfile", rpcJson.toString())
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = rpcJson
                            )
                        }
                    } else if (profileKey.equals("3")) {
                        if (rpcJson.toString().contains("p3")) {
                            AppPreference.put(context!!, "ThirdProfile", rpcJson.toString())
                            ThingsManager.callRPCTwoWay(
                                c = activity!!,
                                l = thisFragment,
                                deviceId = gatewayDevice!!.id!!.id!!,
                                jsonObject = rpcJson
                            )
                        }
                    } else {
                        ThingsManager.callRPCTwoWay(
                            c = activity!!,
                            l = thisFragment,
                            deviceId = gatewayDevice!!.id!!.id!!,
                            jsonObject = rpcJson
                        )
                    }
                } else {
                    Snackbar.make(
                        view!!,
                        "Please check your profile before apply",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context!!, "No Network Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDevices() {
        try {
            if (entityGroupId != null) {
                var deviceList =
                    DatabaseClient.getInstance(context).appDatabase.addDeviceDAO!!.devices
                if (!deviceList.isEmpty()) {
                    DeviceSelectedCount = deviceList.size
                    SelectDeviceSelectedCount = deviceList.size
//                    StatusSetup = ""
                    for (i in 0 until deviceList.size) {
                        devices.clear()
                        devicesMap.clear()
                        list!!.adapter!!.notifyDataSetChanged()

                        ThingsManager.getDevice(
                            c = context!!,
                            l = this,
                            deviceId = deviceList.get(i).deviceid
                        )
                    }
                } else {
                    devices.clear()
                    devicesMap.clear()
                    list!!.adapter!!.notifyDataSetChanged()
                    SelectDeviceSelectedCount = 2
                    DatabaseClient.getInstance(context)
                        .appDatabase.allDevices!!.Deletedevices()

                    var mLoginUser = AppPreference.get(context!!, "UserNumber", "")
                    if (!mLoginUser.isNullOrEmpty()) {
                        ThingsManager.getDevices(
                            c = activity!!,
                            l = this,
                            entityGroupId = entityGroupId!!
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private fun loadDevices() {
//        if (entityGroupId != null) {
//            devices.clear()
//            devicesMap.clear()
//            list!!.adapter!!.notifyDataSetChanged()
//
//            val connectionDetails = BaseActivity.internetIsAvailable(context!!)
//            if (connectionDetails) {
//
//                DialogTexter!!.visibility = View.VISIBLE
////                AppDialogs.showProgressDialog(
////                    context = activity!!,
////                    desc = "Please wait fetching state from server"
////                )
//                ThingsManager.getDevices(c = activity!!, l = this, entityGroupId = entityGroupId!!)
//            } else {
//                Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun GetDeviceIndexVal(deviceid: String) {
//        AppDialogs.showProgressDialog(
//            context = activity!!,
//            desc = "Fetching Device index value ..."
//        )
//        ThingsManager.getdeviceindexval(context!!, thisFragment, deviceid);
//    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        activity!!.menuInflater.inflate(R.menu.add_device_option, menu)
    }

    interface OnDeviceActionTriggered {
        fun onDeviceSelected(device: Device?)
        fun onDeviceLongPressed(device: Device?)
        fun onDeviceButtonSwitched(device: Device?, arm: Boolean = true)
    }

    fun triggerSOSAction() {
        Snackbar.make(view!!, "Replace with Trigger SOS SMS action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

//    private fun callDisArmLoader() {
//        if (context != null) {
//            try {
////                disArmDevices!!.visibility = View.VISIBLE
////                armDevices!!.visibility = View.GONE
//                DisArm_Icon!!.setBackgroundResource(R.drawable.ic_disarmed_red)
//                Arm_Icon!!.setBackgroundResource(R.drawable.ic_armed_grey)
//                Profile_One!!.setBackgroundResource(R.drawable.ic_home_grey)
//                Profile_Two!!.setBackgroundResource(R.drawable.ic_sleep_grey)
//                Profile_Three!!.setBackgroundResource(R.drawable.ic_away_grey)
////        disArmDevices!!.setBackgroundResource(R.color.litegreen)
////        profileloader()
//                profileDataStatus = "DisArm"
//                AppPreference.put(context!!, "CurrentState", "DISARM")
//                AppPreference.put(
//                    activity!!,
//                    "NotificationState",
//                    "DISARM"
//                )
//                StatusSetup = "DisArm"
//                loadDevices()
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

//    private fun callHomeProfileLoader() {
////        profileloader()
////        profile1Selector!!.setBackgroundResource(R.color.litegreen)
////        armDevices!!.setBackgroundResource(R.color.whiter)
//        try {
//            Arm_Icon!!.setBackgroundResource(R.drawable.ic_armed_grey)
//            DisArm_Icon!!.setBackgroundResource(R.drawable.ic_disarmed_grey)
//            Profile_One!!.setBackgroundResource(R.drawable.ic_home_green_24dp)
//            Profile_Two!!.setBackgroundResource(R.drawable.ic_sleep_grey)
//            Profile_Three!!.setBackgroundResource(R.drawable.ic_away_grey)
//            profileDataStatus = "okP1"
//            AppPreference.put(context!!, "CurrentState", "okP1")
//            AppPreference.put(
//                activity!!,
//                "NotificationState",
//                "okP1"
//            )
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//        loadDevices()
//    }
//
//
//    private fun callArmLoader() {
//        if (context != null) {
//            try {
////                armDevices!!.visibility = View.VISIBLE
////                disArmDevices!!.visibility = View.GONE
//                Arm_Icon!!.setBackgroundResource(R.drawable.ic_armed_green)
//                DisArm_Icon!!.setBackgroundResource(R.drawable.ic_disarmed_grey)
//                Profile_One!!.setBackgroundResource(R.drawable.ic_home_grey)
//                Profile_Two!!.setBackgroundResource(R.drawable.ic_sleep_grey)
//                Profile_Three!!.setBackgroundResource(R.drawable.ic_away_grey)
//
////        armDevices!!.setBackgroundResource(R.color.litegreen)
////        profileloader()
//                profileDataStatus = "Arm"
//
//                AppPreference.put(
//                    activity!!,
//                    "CurrentState",
//                    "ARMED"
//                )
//
//                AppPreference.put(
//                    activity!!,
//                    "NotificationState",
//                    "ARMED"
//                )
//                StatusSetup = "Arm"
//                loadDevices()
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//        }
//
//    }

    override fun onResponse(r: Response?) {
        if (r == null) {
            return
        }
        try {
//            AppDialogs.hideProgressDialog()
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
                AppPreference.clearAll(c = activity!!)
                Snackbar.make(view!!, "Session expired. Please login again", Snackbar.LENGTH_LONG)
                    .show()
                Handler().postDelayed({
                    val i = context!!.packageManager
                        .getLaunchIntentForPackage(context!!.packageName)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    activity!!.finish()
                }, 2000)

            } else if(r.message == "Requested item wasn't found!"){
                Toast.makeText(
                    context!!,
                   "No Device Found",
                    Toast.LENGTH_SHORT
                ).show()
                DialogTexter!!.visibility = View.GONE
            }else if (r.statusMessage == "Failure") {
                DialogTexter!!.visibility = View.INVISIBLE
                if (StatusSetup == "ArmAll") {
                    StatusId = "1"
//                    ApplyChanges!!.visibility = View.VISIBLE

                    AppPreference.put(context!!.applicationContext, "State", "Arm")
//                    loadDevices()
                    disArmDevices!!.setBackgroundResource(R.color.litegreen)
                    armDevices!!.visibility = View.GONE
                    disArmDevices!!.visibility = View.VISIBLE
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    UserSettingsSelector!!.setBackgroundResource(R.color.whiter)
                    !swipeView!!.isFocusableInTouchMode
//                    ApplyChanges!!.visibility = View.GONE

                } else if (StatusSetup == "DisArmAll") {
                    StatusId = "1"
//                    ApplyChanges!!.visibility = View.VISIBLE

                    AppPreference.put(context!!.applicationContext, "State", "DisArm")
//                    loadDevices()
                    armDevices!!.visibility = View.VISIBLE
                    disArmDevices!!.visibility = View.VISIBLE
                    armDevices!!.setBackgroundResource(R.color.litegreen)
                    profile1Selector!!.setBackgroundResource(R.color.whiter)
                    profile2Selector!!.setBackgroundResource(R.color.whiter)
                    profile3Selector!!.setBackgroundResource(R.color.whiter)
                    UserSettingsSelector!!.setBackgroundResource(R.color.whiter)
//                    ApplyChanges!!.visibility = View.GONE

                } else if (StatusSetup == "Profile") {
//                    loadDevices()
                } else if (StatusSetup == "GetState") {
                        DialogTexter!!.visibility = View.VISIBLE
                        loadDevices()
                }
//                Snackbar.make(
//                    view!!,
//                    "Unable to connect to Gateway, Please try again Later.",
//                    Snackbar.LENGTH_LONG
//                )
//                    .show()
//                loadDevices()
            } else when (r.requestType) {
                ThingsManager.API.entitiesUnderGroup.hashCode() -> {
                    swipeView!!.isRefreshing = false
                    if (r is Device) {
                        devices.clear()
                        devicesMap.clear()
                        list!!.adapter!!.notifyDataSetChanged()
                        DeviceSelectedCount = r.deviceList!!.size
                        SelectDeviceSelectedCount = r.deviceList!!.size
                        for (device in r.deviceList!!) {
                            GetState = true
                            val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                            if (connectionDetails) {
                                ThingsManager.getDevice(
                                    c = activity!!,
                                    l = this,
                                    deviceId = device.id!!.id!!
                                )
                            } else {
                                Toast.makeText(
                                    context!!,
                                    getString(R.string.nonetwor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        DialogTexter!!.visibility = View.INVISIBLE
                        if (noDataView != null) {
                            if (r.deviceList!!.size <= 1) {
                                noDataView!!.visibility = View.VISIBLE
//                                list!!.visibility = View.GONE
                            } else {
                                noDataView!!.visibility = View.GONE
                                list!!.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        if (view != null)
                            Snackbar.make(view!!, "Could not load devices", Snackbar.LENGTH_LONG)
                                .show()
                    }
                }

                ThingsManager.API.getDeviceAttribute.hashCode() -> {
                    if (r is LatestTelemetryData) {
                        if (!r.telemetrylist.isNullOrEmpty()) {
                            for (i in 0 until r.telemetrylist!!.size) {
                                AttributeData
                                r.telemetrylist!![i].telkey

                                val splitters = r.telemetrylist!![i].telvalue!!.split("/*/")
                                val attdevice =
                                    DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getDevicebyUid(
                                        splitters[1]
                                    )

                                val latestAttribute = LatestAttribute()
                                latestAttribute.deviceid = attdevice.deviceid
                                latestAttribute.devicename = splitters[0]

                                try {
                                    DatabaseClient.getInstance(context!!).appDatabase.geAttributesDAO()!!
                                        .insert(latestAttribute)
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
                                    DatabaseClient.getInstance(context!!).appDatabase.telemetryDAO!!.insert(
                                        teledata
                                    )
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
                                DatabaseClient.getInstance(context)
                                    .appDatabase.addDeviceDAO.DeleteSensor(r.devid!!)
                                //DELETE DEVICE IN LOCAL DATABASE
//                                val resultststae = mAddDeviceDAO!!.DeleteSensor(r.devid!!)
                                loadDevices()
                            } else {
                                Snackbar.make(
                                    view!!,
                                    "Could not delete device. Please try again",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            loadDevices()
                        } else if (r is Device) {
                            if (r.requestMethod == Request.Method.GET) {
                                if (!devicesMap.keys.contains(r.id!!.id)) {
                                    if (r.additionalInfo != null && r.additionalInfo!!.gateway == true) {
                                        gatewayDevice = r
//                                    TODO selected profile should be highlighted
                                    } else {
                                        var deviceDetails =
                                            DatabaseClient.getInstance(context)
                                                .appDatabase.deviceindex.getSDevice(r.id!!.id!!)
                                        if (deviceDetails != null) {
                                            var selectedDeviceindex =
                                                DatabaseClient.getInstance(context)
                                                    .appDatabase.addDeviceDAO.getDeviceid(r.id!!.id!!)

                                            if (selectedDeviceindex.deviceindex.isNotEmpty()) {

                                                if (!deviceDetails.deviceid.isEmpty()) {
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.deviceindex.updatedeviceindex(
                                                            selectedDeviceindex.deviceindex,
                                                            r.id!!.id!!
                                                        )
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.deviceindex.updatedevicetype(
                                                            selectedDeviceindex.deviceindex,
                                                            r.type,
                                                            r.id!!.id!!
                                                        )

                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.allDevices!!.updatedeviceindex(
                                                            selectedDeviceindex.deviceindex,
                                                            r.id!!.id!!
                                                        )
                                                }
                                            } else {
                                                if (!deviceDetails.deviceid.isEmpty()) {
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.deviceindex.updatedeviceindex(
                                                            r.additionalInfo!!.deviceIndex.toString(),
                                                            r.id!!.id!!
                                                        )

                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.allDevices!!.updatedeviceindex(
                                                            r.additionalInfo!!.deviceIndex.toString(),
                                                            r.id!!.id!!
                                                        )
                                                }
                                            }
                                        }

                                        var dsdetails = DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.getDeviceid(r.id!!.id!!)
                                        if (dsdetails != null) {
                                            if (!dsdetails.deviceid.isEmpty()) {
                                                DatabaseClient.getInstance(context)
                                                    .appDatabase.addDeviceDAO.updateall(
                                                        r.additionalInfo!!.displayName,
                                                        dsdetails.deviceindex,
                                                        r.id!!.id!!
                                                    )
                                            }
                                        }


                                        if (!r.devIndex.equals("0")) {
                                            if (profileDataStatus == "Arm") {
                                                r.additionalInfo!!.armState = true
                                                StatusSetup = "Arm"
//                                                try {
//                                                    AppPreference.put(
//                                                        context!!,
//                                                        "CurrentState",
//                                                        "ARMED"
//                                                    )
//                                                } catch (e: java.lang.Exception) {
//                                                    e.printStackTrace()
//                                                }
                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }

                                            } else if (profileDataStatus == "DisArm") {
                                                r.additionalInfo!!.armState = false
                                                StatusSetup = "DisArm"
//                                                try {
//                                                    AppPreference.put(
//                                                        mContext!!,
//                                                        "CurrentState",
//                                                        "DISARM"
//                                                    )
//                                                } catch (e: java.lang.Exception) {
//                                                    e.printStackTrace()
//                                                }
                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }

                                            } else if (profileDataStatus == "okP1") {
                                                profileDataStatus = "okP1"
                                                AppPreference.put(context!!, "CurrentState", "okP1")
                                                val devicedao =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.addDeviceDAO.getEntityGroup(
                                                            entityGroupId
                                                        )
//                                                val stateProfile =
//                                                    mProfileState!!.getstatebyGw(entityGroupId)

//                                                if (!stateProfile.p1state.isNullOrEmpty()) {
//
//                                                    val reports = (stateProfile.p1state)
//                                                    val jresponse = JSONObject(reports.toString())
//
//                                                    for (i in 0 until devicedao.size) {
//                                                        if (devicedao[i].deviceindex != "") {
//                                                            if (r.additionalInfo!!.deviceIndex!!.equals(
//                                                                    devicedao[i].deviceindex.toInt()
//                                                                )
//                                                            ) {
//                                                                if (jresponse.toString()
//                                                                        .contains(devicedao[i].deviceindex)
//                                                                ) {
//                                                                    r.additionalInfo!!.armState =
//                                                                        !r.type.equals("ms")
//                                                                    r.additionalInfo!!.armState =
//                                                                        jresponse.get(devicedao[i].deviceindex)
//                                                                            .toString() != "0"
//                                                                } else {
//                                                                    r.additionalInfo!!.armState =
//                                                                        false
//                                                                    r.additionalInfo!!.armState =
//                                                                        !r.type.equals("ms")
//                                                                    r.additionalInfo!!.armState =
//                                                                        r.type.equals("ds")
//                                                                }
//                                                            }
//                                                        }
//                                                    }

//                                                    SelectDeviceUnSelectedCount =
//                                                        SelectDeviceUnSelectedCount!! + 1
////
//                                                    if (SelectDeviceUnSelectedCount == SelectDeviceSelectedCount!!.minus(1)) {
//
//                                                    }
//
//                                                        AppNewDialogs.showProgressDialog(
//                                                            context = activity!!,
//                                                            desc = "Applying Home Profile 1..."
//                                                        )
//                                                        StatusSetup = "profile"
//                                                        StatusId = "1"
//                                                        profileDataStatus = "Schnell"
//
//                                                        val obj = JSONObject(stateProfile.p1state)
//                                                        obj.remove("result")
//
//                                                        var rpcString =
//                                                            "{\"params\":" + obj.toString() + ",\"method\":\"p1\",\"timeout\":40000}"
//                                                        val resultobj = JSONObject(rpcString)
//
//                                                        ThingsManager.callRPCTwoWay(
//                                                            c = activity!!,
//                                                            l = thisFragment,
//                                                            deviceId = gatewayDevice!!.id!!.id!!,
//                                                            jsonObject = resultobj,
//                                                            devicename = "Account"
//                                                        )
//                                                        AppPreference.put(
//                                                            context!!,
//                                                            "Apply",
//                                                            ""
//                                                        )
//                                                        SelectDeviceUnSelectedCount = 0
//                                                    }


//                                                } else {
                                                for (i in 0 until devicedao.size) {
                                                    if (devicedao[i].deviceindex != "") {
                                                        r.additionalInfo!!.armState = false
                                                        r.additionalInfo!!.armState =
                                                            !r.type.equals("ms")
                                                        r.additionalInfo!!.armState =
                                                            r.type.equals("ds")

                                                        if (devicedao[i].devicetype.equals("ds") || devicedao[i].devicetype.equals(
                                                                "rm"
                                                            )
                                                        ) {
                                                            P1HashMap.put(
                                                                devicedao[i].deviceindex,
                                                                1
                                                            )
                                                        } else if (devicedao[i].devicetype.equals(
                                                                "rm"
                                                            )
                                                        ) {
                                                            P1HashMap.put(
                                                                devicedao[i].deviceindex,
                                                                0
                                                            )
                                                        } else {
                                                            P1HashMap.put(
                                                                devicedao[i].deviceindex,
                                                                0
                                                            )
                                                        }
                                                    }
                                                }
                                                DeviceUnSelectedCount =
                                                    DeviceUnSelectedCount!! + 1

                                                if (DeviceUnSelectedCount == DeviceSelectedCount!!.minus(
                                                        1
                                                    )
                                                ) {

                                                    var rdevices =
                                                        AppPreference.get(
                                                            context!!,
                                                            "Apply",
                                                            ""
                                                        )
                                                    if (rdevices == "Profile") {

                                                        if (profileDataStatus == "okP1") {
                                                            val jObject = JSONObject(P1HashMap)

//                                                            AppNewDialogs.showProgressDialog(
//                                                                context = activity!!,
//                                                                desc = "Applying Home Profile 1..."
//                                                            )
//                                                            updatetext!!.visibility = View.VISIBLE
                                                            var rpcString =
                                                                "{\"params\":$jObject,\"method\":\"p1\",\"timeout\":40000}"
                                                            val obj = JSONObject(rpcString)

                                                            AppPreference.put(
                                                                context!!,
                                                                "FirstProfile",
                                                                obj.toString()
                                                            )

                                                            StatusSetup = "profile"
                                                            StatusId = "1"
                                                            profileDataStatus = "Schnell"

                                                            ThingsManager.callRPCTwoWay(
                                                                c = activity!!,
                                                                l = thisFragment,
                                                                deviceId = gatewayDevice!!.id!!.id!!,
                                                                jsonObject = obj
                                                            )
//                                                            callTimerCondition()
                                                            AppPreference.put(
                                                                context!!,
                                                                "Apply",
                                                                ""
                                                            )
                                                            DeviceUnSelectedCount = 0
                                                        }
                                                    }
                                                }

                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }

                                                StatusSetup = "profile"
                                                StatusId = "1"


                                            } else if (profileDataStatus == "okP2") {
                                                AppPreference.put(
                                                    context!!,
                                                    "CurrentState",
                                                    "okP2"
                                                )
                                                val devicedao =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.addDeviceDAO.getEntityGroupname(
                                                            entityGroupId, r.name.toString()
                                                        )
                                                val stateProfile =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase!!.profileState.getstatebyGw(
                                                            entityGroupId
                                                        )
                                                if (stateProfile != null) {
                                                    if (!stateProfile.p2state.isNullOrEmpty()) {
                                                        val reports = stateProfile.p2state
                                                        val jresponse =
                                                            JSONObject(reports.toString())

                                                        for (i in 0 until devicedao.size) {
                                                            if (devicedao[i].deviceindex != "") {
                                                                if (devicedao[i].devicetype != "gw") {
//                                                                if (r.additionalInfo!!.deviceIndex!!.equals(
//                                                                        devicedao[i].deviceindex.toInt()
//                                                                    ))
//                                                                {
                                                                    if (jresponse.toString()
                                                                            .replace("\"", "")
                                                                            .toString()
                                                                            .contains(devicedao[i].deviceindex + ":1".toString())
                                                                    ) {
                                                                        if (jresponse.toString()
                                                                                .contains(devicedao[i].deviceindex)
                                                                        ) {
                                                                            r.additionalInfo!!.armState =
                                                                                jresponse.getString(
                                                                                    devicedao[i].deviceindex
                                                                                ) != "0"
                                                                        } else {
                                                                            r.additionalInfo!!.armState =
                                                                                false
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }
                                                StatusSetup = "profile"
                                                StatusId = "2"

                                            } else if (profileDataStatus == "okP3") {
                                                AppPreference.put(
                                                    context!!,
                                                    "CurrentState",
                                                    "okP3"
                                                )
                                                val devicedao =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.addDeviceDAO.getEntityGroupname(
                                                            entityGroupId, r.name.toString()
                                                        )
                                                val stateProfile =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase!!.profileState.getstatebyGw(
                                                            entityGroupId
                                                        )
                                                if (stateProfile != null) {
                                                    if (!stateProfile.p3state.isNullOrEmpty()) {
                                                        val reports = stateProfile.p3state
                                                        val jresponse =
                                                            JSONObject(reports.toString())

                                                        for (i in 0 until devicedao.size) {
                                                            if (devicedao[i].deviceindex != "") {
//                                                                if (r.additionalInfo!!.deviceIndex!!.equals(
//                                                                        devicedao[i].deviceindex.toInt()
//                                                                    )
//                                                                ) {
                                                                if (devicedao[i].devicetype != "gw") {
                                                                    if (jresponse.toString()
                                                                            .replace("\"", "")
                                                                            .toString()
                                                                            .contains(devicedao[i].deviceindex + ":1".toString())
                                                                    ) {
                                                                        r.additionalInfo!!.armState =
                                                                            jresponse.getString(
                                                                                devicedao[i].deviceindex
                                                                            ) != "0"
                                                                    } else {
                                                                        r.additionalInfo!!.armState =
                                                                            false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }
                                                StatusSetup = "profile"
                                                StatusId = "3"

                                            } else if (profileDataStatus == "Schnell") {
                                                val attributedatas =
                                                    DatabaseClient.getInstance(context)
                                                        .appDatabase.geAttributesDAO()
                                                        .getDevicebyUid(r.id!!.id!!)
                                                if (attributedatas != null) {
                                                    if (r.id!!.id!!.equals(attributedatas.deviceid)) {
                                                        r.additionalInfo!!.displayName =
                                                            attributedatas.devicename
                                                    }
                                                }
                                            }
                                            devices.add(r)
                                            devicesMap[r.id!!.id!!] = r

                                            var despd: Boolean = true
                                            SelectDeviceUnSelectedCount =
                                                SelectDeviceUnSelectedCount!! + 1
                                            if (SelectDeviceUnSelectedCount == SelectDeviceSelectedCount!!.minus(
                                                    1
                                                )
                                            ) {
                                                despd = false
                                                SelectDeviceUnSelectedCount = 0
                                                list!!.visibility = View.VISIBLE
                                                DialogTexter!!.visibility = View.GONE
                                            }
                                            list!!.adapter!!.notifyDataSetChanged()
                                            if (despd) {
                                                list!!.visibility = View.VISIBLE
                                                DialogTexter!!.visibility = View.GONE
                                            }
                                        }

                                    }
                                    DialogTexter!!.visibility = View.GONE
                                }
                            } else {
                                if (r.additionalInfo!!.gateway == true) {
                                    gatewayDevice = r
                                } else {
                                    devicesMap[r.id!!.id]!!.additionalInfo = r.additionalInfo
                                    list!!.adapter!!.notifyDataSetChanged()
                                }
                                if (r.extraOutput != null) {
                                    Snackbar.make(view!!, r.extraOutput!!, Snackbar.LENGTH_LONG)
                                        .show()
                                } else {
                                    Snackbar.make(
                                        view!!,
                                        "Updated device ${r.getDisplayName()}",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .show()
//                                    loadDevices()
                                }
                            }
                        }
                    } else {
                        if (GetState == false) {
                            if (r is ThingsBoardResponse) {
                                r.additionalInfo!!.deviceIndex!!

                                val devicedetails =
                                    DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.getDeviceid(
                                        r.id!!.id!!
                                    )
                                val addDevice = AddDevice()
                                addDevice.devicename = r.additionalInfo!!.displayName
                                addDevice.deviceuid = devicedetails.deviceuid
                                addDevice.devicetype = devicedetails.devicetype
                                addDevice.deviceid = r.id!!.id!!
                                addDevice.deviceindex = r.additionalInfo!!.deviceIndex!!.toString()
                                addDevice.entitygroupid = devicedetails.entitygroupid

                                try {
                                    DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.updateall(
                                        r.additionalInfo!!.displayName,
                                        r.additionalInfo!!.deviceIndex!!.toString(),
                                        r.id!!.id!!
                                    )
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }
                            }
                            callGetState()
                        }
                    }
                }
                ThingsManager.API.rpcTwoWaygetStatus.hashCode() -> {
                    if (profileDataStatus == "Schnell") {
                        DialogTexter!!.visibility = View.GONE
                        if (r.result == "ok" || r.result == "okP1" || r.result == "okP2" || r.result == "okP3") {
                            UserGetStatus = true
                            if (r.profilestate == "arm") {
//                                if (CallState == "Getstate") {
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.litegreen)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "Arm"
                                AppPreference.put(context!!.applicationContext, "State", "Arm")
                                loadDevices()
                            } else if (r.profilestate == "disarm") {
//                                callDisArmLoader()
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.setBackgroundResource(R.color.litegreen)
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "DisArm"
                                AppPreference.put(context!!.applicationContext, "State", "DisArm")
                                loadDevices()
                            } else if (r.result == "okP1") {

                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)
                                var pstate = ProfileState()
                                pstate.gateway = entityGroupId!!

                                if (listestate != null) {
                                    pstate.p2state = listestate.p2state
                                    pstate.p3state = listestate.p3state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            r.response.toString(),
                                            listestate.p2state,
                                            listestate.p3state,
                                            entityGroupId
                                        )
                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = r.response.toString()
                                    pstate.p2state = ""
                                    pstate.p3state = ""
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }


                                armDevices!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.litegreen)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP1"
                                AppPreference.put(context!!, "CurrentState", "okP1")
                                AppPreference.put(
                                    activity!!,
                                    "NotificationState",
                                    "okP1"
                                )

                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP1")
                                var profileData = ProfileSet()
                                profileData.profile = "okP1"
                                profileData.response = r.extraOutput
                                StatusId = "1"

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

                            } else if (r.profilestate == "p1") {
                                AppPreference.put(context!!, "CurrentState", "okP1")
                                loadDevices()

                            } else if (r.result == "okP2") {

                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)
                                var pstate = ProfileState()
                                pstate.gateway = entityGroupId!!

                                if (listestate != null) {
                                    pstate.p1state = listestate.p1state
                                    pstate.p3state = listestate.p3state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            listestate.p1state,
                                            r.response.toString(),
                                            listestate.p3state,
                                            entityGroupId
                                        )
                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = ""
                                    pstate.p2state = r.response.toString()
                                    pstate.p3state = ""
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }

                                armDevices!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.litegreen)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP1"
                                AppPreference.put(context!!, "CurrentState", "okP2")
                                AppPreference.put(
                                    activity!!,
                                    "NotificationState",
                                    "okP2"
                                )

//                                profileloader()
//                                profile2Selector!!.setBackgroundResource(R.color.litegreen)
//                                armDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP2"
                                try {
//                                    ApplyChanges!!.visibility = View.GONE
                                    AppPreference.put(context!!, "CurrentState", "okP2")
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                                loadDevices()
                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP2")
                                var profileData = ProfileSet()
                                profileData.profile = "okP2"
                                profileData.response = r.extraOutput
                                StatusId = "2"

//                                callHomeProfile2Loader()

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

                            } else if (r.result == "okP3") {

                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)
                                var pstate = ProfileState()
                                pstate.gateway = entityGroupId!!

                                if (listestate != null) {
                                    pstate.p2state = listestate.p2state
                                    pstate.p1state = listestate.p1state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            listestate.p1state,
                                            listestate.p2state,
                                            r.response.toString(),
                                            entityGroupId
                                        )

                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = ""
                                    pstate.p2state = ""
                                    pstate.p3state = r.response.toString()
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }

//                                callHomeProfile3Loader()

                                armDevices!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.litegreen)
                                profileDataStatus = "okP1"
                                AppPreference.put(context!!, "CurrentState", "okP3")
                                AppPreference.put(
                                    activity!!,
                                    "NotificationState",
                                    "okP3"
                                )

//                                profileloader()
//                                profile3Selector!!.setBackgroundResource(R.color.litegreen)
//                                armDevices!!.setBackgroundResource(R.color.whiter)
//                                ApplyChanges!!.visibility = View.GONE
                                profileDataStatus = "okP3"
                                AppPreference.put(context!!, "CurrentState", "okP3")
                                loadDevices()
                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP3")
                                var profileData = ProfileSet()
                                profileData.profile = "okP3"
                                profileData.response = r.extraOutput
                                StatusId = "3"

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }
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
//                        AppNewDialogs.hideProgressDialog()
                        if (r.result != "fail") {
//                            ThingsManager.deleteDevice(
//                                c = activity!!,
//                                l = this,
//                                deviceId = deviceIdTobeModified!!,
//                                devicename = "Account"
//                            )

                            var profileOneData =
                                AppPreference.get(context!!, "FirstProfile", "")
                            if (!profileOneData.isNullOrEmpty()) {
                                var profileOneResult =
                                    deleteRPCProfile(profileOneData, RemovedDeviceindex, "p1")
                                if (profileOneResult.contains("p1")) {
                                    AppPreference.put(
                                        context!!,
                                        "FirstProfile",
                                        profileOneResult
                                    )
                                }
                            }

                            var profileTwoData =
                                AppPreference.get(context!!, "SecondProfile", "")
                            if (!profileTwoData.isNullOrEmpty()) {
                                var profileTwoResult =
                                    deleteRPCProfile(
                                        profileTwoData,
                                        RemovedDeviceindex,
                                        "p2"
                                    )
                                if (profileTwoResult.contains("p2")) {
                                    AppPreference.put(
                                        context!!,
                                        "SecondProfile",
                                        profileTwoResult
                                    )
                                }
                            }

                            var profileThreeData =
                                AppPreference.get(context!!, "ThirdProfile", "")
                            if (!profileThreeData.isNullOrEmpty()) {
                                var profileThreeResult =
                                    deleteRPCProfile(
                                        profileThreeData,
                                        RemovedDeviceindex,
                                        "p3"
                                    )
                                if (profileThreeResult.contains("p3")) {
                                    AppPreference.put(
                                        context!!,
                                        "ThirdProfile",
                                        profileThreeResult
                                    )
                                }
                            }

                            DatabaseClient.getInstance(context)
                                .appDatabase.addDeviceDAO.DeleteSensor(deviceIdTobeModified)
                            DatabaseClient.getInstance(context)
                                .appDatabase.deviceindex.DeleteDevices(deviceIdTobeModified)
                            DatabaseClient.getInstance(context).appDatabase.deviceindex!!.DeleteDevices(
                                deviceIdTobeModified
                            )
                            DatabaseClient.getInstance(context)
                                .appDatabase.allDevices!!.DeleteindexDevices(
                                    RemovedDeviceindex
                                )
                            profileDataStatus = "Schnell"
                            loadDevices()
                        } else if (r.result == "fail") {

                            Toast.makeText(
                                context!!,
                                "Unable to Delete Sensor",
                                Toast.LENGTH_SHORT
                            ).show()

//                            updatetext!!.visibility = View.GONE

//                            AppNewDialogs.hideProgressDialog()
//                            ThingsManager.deleteDevice(
//                                c = activity!!,
//                                l = this,
//                                deviceId = deviceIdTobeModified!!,
//                                devicename = "Account"
//                            )
//
//                            var profileOneData =
//                                AppPreference.get(context!!, "FirstProfile", "")
//                            if (!profileOneData.isNullOrEmpty()) {
//                                var profileOneResult =
//                                    deleteRPCProfile(profileOneData, RemovedDeviceindex, "p1")
//                                AppPreference.put(context!!, "FirstProfile", profileOneResult)
//                            }
//
//                            var profileTwoData =
//                                AppPreference.get(context!!, "SecondProfile", "")
//                            if (!profileTwoData.isNullOrEmpty()) {
//                                var profileTwoResult =
//                                    deleteRPCProfile(
//                                        profileTwoData,
//                                        RemovedDeviceindex,
//                                        "p2"
//                                    )
//                                AppPreference.put(context!!, "SecondProfile", profileTwoResult)
//                            }
//
//                            var profileThreeData =
//                                AppPreference.get(context!!, "ThirdProfile", "")
//                            if (!profileThreeData.isNullOrEmpty()) {
//                                var profileThreeResult =
//                                    deleteRPCProfile(
//                                        profileThreeData,
//                                        RemovedDeviceindex,
//                                        "p3"
//                                    )
//                                AppPreference.put(context!!, "ThirdProfile", profileThreeResult)
//                            }
//                            mAddDeviceDAO!!.DeleteSensor(deviceIdTobeModified)
//                            deviceConfigDAO!!.DeleteDevices(deviceIdTobeModified)
//                            deviceListDAO!!.DeleteindexDevices(RemovedDeviceindex)
//                            profileDataStatus = "Schnell"
//                            loadDevices()

                        } else {
                            Toast.makeText(
                                context,
                                "Failed to delete the device",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
//                    getLatestAttributes()
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
                            DialogTexter!!.visibility = View.VISIBLE
                            for (device in devices) {
                                ThingsManager.saveDevice(
                                    c = activity!!,
                                    l = thisFragment,
                                    entityGroupId = entityGroupId!!,
                                    device = device
                                )
                            }
                            AppPreference.put(context!!.applicationContext, "State", "Arm")
                            armDevices!!.visibility = View.GONE
                            disArmDevices!!.visibility = View.VISIBLE
                            armDevices!!.setBackgroundResource(R.drawable.selected_circle_accent)


                        } else if (StatusSetup == "DisArmAll") {

                            // Save arm state in gateway
                            gatewayDevice!!.additionalInfo!!.armState = false
                            gatewayDevice!!.additionalInfo!!.selectedProfile = "ad"

                            val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                            if (connectionDetails) {

                                // Update all devices either dis arm
                                DialogTexter!!.visibility = View.VISIBLE

                                for (device in devices) {
                                    device.additionalInfo!!.armState = false
                                    ThingsManager.saveDevice(
                                        c = activity!!,
                                        l = thisFragment,
                                        entityGroupId = entityGroupId!!,
                                        device = device
                                    )
                                }
                            } else {
                                Toast.makeText(
                                    context!!,
                                    getString(R.string.nonetwor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            AppPreference.put(context!!.applicationContext, "State", "DisArm")
                            disArmDevices!!.visibility = View.VISIBLE
                            armDevices!!.visibility = View.VISIBLE
                            disArmDevices!!.setBackgroundResource(R.drawable.selected_circle_accent)

                        } else if (StatusSetup == "ApplyAll") {

//                             Save profile in gateway
                            gatewayDevice!!.additionalInfo!!.armState = null
                            gatewayDevice!!.additionalInfo!!.selectedProfile = "p$StatusId"

                            val jresponse = JSONObject(jsonresponse)

                            val connectionDetails = BaseActivity.internetIsAvailable(context!!)
                            if (connectionDetails) {

//                            Update all devices either dis arm based on profile
                                DialogTexter!!.visibility = View.VISIBLE


                                val params = jresponse.get("params") as JSONObject
                                for (device in devices) {
                                    device.additionalInfo!!.armState = params.get(
                                        "${device.additionalInfo!!.deviceIndex}".padStart(
                                            2,
                                            '0'
                                        )
                                    ) == 1
                                    ThingsManager.saveDevice(
                                        c = activity!!,
                                        l = thisFragment,
                                        entityGroupId = entityGroupId!!,
                                        device = device
                                    )
                                }
                            } else {
                                Toast.makeText(
                                    context!!,
                                    getString(R.string.nonetwor),
                                    Toast.LENGTH_SHORT
                                ).show()
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

                                            val connectionDetails =
                                                BaseActivity.internetIsAvailable(context!!)
                                            if (connectionDetails) {
                                                ThingsManager.saveDevice(
                                                    c = activity!!,
                                                    l = thisFragment,
                                                    entityGroupId = entityGroupId!!,
                                                    device = device,
                                                    extraOutput = "Updated device arm status for ${device.getDisplayName()}"
                                                )
                                            } else {
                                                Toast.makeText(
                                                    context!!,
                                                    getString(R.string.nonetwor),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
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
//                    if (r is History) {
//                        r.historyList?.size
//                    }
//                    System.out.println("response---->$")
                    if (r is IndVal) {
                        if (r.dList!!.size == 5) {
                            var deviceIndex = ""
                            var deviceName = ""
                            var deviceuid = ""
                            var deviceEditName = ""
                            var devicetype = ""

                            for (i in 0 until r.dList!!.size) {
                                if (r.dList!!.get(i).devicekey!!.equals("devIndex")) {
                                    deviceIndex = r.dList!!.get(i).Devicevalue.toString()
                                }
                                if (r.dList!!.get(i).devicekey!!.equals("devEditLabel")) {
                                    deviceEditName = r.dList!!.get(i).Devicevalue.toString()
                                }

                                if (r.dList!!.get(i).devicekey!!.equals("deviceuid")) {
                                    deviceuid = r.dList!!.get(i).Devicevalue.toString()
                                }

                                if (r.dList!!.get(i).devicekey!!.equals("devLabel")) {
                                    deviceName = r.dList!!.get(i).Devicevalue.toString()
                                }

                                if (r.dList!!.get(i).devicekey!!.equals("type")) {
                                    devicetype = r.dList!!.get(i).Devicevalue.toString()
                                }
                            }

                            if (!deviceIndex.toString().equals("0")) {
                                val us = deviceIndex.split("/")
                                if (deviceEditName.contains("/*/")) {
                                    var deviceNameDetails = deviceEditName.split("/*/")

                                    val devicedetails =
                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.getDevicebyUid(deviceName)
                                    try {

                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updatedevicetypeall(
                                                deviceNameDetails[0].toString(),
                                                us.get(0),
                                                devicetype,
                                                devicedetails.deviceid
                                            )

                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updateall(
                                                deviceNameDetails[0].toString(),
                                                us.get(0),
                                                devicedetails.deviceid
                                            )
                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updatedeviceuid(
                                                deviceuid,
                                                devicedetails.deviceid
                                            )

                                    } catch (e: SQLiteConstraintException) {
                                        System.out.println(e)
                                    }

                                    DatabaseClient.getInstance(context).appDatabase
                                        .geAttributesDAO()
                                        .DeletedeviceSensor(devicedetails.deviceid)

                                    val latestAttribute = LatestAttribute()
                                    latestAttribute.deviceid = devicedetails.deviceid
                                    latestAttribute.devicename = deviceNameDetails[0].toString()

                                    try {
                                        DatabaseClient.getInstance(context).appDatabase
                                            .geAttributesDAO().insert(latestAttribute)
                                    } catch (e: SQLiteConstraintException) {
                                        System.out.println(e)
                                    }

                                } else {
                                    val devicedetails = DatabaseClient.getInstance(context)
                                        .appDatabase.addDeviceDAO.getDevicebyUid(deviceName)
                                    try {
                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updateall(
                                                deviceName,
                                                us.get(0),
                                                devicedetails.deviceid
                                            )

                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updatedevicetypeall(
                                                deviceName,
                                                us.get(0),
                                                devicetype,
                                                devicedetails.deviceid
                                            )

                                        DatabaseClient.getInstance(context)
                                            .appDatabase.addDeviceDAO.updatedeviceuid(
                                                deviceuid,
                                                devicedetails.deviceid
                                            )
                                    } catch (e: SQLiteConstraintException) {
                                        System.out.println(e)
                                    }
                                }

                                GetState = true
                                DialogTexter!!.visibility = View.VISIBLE

                                ThingsManager.getdevicedetails(
                                    c = activity!!,
                                    l = this,
                                    deviceId = DvalId!!
                                )
                            }

                        } else {
                            if (r.dList!![0].Devicevalue.equals("ARMED")) {

                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.litegreen)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "Arm"
                                AppPreference.put(context!!.applicationContext, "State", "Arm")
                                loadDevices()

                            } else if (r.dList!![0].Devicevalue.equals("DISARM")) {

                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                disArmDevices!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "DisArm"
                                AppPreference.put(context!!.applicationContext, "State", "DisArm")
                                loadDevices()

                            } else if (r.dList!![0].Devicevalue.equals("HOME")) {

                                profile1Selector!!.setBackgroundResource(R.color.litegreen)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "p1"

//                                ApplyChanges!!.visibility = View.VISIBLE
                                AppPreference.put(context!!.applicationContext, "State", "Home")
                                loadDevices()

                            } else if (r.dList!![0].Devicevalue!!.contains("okP1")) {

                                DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.devices
                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)

                                if (listestate != null) {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p2state = listestate.p2state
                                    pstate.p3state = listestate.p3state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            r.dList!![0].Devicevalue!!,
                                            listestate.p2state,
                                            listestate.p3state,
                                            entityGroupId
                                        )
                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = r.dList!![0].Devicevalue!!
                                    pstate.p2state = ""
                                    pstate.p3state = ""
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }

                                profile1Selector!!.setBackgroundResource(R.color.litegreen)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP1"

                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP1")
                                var profileData = ProfileSet()
                                profileData.profile = "okP1"
                                profileData.response = r.dList!![0].Devicevalue!!.toString()
                                StatusId = "1"

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

//                                ApplyChanges!!.visibility = View.VISIBLE
                                AppPreference.put(context!!.applicationContext, "State", "Home")
                                loadDevices()

                            } else if (r.dList!![0].Devicevalue!!.contains("okP2")) {

                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)
                                var pstate = ProfileState()
                                pstate.gateway = entityGroupId!!

                                if (listestate != null) {
                                    pstate.p1state = listestate.p1state
                                    pstate.p3state = listestate.p3state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            "",
                                            r.dList!![0].Devicevalue!!,
                                            "",
                                            entityGroupId
                                        )
                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = ""
                                    pstate.p2state = r.dList!![0].Devicevalue!!
                                    pstate.p3state = ""
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }

                                profile2Selector!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile3Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP2"
//                                ApplyChanges!!.visibility = View.VISIBLE

                                try {
//                                    ApplyChanges!!.visibility = View.GONE
                                    AppPreference.put(context!!, "CurrentState", "okP2")
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }

                                loadDevices()
                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP2")
                                var profileData = ProfileSet()
                                profileData.profile = "okP2"
                                profileData.response = r.dList!![0].Devicevalue!!.toString()
                                StatusId = "2"

//                                callHomeProfile2Loader()

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

                                AppPreference.put(context!!.applicationContext, "State", "Home")
                                loadDevices()

                            } else if (r.dList!![0].Devicevalue!!.contains("okP3")) {

                                val listestate = DatabaseClient.getInstance(context)
                                    .appDatabase!!.profileState.getstatebyGw(entityGroupId)
                                var pstate = ProfileState()
                                pstate.gateway = entityGroupId!!

                                if (listestate != null) {
                                    pstate.p2state = listestate.p2state
                                    pstate.p1state = listestate.p1state

                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.updateall(
                                            listestate.p1state,
                                            listestate.p2state,
                                            r.dList!![0].Devicevalue!!,
                                            entityGroupId
                                        )

                                } else {
                                    var pstate = ProfileState()
                                    pstate.gateway = entityGroupId!!
                                    pstate.p1state = ""
                                    pstate.p2state = ""
                                    pstate.p3state = r.dList!![0].Devicevalue!!
                                    DatabaseClient.getInstance(context)
                                        .appDatabase!!.profileState.insert(pstate)
                                }

                                profile3Selector!!.setBackgroundResource(R.color.litegreen)
                                profile1Selector!!.setBackgroundResource(R.color.whiter)
                                profile2Selector!!.setBackgroundResource(R.color.whiter)
                                armDevices!!.visibility = View.VISIBLE
                                disArmDevices!!.visibility = View.VISIBLE
                                armDevices!!.setBackgroundResource(R.color.whiter)
                                disArmDevices!!.setBackgroundResource(R.color.whiter)
                                profileDataStatus = "okP3"
//                                ApplyChanges!!.visibility = View.VISIBLE

                                DatabaseClient.getInstance(context).appDatabase
                                    .getprofileDao()
                                    .Deleteprfoile("okP3")
                                var profileData = ProfileSet()
                                profileData.profile = "okP3"
                                profileData.response = r.dList!![0].Devicevalue!!.toString()
                                StatusId = "3"

                                try {
                                    DatabaseClient.getInstance(context).appDatabase
                                        .getprofileDao().insert(profileData)
                                } catch (e: SQLiteConstraintException) {
                                    System.out.println(e)
                                }

                                AppPreference.put(context!!.applicationContext, "State", "Home")
                                loadDevices()
                            }
                        }
                    }
                }

                ThingsManager.API.getDeviceFromenityGroup.hashCode() -> {
                    if (r is Device)
                        for (devsensor in r.deviceList!!) {
                            val addDevice = AddDevice()
                            addDevice.devicename = ""
                            addDevice.deviceuid = devsensor.name!!
                            addDevice.devicetype = ""
                            DvalId = devsensor.id!!.id!!
                            addDevice.deviceid = devsensor.id!!.id!!
                            addDevice.deviceindex = ""
                            addDevice.entitygroupid = entityGroupId
                            addDevice.gatewayDeviceId = devsensor.id!!.id!!
                            gatewayDataDeviceId = devsensor.id!!.id!!

                            if (entityGWName!!.equals(devsensor.name)) {
                                addDevice.devicename = entityGWName
                                addDevice.devicetype = "gw"
                                gatewayDataDeviceId = devsensor.id!!.id!!
                                addDevice.deviceindex = "0"
                                AppPreference.put(
                                    activity!!,
                                    "gatewaydeviceid",
                                    entityGroupId!!
                                )
                                AppPreference.put(
                                    activity!!,
                                    "gdeviceid",
                                    gatewayDataDeviceId!!
                                )
                            }

                            try {
                                DatabaseClient.getInstance(context)
                                    .appDatabase.addDeviceDAO.insert(addDevice)
                            } catch (e: SQLiteConstraintException) {
                                System.out.println(e)
                            }

                            val deviceDetails =
                                DatabaseClient.getInstance(context)
                                    .appDatabase.deviceindex.getDevice(
                                        gatewayDataDeviceId,
                                        DvalId
                                    )
                            if (deviceDetails == null) {
                                val deviceConfig = DeviceConfig()
                                deviceConfig.gatewayid = entityGroupId
                                deviceConfig.deviceid = DvalId
                                deviceConfig.devicestatus = "ok"
                                deviceConfig.devicetype = ""
                                deviceConfig.deviceindex =
                                    r.additionalInfo!!.deviceIndex.toString()
                                deviceConfig.deviceloader = "0"
                                DatabaseClient.getInstance(context)
                                    .appDatabase.deviceindex.insert(deviceConfig)
                            }
                            getDeviceIndexVal(DvalId!!)
                        }

                    DialogTexter!!.visibility = View.VISIBLE

//                    ThingsManager.addAttribute(
//                        c = activity!!,
//                        l = thisFragment,
//                        deviceId = gatewayDataDeviceId!!
//                    )

//                    AppNewDialogs.showProgressDialog(
//                        context = thisFragment.context!!,
//                        desc = "Fetching current or Exact state from Gateway"
//                    )
                    callGetState()
                }

//                ThingsManager.API.getDeviceFromenityGroup.hashCode() -> {
//                    if (r is Device)
//                        for (devsensor in r.deviceList!!) {
//
//                            val addDevice = AddDevice()
//                            addDevice.devicename = ""
//                            addDevice.deviceuid = devsensor.name!!
//                            addDevice.devicetype = ""
//                            DvalId = devsensor.id!!.id!!
//                            addDevice.deviceid = devsensor.id!!.id!!
//                            addDevice.deviceindex = "0"
//                            addDevice.entitygroupid = entityGroupId
//                            addDevice.gatewayDeviceId = devsensor.id!!.id!!
//                            gatewayDataDeviceId = devsensor.id!!.id!!
//
//                            try {
//                                DatabaseClient.getInstance(context!!).appDatabase.addDeviceDAO!!.insert(
//                                    addDevice
//                                )
//                            } catch (e: SQLiteConstraintException) {
//                                System.out.println(e)
//                            }
//
//                            GetState = false
//
//                            val connectionDetails = BaseActivity.internetIsAvailable(context!!)
//                            if (connectionDetails) {
//                                AppDialogs.showProgressDialog(
//                                    context = activity!!,
//                                    desc = "Fetching Datas From Server"
//                                )
//                                ThingsManager.getdevicedetails(
//                                    c = activity!!,
//                                    l = this,
//                                    deviceId = DvalId!!
//                                )
//                            } else {
//                                Toast.makeText(
//                                    context!!,
//                                    getString(R.string.nonetwor),
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
////                            GetDeviceIndexVal(DvalId!!)
//                        }
//                    val connectionDetails = BaseActivity.internetIsAvailable(context!!)
//                    if (connectionDetails) {
////                        ThingsManager.addAttribute(
////                            c = activity!!,
////                            l = thisFragment,
////                            deviceId = gatewayDataDeviceId!!
////                        )
//                    } else {
//                        Toast.makeText(context!!, getString(R.string.nonetwor), Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }

                ThingsManager.API.getDeviceindexval.hashCode() -> {
                    if (r is IndVal) {
                        r.dList?.size
                    }
                }

                ThingsManager.API.enityGroup.hashCode() -> {
                    if (r.result == "ok") {
                        Handler().postDelayed({
                            val i = context!!.packageManager
                                .getLaunchIntentForPackage(context!!.packageName)
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

    private fun getDeviceIndexVal(deviceid: String) {
        DialogTexter!!.visibility = View.VISIBLE
        ThingsManager.getdeviceindexval(context!!, thisFragment, deviceid)
    }

    private fun deleteRPCProfile(
        rpcstring: String?,
        deviceindex: String?,
        profile: String?
    ): String {
        var finaljson: String? = null
        val gson = Gson()
        try {
            val json = JSONObject(rpcstring)
            var getParams = json.get("params")
            val Paramvalues = getParams.toString()
            val linkedresult = gson.fromJson(Paramvalues, LinkedTreeMap::class.java)
            linkedresult.remove(deviceindex)
            val jsonObject: JsonObject = gson.toJsonTree(linkedresult).asJsonObject
            val removedresult = jsonObject.toString().replace(".0", "")
            val lastjson = JSONObject(removedresult)
            finaljson = "{\"params\":$lastjson,\"method\":\"$profile\",\"timeout\":40000}"
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return finaljson!!
    }

    class GetDetails(
        context: Context,
        entityGroupId: String,
        thisFragment: GatewayFragment
    ) : AsyncTask<Unit, Unit, String>() {

        val context = context
        val entityGroupId = entityGroupId
        val thisFragment = thisFragment

        override fun doInBackground(vararg params: Unit?): String? {
            System.out.println("Testing            ->>>>>>>>>>>>>>>>>>>>>>>>>>>")
            val connectionDetails = BaseActivity.internetIsAvailable(context)
            if (connectionDetails) {
                ThingsManager.getdevicelist(
                    c = context,
                    l = thisFragment,
                    entityGroupId = entityGroupId
                )
            } else {
                Toast.makeText(
                    context,
                    "Please check your Network Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return null
        }
    }

}



