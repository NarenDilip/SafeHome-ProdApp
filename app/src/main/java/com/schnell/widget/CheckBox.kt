package com.schnell.widget

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import com.schnell.safehome.R

/**
 * @since 26/4/17.
 */

class CheckBox @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : android.support.v7.widget.AppCompatCheckBox(context, attrs) {

    init {
        init()
    }

    private fun init() {
        if (!isInEditMode) {
//            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        }
    }
}
