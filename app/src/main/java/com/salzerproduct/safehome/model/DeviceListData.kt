package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName

class DeviceListData : ThingsBoardResponse() {

    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("created_time")
    var createdtime: String? = null

    @SerializedName("data")
    var deviceList: ArrayList<DeviceListData>? = null

}
