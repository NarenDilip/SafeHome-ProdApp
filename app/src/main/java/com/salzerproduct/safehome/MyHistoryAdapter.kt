package com.salzerproduct.safehome

import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.salzerproduct.safehome.model.History
import java.util.*
import kotlin.collections.ArrayList


class MyHistoryAdapter(val userList: ArrayList<History>?) : RecyclerView.Adapter<MyHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHistoryAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_history_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyHistoryAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList!![position])
    }

    override fun getItemCount(): Int {
        return userList!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(user: History?) {
            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            val textViewAddress = itemView.findViewById(R.id.textViewAddress) as TextView


            val cal = Calendar.getInstance(Locale.ENGLISH)
            var namedetails = user?.name
            val l = java.lang.Long.parseLong(namedetails)
            cal.timeInMillis = l
            val date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString()

//            val l = java.lang.Long.parseLong(user?.name)
//            var tester = getDate(l)


            textViewName.text = date
            textViewAddress.text = user?.value
        }

        private fun getDate(time: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.setTimeInMillis(time * 1000)
            return DateFormat.format("dd-MM-yyyy", cal).toString()
        }
    }


}

