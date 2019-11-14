package com.schnell.http

import android.content.Context
import android.os.Environment
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.schnell.util.Utility
import java.io.*
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL


/**
 * @since 23/2/17.
 * Wrapper class for handling the http request using Volley library
 */

class DownloadClient
/**
 * @param c   Context of Application
 * @param m   Request method [Request.Method].Method.POST / others from Request.Method.*
 * @param url Url of the request
 * @param rt  Unique request type from constants. This param may be deprecated in future release.
 */
    (c: Context, m: Int, url: String, rt: Int) : VolleyClient(c, m, url, rt),
    com.android.volley.Response.Listener<ByteArray> {

    init {
        setTimeout(120)
    }


    override fun onResponse(response: ByteArray) {
        try {
            if (l != null) {
                val r: Response?
                var filename: String? = mHeaders["Content-Disposition"]
                if (filename != null) {
                    val fr = FileResponse()
                    r = fr
                    r.requestType = requestType

                    filename =
                            filename.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }

                    //                String filename = arrTag[1];
                    filename = filename.replace(":", ".")
                    //                    filename = filename.replace("/", "-");
                    Log.d("DownloadClient", filename)

                    try {
                        // covert response to input stream
                        val input = ByteArrayInputStream(response)

                        //Create a file on desired path and write stream data to it
                        var path = Environment.getExternalStorageDirectory()
                        path = File(path.absoluteFile.toString() + "/XSerp")
                        path.mkdir()

                        val file = File(path, filename)
                        fr.filename = file.absolutePath

                        val output = BufferedOutputStream(FileOutputStream(file))
                        val data = ByteArray(1024)

                        var total: Long = 0
                        var count: Int
                        count = input.read(data)
                        while (count != -1) {
                            total += count.toLong()
                            output.write(data, 0, count)
                            count = input.read(data)
                        }

                        output.flush()
                        output.close()

                        input.close()
                        Log.d(
                            "DownloadClient",
                            String.format(
                                "File Downloaded. Name: %s with size %s KB",
                                filename,
                                total.toDouble() / 1024
                            )
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()

                    }

                    r.statusMessage = "Success"
                } else {
                    r = VolleyClient.parseResponse(String(response), responseType)
                    if (r != null) {
                        r.requestType = requestType
                    }
                }
                r!!.extraOutput = extraOutput
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
    fun execute(l: ResponseListener, responseType: Type) {
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

                val request = object : Request<ByteArray>(method, url, this) {
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

                    override fun parseNetworkResponse(response: NetworkResponse?): com.android.volley.Response<ByteArray> {
                        if (response?.headers != null) {
                            addHeaders(response.headers)
                        }
                        if (response == null) throw AssertionError()
                        return com.android.volley.Response.success(
                            response.data,
                            HttpHeaderParser.parseCacheHeaders(response)
                        )
                    }

                    override fun deliverResponse(response: ByteArray) {
                        onResponse(response)
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
            Log.e("DownloadClient", "Exception-------------------\n")
            throw e
        }

    }
}
