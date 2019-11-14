package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName

class DeviceInfo : ThingsBoardResponse() {

    @SerializedName("armState")
    var armstate: String? = null

    @SerializedName("deviceIndex")
    var devindex: String? = null

    @SerializedName("displayName")
    var dname: String? = null
}