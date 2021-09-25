package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName

class IndVal : ThingsBoardResponse() {

    @SerializedName("lastUpdateTs")
    var tsdata: String? = null

    @SerializedName("key")
    var devicekey: String? = null

    @SerializedName("value")
    var Devicevalue: String? = null

    @SerializedName("data")
    var dList: ArrayList<IndVal>? = null

}