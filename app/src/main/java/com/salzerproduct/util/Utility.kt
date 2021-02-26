package com.salzerproduct.util

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * @since 27/2/17.
 * General utility across the app
 */

object Utility {

    private val df = DecimalFormat("#.###")
    private val dfSingle = DecimalFormat("#.#")
    private val dfMoney = DecimalFormat("#,##,##,##,##,##,##,###.00")
    private val dfMoneyEntry = DecimalFormat("################.0")
    private val dfFiveDecimal = DecimalFormat("#,##,##,##,##,##,##,###.00000")
    fun formatDecimal(value: Double): String {
        return df.format(value)
    }

    fun formatDecimalToSingleDigit(value: Double): String {
        return dfSingle.format(value)
    }

    fun round(value: Double, places: Int): Double {
        if (places < 0) throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun formatDecimalToMoney(value: Double): String {
        return if (value == 0.0) "0.00" else dfMoney.format(value)
    }

    fun formatDecimalToMoneyFiveDecimal(value: Double): String {
        return if (value == 0.0) "0.00000" else dfFiveDecimal.format(value)
    }

    fun formatDecimalToMoneyEntry(value: Double): String {
        return if (value == 0.0) "0.0" else dfMoneyEntry.format(value)
    }

    fun isInternetAvailable(context: Context?): Boolean {
        try {
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * The build is with or after Marshmallow then this method call is must before using the permission the we need
     */
    private fun checkCallPermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.CALL_PHONE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSerp", "Requesting permission ")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            1010)
                    return false
                }
            }
        }
        return true
    }

    /**
     * The build is with or after Marshmallow then this method call is must before using the permission the we need
     */
    fun checkStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSERP", "Permission request for writing in internal storage")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            102)

                }
            }
        }
    }

    /**
     * The build is with or after Marshmallow then this method call is must before using the permission the we need
     */
    fun checkCameraPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The permission check is available only after Marshmallow
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.CAMERA)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d("XSERP", "Permission request for writing in internal storage")
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 102)
                }
            }
        }
    }

    /**
     * Opens file with system app
     *
     * @param c Context object
     * @param file File object
     */
    fun openFile(c: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Uri.fromFile(file)
            } else {
                FileProvider.getUriForFile(c, c.applicationContext.packageName + ".provider", file)
            }
            if (file.name.endsWith(".pdf")) {
                intent.setDataAndType(uri, "application/pdf")
            } else {
                intent.setDataAndType(uri, "image/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            c.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(c, "Install PDF viewer to view the file", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    fun disableSubView(linearLayout: LinearLayout, btn: Button) {
        for (view in linearLayout.touchables) {
            if (view is EditText) {
                view.isEnabled = false
                view.isFocusable = false
                view.isFocusableInTouchMode = false
            }
        }
        btn.isEnabled = false
    }

    fun disableRootView(v: View) {
        for (view in v.touchables) {
            view.isEnabled = false
            view.isFocusable = false
            view.isFocusableInTouchMode = false
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }


            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun encodeFileToBase64Binary(filePath: String): String? {
        var encodedfile: String? = null
        try {
            val file = File(filePath)
            val fileInputStreamReader = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            fileInputStreamReader.read(bytes)
            encodedfile = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch <span id="IL_AD1" class="IL_AD">block</span>
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return encodedfile
    }

    fun showError(message: String?, v: View) {
        Snackbar.make(v, message!!.toString(), Snackbar.LENGTH_LONG).show()
    }

}
