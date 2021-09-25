package com.salzerproduct.safehome.model

import com.google.gson.annotations.SerializedName
import com.salzerproduct.http.Response

class LoginResponse : Response() {

    @SerializedName("token")
    var token: String? = null

    @SerializedName("refreshToken")
    var refreshToken: String? = null

}