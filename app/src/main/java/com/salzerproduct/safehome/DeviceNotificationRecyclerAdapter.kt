package com.salzerproduct.safehome.adapter

import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.salzerproduct.database.model.AddDeviceDAO
import com.salzerproduct.database.model.AppDatabase
import com.salzerproduct.database.model.Devices
import com.salzerproduct.safehome.MyDiffUtilCallBack
import com.salzerproduct.safehome.R
import java.util.*
import kotlin.collections.ArrayList

class DeviceNotificationRecyclerAdapter internal constructor(
    private val context: Context,
    private var deviceList: ArrayList<Devices>?,
    private var cleanlist: ArrayList<Devices>?,

    private val colors: IntArray
) : RecyclerView.Adapter<DeviceNotificationRecyclerAdapter.ViewHolder>() {

    private var mActionCallbacks: ActionCallback? = null
    private var mContactRecyclerAdapter: DeviceNotificationRecyclerAdapter? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null

    val notificationObserver = Observer<List<Devices>> {
        try {
            deviceList?.clear()
            cleanlist?.clear()
            deviceList?.addAll(it!!)
            cleanlist?.addAll(it!!)
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
        cleanlist?.addAll(device)
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
                mDeviceName.text = devices.devLabel

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

                val strt = devices.alert.replace("+", " ")

                mStatus.text = "     " + strt
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


    fun filter(charText: String) {
        var charText = charText
        charText = charText.toLowerCase(Locale.getDefault())
        deviceList = ArrayList<Devices>();
        if (charText.length == 0) {
            deviceList!!.addAll(cleanlist!!)
        } else {
            for (item in cleanlist!!) {
                if (item.alert.toLowerCase(Locale.getDefault()).contains(charText) || item.createdDate.toString().contains(
                        charText
                    )
                ) {
                    deviceList!!.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

//    fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
//                val charString = charSequence.toString()
//                if (charString.isEmpty()) {
//                    deviceList = deviceList
//
//                    val filterResults = Filter.FilterResults()
//                    filterResults.values = deviceList
//                    return filterResults
//                    notifyDataSetChanged()
//
//                } else {
//                    val filteredList = ArrayList<Devices>()
//                    for (row in deviceList!!) {
//                        if (row.alert!!.contains(charSequence)) {
//                            filteredList.add(row)
//                        }
//                    }
//                    val filterResults = Filter.FilterResults()
//                    filterResults.values = filteredList
//                    return filterResults
//                    notifyDataSetChanged()
//                }
//            }
//
//            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
//                deviceList = filterResults.values as ArrayList<Devices>
//                notifyDataSetChanged()
//            }
//        }
//    }
}
