package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName

class Device : ThingsBoardResponse() {

    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("lastUpdateTs")
    var userts: String? = null

    @SerializedName("key")
    var userkey: String? = null

    @SerializedName("value")
    var userval: String? = null

    @SerializedName("data")
    var deviceList: ArrayList<Device>? = null

    fun getDisplayName(): String {
        return if (additionalInfo != null && additionalInfo!!.displayName != null) additionalInfo!!.displayName!! else name!!
    }
}