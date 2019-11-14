package com.schnell.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * @since 14/2/17.
 * Date oriented calculations are held here
 */

object DateUtil {


    //    SimpleDateFormat: hh:mm:ss, dd MMM yyyy hh:mm:ss zzz, E MMM dd yyyy     YYYYMMDD  (EEEE, dd MMM''yy)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    private val time24 = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val time12 = SimpleDateFormat("hh:mm a", Locale.US)

    private val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US)
    private val displayDateTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
    private val displayDateForStringFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    /**
     * @param ds Date String
     * @return instance of [Date]
     */
    fun parseApiDate(ds: String): Date? {
        var date: Date? = null
        try {
            date = apiDateFormat.parse(ds)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * @param ds Date String
     * @return instance of [Date]
     */
    fun parseApiDateTime(ds: String): Date? {
        var date: Date? = null
        try {
            date = apiDateTimeFormat.parse(ds)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * @param ds Date String in format dd-MM-yyyy
     * @return instance of [Date]
     */
    fun parseDisplayDate(ds: String): Date? {
        var date: Date? = null
        try {
            date = displayDateFormat.parse(ds)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * @param d Date Object
     * @return string in format yyyy-MM-dd
     */
    fun formatApiDate(d: Date): String? {
        var ds: String? = null
        try {
            ds = apiDateFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    /**
     * @param d Date Object
     * @return String in format dd-MM-yyyy
     */
    fun formatDisplayDate(d: Date): String? {
        var ds: String? = null
        try {
            ds = displayDateFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    /**
     * @param d Date Object
     * @return String in format dd-MM-yyyy
     */
    fun formatDisplayDateTime(d: Date): String? {
        var ds: String? = null
        try {
            ds = displayDateTimeFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    fun formatDisplayforStringDate(d: Date): String? {
        var ds: String? = null
        try {
            ds = displayDateForStringFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    /**
     *
     * @return converted time in format hh:mm:ss
     */
    fun getCurrentformat(): String? {
        var ds: String? = null
        try {
            val cal = Calendar.getInstance()
            ds = time12.format(cal.time)
        } catch (e: Exception) {
            // No print need
        }

        return ds
    }

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return formatter.format(date)
    }
}
