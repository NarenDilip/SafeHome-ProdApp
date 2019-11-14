package com.schnell.safehome.model

import com.google.gson.annotations.SerializedName
import com.schnell.http.Response

class LoginResponse : Response() {

    @SerializedName("token")
    var token: String? = null

    @SerializedName("refreshToken")
    var refreshToken: String? = null

}