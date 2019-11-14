package com.schnell.safehome

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.util.DiffUtil
import com.schnell.database.model.Devices


class MyDiffUtilCallBack(internal var newList: ArrayList<Devices>?, internal var oldList: ArrayList<Devices>?) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return if (oldList != null) oldList!!.size else 0
    }

    override fun getNewListSize(): Int {
        return if (newList != null) newList!!.size else 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList!![newItemPosition].name === oldList!!.get(oldItemPosition).name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val result = newList!![newItemPosition].compareTo(oldList!![oldItemPosition])
        return result == 0
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

        val newModel = newList!![newItemPosition]
        val oldModel = oldList!![oldItemPosition]

        val diff = Bundle()

        if (newModel.id !== oldModel.id) {
            diff.putInt("id", newModel.id)
        }
        return if (diff.size() == 0) {
            null
        } else diff
//return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
