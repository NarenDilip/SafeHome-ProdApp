package com.salzerproduct.http

import com.google.gson.annotations.SerializedName

/**
 * @since 5/4/17.
 * Response parent to receive file
 */

class FileResponse : Response() {
    @SerializedName("fr1")
    var filename: String? = null
    @SerializedName("fr2")
    var path: String? = null
}
