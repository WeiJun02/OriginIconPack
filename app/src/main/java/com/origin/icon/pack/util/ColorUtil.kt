package com.origin.icon.pack.util

import android.graphics.Color

object ColorUtil {

    fun getColor(startColor: Int, endColor: Int,i: Float, max: Float): Int {

        //textColor
        val sR = Color.red(startColor)
        val sG = Color.green(startColor)
        val sB = Color.blue(startColor)

        //white
        val eR = Color.red(endColor)
        val eG = Color.green(endColor)
        val eB = Color.blue(endColor)

        val r = (eR - sR) * i / max
        val g = (eG - sG) * i / max
        val b = (eB - sB) * i / max

        return Color.rgb((sR + r).toInt(), (sG + g).toInt(), (sB + b).toInt())
    }
}