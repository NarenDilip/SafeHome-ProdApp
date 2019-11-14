package com.schnell.safehome

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.schnell.safehome.model.History
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

            textViewName.text = user?.name
            textViewAddress.text = user?.value
        }
    }
}

