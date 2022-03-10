package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName
import com.salzerproduct.http.Response

class FromAddress : Response() {

    @SerializedName("from")
    var from: Entity? = null

    @SerializedName("to")
    var to: Entity? = null

    @SerializedName("fromName")
    var fromName: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("data")
    var fromList: ArrayList<FromAddress>? = null

}