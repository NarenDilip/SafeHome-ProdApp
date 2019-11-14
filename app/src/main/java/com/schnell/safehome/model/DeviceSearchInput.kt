package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DeviceSearchInput : Serializable {
    class Parameter : Serializable {
        @SerializedName("entityId")
        var entityId: Entity? = null
        @SerializedName("rootId")
        var rootId: String? = null
        @SerializedName("rootType")
        var rootType: String? = null
        @SerializedName("direction")
        var direction = "FROM"
        @SerializedName("relationTypeGroup")
        var relationTypeGroup = "COMMON"
        @SerializedName("maxLevel")
        var maxLevel = 1
    }

    @SerializedName("deviceTypes")
    var deviceTypes: ArrayList<String> = arrayListOf()
    @SerializedName("parameters")
    var parameters: Parameter =
        Parameter()
    @SerializedName("relationType")
    var relationType = "CONTAINS"
}