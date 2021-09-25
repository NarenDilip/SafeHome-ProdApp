package com.salzerproduct.http

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.salzerproduct.util.Utility
import java.io.IOException
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL


/**
 * @since 23/2/17.
 * Wrapper class for handling the http request using Volley library
 */

class RestClient
/**
 * @param c   Context of Application
 * @param m   Request method [Request.Method].Method.POST / others from Request.Method.*
 * @param url Url of the request
 * @param rt  Unique request type from constants. This param may be deprecated in future release.
 */
    (c: Context?, m: Int, url: String, rt: Int) : VolleyClient(c, m, url, rt),
    com.android.volley.Response.Listener<String> {

    /**
     * Default is 50 seconds
     *
     * @param timeout Set time out in seconds
     */
    override fun setTimeout(timeout: Int) {
        this.timeout = timeout * 1000
    }

    override fun onResponse(response: String) {
        try {
            if (l != null) {
                val responseString = if (response.startsWith("[")) "{\"data\":$response}" else response
                val r = VolleyClient.parseResponse(responseString, responseType)
                if (r != null) {
                    r.requestType = requestType
                    r.extraOutput = extraOutput
                    r.requestMethod = method
                }
                l!!.onResponse(r)
            } else {
                Log.w("RestClient", "Response received but not listened.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Executes the url request that is preset in constructor
     *
     * @param l            Response Listener which is implemented in the activity
     * @param responseType Response type from one of model default is [Response]
     */
    @Throws(Exception::class)
    fun execute(l: ResponseListener?, responseType: Type) {
        this.l = l
        this.responseType = responseType
        try {
            if (Utility.isInternetAvailable(c)) {
                val hurlStack = object : HurlStack() {
                    @Throws(IOException::class)
                    override fun createConnection(url: URL): HttpURLConnection {
                        val connection = super.createConnection(url)
                        Log.i("VolleyClient", "Connection created")
                        return connection
                    }
                }

                val queue = Volley.newRequestQueue(c, hurlStack)

                val request = object : StringRequest(method, url, this, this) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        addHeader("Accept-Encoding", "")
                        return mHeaders
                    }

                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        return mParams
                    }
                }

                // Request Time out
                request.retryPolicy = DefaultRetryPolicy(
                    timeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )

                System.setProperty("http.keepAlive", "false")

                // Default request queue
                queue.add(request)
            } else {
                throw ConnectException("No network access detected")
            }
        } catch (e: Exception) {
            Log.e("RestClint", "Exception-------------------\n")
            throw e
        }

    }
}
