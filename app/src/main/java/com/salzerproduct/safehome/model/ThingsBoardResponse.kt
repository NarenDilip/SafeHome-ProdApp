package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName
import com.salzerproduct.http.Response

open class ThingsBoardResponse : Response() {

    @SerializedName("createdTime")
    var createdTime: Long = 0
    @SerializedName("id")
    var id: Entity? = null
    @SerializedName("tenantId")
    var tenantId: Entity? = null
    @SerializedName("customerId")
    var customerId: Entity? = null
    @SerializedName("ownerId")
    var ownerId: Entity? = null
    @SerializedName("deviceIndex")
    var deviceIndex = 0
    @SerializedName("additionalInfo")
    var additionalInfo: AdditionalInfo? = AdditionalInfo()

}