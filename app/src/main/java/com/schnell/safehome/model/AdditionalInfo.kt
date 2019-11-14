package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class AdditionalInfo : Serializable {
    @SerializedName("lang")
    var lang: String? = null

    @SerializedName("privacyPolicyAccepted")
    var privacyPolicyAccepted: String? = null

    @SerializedName("registrationIds")
    var registrationIds: ArrayList<String> = arrayListOf()


    @SerializedName("gateway")
    var gateway: Boolean? = null

    @SerializedName("description")
    var description: Boolean? = null

    @SerializedName("deviceIndex")
    var deviceIndex: Int? = null

    @SerializedName("armState")
    var armState: Boolean? = null

    @SerializedName("displayName")
    var displayName: String? = null

    @SerializedName("gatewaySimNumber")
    var gatewaySimNumber: String? = null

    @SerializedName("profile1")
    var profile1: String? = null

    @SerializedName("profile2")
    var profile2: String? = null

    @SerializedName("profile3")
    var profile3: String? = null

    @SerializedName("selectedProfile")
    var selectedProfile: String? = null
}