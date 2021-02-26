package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName

class DevicedetailsInfo : ThingsBoardResponse() {

    @SerializedName("additionalInfo")
    var deviceInfo: DeviceInfo? = null

    var devicedata: ArrayList<DevicedetailsInfo>? = null

}