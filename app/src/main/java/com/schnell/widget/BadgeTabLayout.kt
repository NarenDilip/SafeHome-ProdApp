package com.schnell.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.schnell.safehome.R

/**
 * Created by eneim on 9/2/15.
 *
 *
 * A custom TabLayout with Builder support for customizing Tabs.
 *
 *
 * Since every Tab must be attached to a parent TabLayout, it's reasonable to have an inner
 * Builder for every Tab in TabLayout, but not a global TabLayout#Builder. Builder is not strictly
 * follow Builder design pattern.
 */
class BadgeTabLayout : TabLayout {
    private val mTabBuilders = SparseArray<Builder>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun with(position: Int): Builder {
        val tab = getTabAt(position)
        return with(tab)
    }

    /**
     * Apply a builder for this tab.
     *
     * @param tab for which we create a new builder or retrieve its builder if existed.
     * @return the required Builder.
     */
    fun with(tab: TabLayout.Tab?): Builder {
        if (tab == null) {
            throw IllegalArgumentException("Tab must not be null")
        }
        // clear badge count
        mTabBuilders.clear()
        var builder: Builder? = mTabBuilders.get(tab.position)
        if (builder == null) {
            builder = Builder(this, tab)
            mTabBuilders.put(tab.position, builder)
        }

        return builder
    }

    class Builder
    /**
     * This construct take a TabLayout parent to have its context and other attributes sets. And
     * the tab whose icon will be updated.
     *
     * @param parent TabLayout
     * @param mTab    the tab to be build with badge
     */
    constructor(parent: TabLayout, private val mTab: TabLayout.Tab) {

        private val mView: View?
        private val mContext: Context = parent.context
        private var mBadgeTextView: TextView? = null
        private var mTitleTextView: TextView? = null
        private var mIconView: ImageView? = null
        private var mIconDrawable: Drawable? = null
        private var mIconColorFilter: Int? = null
        private var mBadgeCount = Integer.MIN_VALUE

        private var mHasBadge = false
        private var isFixedWidth = false

        init {
            // initialize current tab's custom view.
            if (mTab.customView != null) {
                this.mView = mTab.customView
            } else {
                this.mView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.tab_badge_view, parent, false)
            }

            if (mView != null) {
                this.mIconView = mView.findViewById(R.id.tab_icon)
                this.mBadgeTextView = mView.findViewById(R.id.tab_badge)
                this.mTitleTextView = mView.findViewById(R.id.title)
            }

            if (this.mBadgeTextView != null) {
                this.mHasBadge = mBadgeTextView!!.visibility == View.VISIBLE
                try {
                    if (mBadgeTextView!!.text.toString().isEmpty()) {
                        this.mBadgeCount = 0
                    } else {
                        this.mBadgeCount = Integer.parseInt(mBadgeTextView!!.text.toString())
                    }
                } catch (er: NumberFormatException) {
                    er.printStackTrace()
                    this.mBadgeCount = INVALID_NUMBER
                }

            }

            if (this.mIconView != null) {
                mIconDrawable = mIconView!!.drawable
            }
        }

        /**
         * The related Tab is about to have a badge
         *
         * @return this builder
         */
        fun hasBadge(): Builder {
            mHasBadge = true
            return this
        }

        fun getTextView(): TextView {
            return mTitleTextView!!
        }

        /**
         * The related Tab is not about to have a badge
         *
         * @return this builder
         */
        fun noBadge(): Builder {
            mHasBadge = false
            return this
        }

        /**
         * Dynamically set the availability of tab's badge
         *
         * @param hasBadge true to show badge
         * @return this builder
         */
        // This method is used for DEBUG purpose only
        /*hide*/
        fun badge(hasBadge: Boolean): Builder {
            mHasBadge = hasBadge
            return this
        }

        /**
         * Set icon custom drawable by Resource ID;
         *
         * @param drawableRes Drawable id
         * @return this builder
         */
        fun icon(drawableRes: Int): Builder {
            mIconDrawable = ContextCompat.getDrawable(mContext, drawableRes)
            return this
        }

        /**
         * Set icon custom drawable by Drawable Object
         *
         * @param drawable
         * @return this builder
         */
        fun icon(drawable: Drawable): Builder {
            mIconDrawable = drawable
            return this
        }

        /**
         * Set drawable color. Use this when user wants to change drawable's color filter
         *
         * @param color
         * @return this builder
         */
        fun iconColor(color: Int?): Builder {
            mIconColorFilter = color
            return this
        }

        /**
         * increase current badge by 1
         *
         * @return this builder
         */
        fun increase(): Builder {
            mBadgeCount = if (mBadgeTextView == null)
                INVALID_NUMBER
            else
                Integer.parseInt(mBadgeTextView!!.text.toString()) + 1
            return this
        }

        /**
         * decrease current badge by 1
         *
         * @return this builder
         */
        fun decrease(): Builder {
            mBadgeCount = if (mBadgeTextView == null)
                INVALID_NUMBER
            else
                Integer.parseInt(mBadgeTextView!!.text.toString()) - 1
            return this
        }

        fun removebadgeCount(): Builder {
            mBadgeCount = -1

            return this
        }

        /**
         * set badge count
         *
         * @param count expected badge number
         * @return this builder
         */
        fun badgeCount(count: Int): Builder {
            mBadgeCount = count
            return this
        }

        fun isFixedWidth(isFixed: Boolean): Builder {
            isFixedWidth = isFixed
            return this
        }

        /**
         * Build the current Tab icon's custom view <BR></BR>
         * Value <=0 wont display
         */
        fun build() {
            try {
                if (mView == null) {
                    return
                }

                if (mTitleTextView != null) {
                    if (mTab.isSelected) {
                        mTitleTextView!!.setTextColor(ContextCompat.getColor(mContext, R.color.tabSelected))
                    } else {
                        mTitleTextView!!.setTextColor(ContextCompat.getColor(mContext, R.color.tabTextColor))
                    }

                    if (mTab.text != null) {
                        mTitleTextView!!.text = mTab.text!!.toString()
                    }
                }
                mHasBadge = mBadgeCount > 0
                // update badge counter
                if (mBadgeTextView != null) {
                    mBadgeTextView!!.text = formatBadgeNumber(mBadgeCount)

                    if (mHasBadge) {
                        mBadgeTextView!!.visibility = View.VISIBLE
                    } else {
                        // set to View#INVISIBLE to not screw up the layout
                        mBadgeTextView!!.visibility = View.GONE
                    }
                }

                // update icon drawable
                if (mIconView != null && mIconDrawable != null) {
                    mIconView!!.setImageDrawable(mIconDrawable!!.mutate())
                    // be careful if you modify this. make sure your result matches your expectation.
                    if (mIconColorFilter != null) {
                        mIconDrawable!!.setColorFilter(mIconColorFilter!!, PorterDuff.Mode.MULTIPLY)
                    }
                }
                if (isFixedWidth) {
                    val params = mView.layoutParams
                    params.width = 240
                    mView.layoutParams = params
                }
                mTab.customView = mView
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        companion object {

            /**
             * This badge widget must not support this value.
             */
            private const val INVALID_NUMBER = Integer.MIN_VALUE
        }
    }

    companion object {

        /**
         * This format must follow User's badge policy.
         *
         * @param value of current badge
         * @return corresponding badge number. TextView need to be passed by a String/CharSequence
         */
        private fun formatBadgeNumber(value: Int): String {
            return if (value < 0) {
                "-$value"
            } else Integer.toString(value)
        }
    }
}