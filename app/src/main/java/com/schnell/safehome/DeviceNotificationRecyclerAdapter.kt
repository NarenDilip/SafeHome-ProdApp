package com.schnell.safehome

import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.schnell.database.model.AddDeviceDAO
import com.schnell.database.model.AppDatabase
import com.schnell.database.model.Devices


class DeviceNotificationRecyclerAdapter internal constructor(
    private val context: Context,
    private var deviceList: ArrayList<Devices>?,

    private val colors: IntArray
) : RecyclerView.Adapter<DeviceNotificationRecyclerAdapter.ViewHolder>() {

    private var mActionCallbacks: ActionCallback? = null
    private var mContactRecyclerAdapter: DeviceNotificationRecyclerAdapter? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null

    private var device: MutableList<Devices>? = null
    private var isLoadingAdded = false

    val notificationObserver = Observer<List<Devices>> {
        try {
            deviceList?.clear()
            deviceList?.addAll(it!!)
            notifyDataSetChanged()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Interface for callbacks
    internal interface ActionCallback {
        fun onLongClickListener(device: Devices)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recycler_contact, parent, false)

        mAddDeviceDAO = Room.databaseBuilder(context, AppDatabase::class.java, "db-devices")
            .allowMainThreadQueries() //Allows room to do operation on main thread
            .build()
            .addDeviceDAO

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return deviceList!!.size
    }

    fun updateData(device: ArrayList<Devices>) {
        this.deviceList = device
        deviceList?.clear()
        deviceList?.addAll(device)
        mContactRecyclerAdapter!!.notifyDataSetChanged()
    }

    fun setData(newData: ArrayList<Devices>) {
        val diffResult = DiffUtil.calculateDiff(MyDiffUtilCallBack(newData, deviceList))
        diffResult.dispatchUpdatesTo(this)
        deviceList!!.clear()
        this.deviceList!!.addAll(newData)
    }

    //View Holder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        private val mDate: TextView
        private val mDeviceName: TextView
        //        private val mSensortype: TextView
        private val mStatus: TextView

        init {
            itemView.setOnLongClickListener(this)
            mDate = itemView.findViewById(R.id.datetime)
            mDeviceName = itemView.findViewById(R.id.devicename)
//            mSensortype = itemView.findViewById(R.id.sensor)
            mStatus = itemView.findViewById(R.id.alert)
        }

        fun bindData(position: Int) {
            try {
                val devices = deviceList!![position]
//                val addDevice = mAddDeviceDAO!!.getDevicebyUid(devices.name)
                mDate.text = devices.createdDate.toString()
//                mDeviceName.text = addDevice.devicename
                mDeviceName.text = devices.name

//                if (devices.type == "gw") {
//                    mSensortype.text = "Gateway"
//                } else if (devices.type == "ds") {
//                    mSensortype.text = "Door Sensor"
//                } else if (devices.type == "ms") {
//                    mSensortype.text = "Movement Sensor"
//                } else if (devices.type == "rm") {
//                    mSensortype.text = "Remote"
//                } else {
//                    mSensortype.text = devices.type
//                }
                mStatus.text = "     " + devices.alert
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (mActionCallbacks != null) {
                mActionCallbacks!!.onLongClickListener(deviceList!![adapterPosition])
            }
            return true
        }
    }

    internal fun addActionCallback(actionCallbacks: ActionCallback) {
        mActionCallbacks = actionCallbacks
    }

    /*
Helpers
_________________________________________________________________________________________________
 */

    fun add(mc: Devices) {
        device!!.add(mc)
        notifyItemInserted(device!!.size - 1)
    }

    fun addAll(mcList: List<Devices>) {
        for (mc in mcList) {
            add(mc)
        }
    }

    fun remove(city: Devices?) {
        val position = device!!.indexOf(city)
        if (position > -1) {
            device!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun isEmpty(): Boolean {
        return itemCount == 0
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Devices())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = device!!.size - 1
        val item = getItem(position)

        if (item != null) {
            device!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Devices? {
        return device!!.get(position)
    }

    /*
   View Holders
   _________________________________________________________________________________________________
    */
}
