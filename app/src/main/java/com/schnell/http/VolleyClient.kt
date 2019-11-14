package com.schnell.http

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.schnell.util.Exclude
import org.json.JSONObject
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

/**
 * @since 5/4/17.
 * Abstract class to handle common behaviors of HttpClient
 */

abstract class VolleyClient
/**
 * @param c   Context of Application
 * @param method   Request method [Request.Method].Method.POST / others from Request.Method.*
 * @param url Url of the request
 * @param requestType  Unique request type from constants. This param may be deprecated in future release.
 */
internal constructor(
    internal var c: Context?,
    internal var method: Int,
    internal var url: String,
    internal var requestType: Int
) : com.android.volley.Response.ErrorListener {

    // Expremental
    var extraOutput: String? = null
    internal var timeout = 100000 // 100 seconds - time out
    internal var mHeaders: MutableMap<String, String> = HashMap()
    internal var mParams: MutableMap<String, String> = HashMap()
    internal var l: ResponseListener? = null
    internal var responseType: Type = Response::class.java

    init {
        Log.i("V-Url", url)
    }

    override fun onErrorResponse(error: VolleyError) {
        try {
            Log.e("V-R-Error", "$error:${String(error.networkResponse.data)}")
            if (l != null) {
                var r = parseResponse(String(error.networkResponse.data), Response::class.java)
                if (r == null) {
                    r = Response()
                    r.status = error.networkResponse.statusCode
                }
                r.requestType = requestType
                r.statusMessage = "Failure"
                r.extraOutput = extraOutput
                r.requestMethod = method
                l!!.onResponse(r)
            }
            error.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Default is 50 seconds
     *
     * @param timeout Set time out in seconds
     */
    open fun setTimeout(timeout: Int) {
        this.timeout = timeout * 1000
    }

    /**
     * @param key   Param name / key
     * @param value Param value
     */
    fun addParam(key: String, value: String) {
        mParams[key] = value
    }

    /**
     * @param key   Header name / key
     * @param value Header value
     */
    fun addHeader(key: String, value: String) {
        mHeaders[key] = value
    }

    /**
     * All Collection
     *
     * @param headers Map of Params
     */
    internal fun addHeaders(headers: Map<String, String>) {
        this.mHeaders.putAll(headers)
    }

    companion object {

        //        /**
//         * @param data    Response String
//         * @param model [Type] of Sub class of [Response]
//         * @return object of @param model
//         */
        @Synchronized
        internal fun parseResponse(data: String, model: Type): Response? {
            return try {
                Log.i("V-Response", data)
                // converting or parsing the content
                GsonBuilder().create().fromJson<Response>(data, model)
            } catch (e: JsonParseException) {
                e.printStackTrace()
                null
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
