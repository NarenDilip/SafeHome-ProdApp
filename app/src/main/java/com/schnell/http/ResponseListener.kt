package com.schnell.http

/**
 * @since 23/2/17.
 * Common listener interface
 */

interface ResponseListener {

    /**
     * @param r - The model class that is passed on the parser
     */
    fun onResponse(r: Response?)


}
