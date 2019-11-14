package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName

class User : ThingsBoardResponse() {

    @SerializedName("email")
    var email: String? = null
    @SerializedName("authority")
    var authority: String? = null

    @SerializedName("firstName")
    var firstName: String? = null
    @SerializedName("lastName")
    var lastName: String? = null

    @SerializedName("name")
    var name: String? = null
}
