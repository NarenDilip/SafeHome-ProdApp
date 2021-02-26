package com.salzerproduct.util

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.annotations.SerializedName

class Exclude : ExclusionStrategy {

    override fun shouldSkipClass(arg0: Class<*>): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun shouldSkipField(field: FieldAttributes): Boolean {
        val ns = field.getAnnotation(SerializedName::class.java)
        return if (ns != null) false else true
    }
}

