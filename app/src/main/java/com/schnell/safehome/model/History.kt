package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName

class History : ThingsBoardResponse() {

    @SerializedName("ts")
    var name: String? = null

    @SerializedName("value")
    var value: String? = null

    @SerializedName("alert")
    var historyList: ArrayList<History>? = null

}