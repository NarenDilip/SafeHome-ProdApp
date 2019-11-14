package com.schnell.widget

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import com.schnell.safehome.R

/**
 * @since 6/5/16.
 */
class EditText(context: Context, attrs: AttributeSet?, defStyle: Int) : AppCompatEditText(context, attrs) {
    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    init {
        init()
    }

    private fun init() {
        if (!isInEditMode) {
            typeface = ResourcesCompat.getFont(context, R.font.helvetica_font)
        }
    }
}

