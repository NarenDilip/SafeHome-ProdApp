package com.salzerproduct.widget

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.salzerproduct.safehome.R

/**
 * Created by krishna on 26/2/16.
 * Customised for setting Custom Font
 */
class TextView(context: Context, attrs: AttributeSet?, defStyle: Int) : AppCompatTextView(context, attrs) {
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
            typeface = ResourcesCompat.getFont(context, R.font.andika)
        }
    }
}

