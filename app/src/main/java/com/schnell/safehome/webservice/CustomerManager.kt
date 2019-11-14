package com.schnell.safehome.webservice

import android.content.Context
import com.schnell.safehome.BuildConfig
import com.schnell.http.VolleyClient

/**
 * @since 24/2/17.<BR></BR>
 * Common request elements can be updated in this class.<BR></BR>
 * Extend this class for all web service classes to avoid redundancy
 */

open class CustomerManager {
    companion object {

        /**
         * Construct the full url by Environment
         *
         * @param urlKey API name
         * @return API url
         */
        fun constructUrl(urlKey: String): String {
            return String.format("%s%s", BuildConfig.SERVER_URL, urlKey)
        }

        /**
         * @param c Context of request
         * @param r client instance of [VolleyClient] or its children
         */
        fun fillCommons(c: Context?, r: VolleyClient) {
            r.addParam("csrfmiddlewaretoken", "9a386d5de12900bfaca7fff92114a57e")
            r.addHeader("cookie", "csrftoken='9a386d5de12900bfaca7fff92114a57e'")
            r.addHeader("referer", "https://ilm.permisso.in")
            r.addParam("token", "TOKEN")
        }
    }
}