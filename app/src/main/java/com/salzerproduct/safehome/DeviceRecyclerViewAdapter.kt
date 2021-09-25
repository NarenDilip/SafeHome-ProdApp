package com.salzerproduct.safehome

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import com.salzerproduct.safehome.GatewayFragment.OnDeviceActionTriggered
import com.salzerproduct.safehome.model.Device
import kotlinx.android.synthetic.main.device_card.view.*

/**
 * [RecyclerView.Adapter] that can display a [Device] and makes a call to the
 * specified [OnDeviceActionTriggered].
 * TODO: Replace the implementation with code for your data type.
 */
class DeviceRecyclerViewAdapter(
    private val mDevices: List<Device>,
    private val mListener: OnDeviceActionTriggered?
) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val mOnLongClickListener: View.OnLongClickListener
    private var lastPosition = -1

    init {
        mOnClickListener = View.OnClickListener { v ->
            val device = v.tag as Device
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            if (v is ToggleButton) {
                mListener?.onDeviceButtonSwitched(device, v.isChecked)
            } else if (v is TextView) {
                mListener?.onDeviceSelected(device)
            }
        }

        mOnLongClickListener = View.OnLongClickListener { v ->
            val device = v.tag as Device
            mListener?.onDeviceLongPressed(device)
            return@OnLongClickListener true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDevices[position]
        holder.deviceName.text =
            if (item.additionalInfo != null) item.additionalInfo!!.displayName else item.name
        holder.deviceArm.isChecked =
            item.additionalInfo != null && item.additionalInfo!!.armState == true
        holder.deviceId.text = item.additionalInfo!!.deviceIndex.toString()


        if (item.type == "rm") {
            holder.deviceArm.visibility = View.INVISIBLE
        } else {
            holder.deviceArm.visibility = View.VISIBLE
        }

        val left = when (item.type) {
            "ds" -> R.drawable.door_open
            "ms" -> R.drawable.water_meter_train_variant
            "rm" -> R.drawable.remote
            "gw" -> R.drawable.router_wireless
            else -> R.drawable.tablet_cellphone
        }

        holder.deviceIcon.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)

//        val right = if (position % 3 == 0) {
//            R.drawable.battery_10
//        } else {
//            0
//        }
//        holder.deviceName.setCompoundDrawablesWithIntrinsicBounds(0, 0, right, 0)

        with(holder.deviceName) {
            tag = item
            setOnClickListener(mOnClickListener)
        }

        with(holder.mView) {
            tag = item
            setOnLongClickListener(mOnLongClickListener)
        }

        with(holder.deviceArm) {
            var details = AppPreference.get(context, "State", "")
            if (details == "Home") {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

//        if (holder.deviceName.text.contains("Remote")) {
//            holder.deviceArm.visibility = View.INVISIBLE
//        } else {
//            holder.deviceArm.visibility = View.VISIBLE
//        }
    }

    override fun getItemCount(): Int = mDevices.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val deviceIcon: TextView = mView.deviceIcon
        val deviceId: TextView = mView.deviceid
        val deviceName: TextView = mView.deviceName
        val deviceArm: ToggleButton = mView.deviceArm

        override fun toString(): String {
            return super.toString() + " '" + deviceName.text + "'"
        }
    }
}
