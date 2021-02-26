package com.salzerproduct.util

import java.util.*
import java.util.regex.Pattern

/**
 * @since 7/3/17.
 * Commonly used methods related to Text
 */

object TextUtil {

    /**
     * @param text
     * @return empty String if it is null and trims for spaces
     */
    fun getValidString(text: String?): String {
        return text?.trim { it <= ' ' } ?: ""
    }

    /**
     * @param text
     * @param lower `true` to covert to lower case else upper case
     * @return empty String if it is null and trims for spaces
     */
    fun getValidString(text: String, lower: Boolean): String {

        return if (lower) {
            getValidString(text).toLowerCase()
        } else {
            getValidString(text).toUpperCase()
        }
    }

    fun fillHyphenForEmpty(value: String?): String {
        return if (value == null || value.trim { it <= ' ' }.isEmpty()) {
            "-"
        } else value.trim { it <= ' ' }
    }

    fun isEmailValid(email: String): Boolean {
        var isValid = false

        val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"

        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }

    fun formatIndiaCurrency(value: Double): String {
        return String.format("%s %s", Currency.getInstance("INR").symbol, Utility.formatDecimalToMoney(value))
    }

    fun formatIndiaCurrencyEntry(value: Double): String {
        return String.format("%s", Utility.formatDecimalToMoneyEntry(value))
    }

    fun formatDollorCurrency(value: Double): String {
        return String.format("$ %s", Utility.formatDecimalToMoney(value))
    }

    fun formatCurrency(symbol: String, value: Double, noNegative: Boolean? = false): String {
        var v = value
        var debit = ""
        if (noNegative!! && v < 0) {
            v *= -1
            debit = " Dr"
        }
        return String.format("%s %s%s", symbol, Utility.formatDecimalToMoney(value), debit)
    }
}
