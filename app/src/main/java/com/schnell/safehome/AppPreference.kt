package com.schnell.safehome

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.schnell.http.Response
import java.lang.reflect.Type

/**
 * @since 27/2/17.
 * General preference works
 */

object AppPreference {

    internal interface Key {
        companion object {
            const val NOTIFICATION_COUNT = "notification_count"
            const val accessToken = "accessToken"
            const val refreshToken = "refreshToken"
            const val loginUser = "loginUser"
        }
    }

    private fun getEditor(c: Context?): SharedPreferences.Editor {
        return getPreference(c).edit()
    }

    private fun getPreference(c: Context?): SharedPreferences {
        return c!!.getSharedPreferences(
                "com.schnell.safehome.preference", Context.MODE_PRIVATE)
    }


    /**
     * @param c     Context
     * @param key   key
     * @param value value
     */
    fun put(c: Context, key: String, value: String) {
        val editor = getEditor(c)
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * @param c   Context
     * @param key key
     * @param dv  default value
     */
    operator fun get(c: Context, key: String, dv: String): String? {
        return getPreference(c).getString(key, dv)
    }


    /**
     * @param c     Context
     * @param key   key
     * @param value value
     */
    fun put(c: Context, key: String, value: Int) {
        val editor = getEditor(c)
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * @param c   Context
     * @param key key
     * @param dv  default value
     */
    operator fun get(c: Context, key: String, dv: Int): Int {
        return getPreference(c).getInt(key, dv)
    }

    fun clearAll(c: Context) {
        val editor = getEditor(c)
        editor.clear()
        editor.apply()
    }

    fun clear(c: Context, key: String) {
        val editor = getEditor(c)
        editor.remove(key)
        editor.apply()
    }


    /**
     * Store Gson serializable data
     *
     * @param c      Context
     * @param key    Key
     * @param data Type of [Response] which is Gson Serializable
     */
    fun storeGson(c: Context, key: String, data: Response?) {
        val e = getEditor(c)
        try {
            e.putString(key, Gson().toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            e.apply()
        }
    }

    /**
     * Get Gson serializable data object of given type
     *
     * @param c Context
     * @param key Key
     * @param type [Type] to be returned
     * @return type given
     */
    fun getGson(c: Context, key: String, type: Type): Response? {
        try {
            val s = getPreference(c).getString(key, null)
            return if (s == null) s else Gson().fromJson(s, type)
        } catch (e: Exception) {
            e.printStackTrace()
            clear(c, key)
        }
        return null
    }
}
