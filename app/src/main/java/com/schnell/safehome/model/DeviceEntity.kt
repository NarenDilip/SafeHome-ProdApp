package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName

class DeviceEntity : ThingsBoardResponse() {

    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var type: String? = null
    @SerializedName("data")
    var deviceList: ArrayList<DeviceEntity> = arrayListOf()
}