package com.salzerproduct.http

import com.android.volley.Request
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.Serializable

/**
 * @since 23/2/17.
 * Response base class
 */
open class Response : Serializable {

    /**
     * unique integer number for the request
     */
    @SerializedName("requestType")
    var requestType: Int? = null

    @SerializedName("response")
    var response: JSONObject? = null

    @SerializedName("statusMessage")
    var statusMessage: String? = null

    @SerializedName("extraOutput")
    var extraOutput: String? = null

    var requestMethod = Request.Method.GET

    @SerializedName("status")
    var status: Int? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("result")
    var result: String? = null

    @SerializedName("errorCode")
    var errorCode: Int? = null

    @SerializedName("profilestate")
    var profilestate: String? = null

    @SerializedName("devid")
    var devid: String? = null

    @SerializedName("devType")
    var devType: String? = null

    @SerializedName("devIndex")
    var devIndex: String? = null

}
