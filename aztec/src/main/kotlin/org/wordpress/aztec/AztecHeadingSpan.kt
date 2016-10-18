package org.wordpress.aztec

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class AztecHeadingSpan(val mHeading: AztecHeadingSpan.Heading) : MetricAffectingSpan() {
    companion object {
        private val SCALE_H1: Float = 2.0f
        private val SCALE_H2: Float = 1.8f
        private val SCALE_H3: Float = 1.6f
        private val SCALE_H4: Float = 1.4f
        private val SCALE_H5: Float = 1.2f
        private val SCALE_H6: Float = 1.0f
    }

    enum class Heading constructor(internal val mScale: Float) {
        H1(SCALE_H1),
        H2(SCALE_H2),
        H3(SCALE_H3),
        H4(SCALE_H4),
        H5(SCALE_H5),
        H6(SCALE_H6)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.textSize *= mHeading.mScale
        textPaint.isFakeBoldText = true
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.textSize *= mHeading.mScale
    }

    fun getTag(): String {
        when (mHeading.mScale) {
            SCALE_H1 -> return "h1"
            SCALE_H2 -> return "h2"
            SCALE_H3 -> return "h3"
            SCALE_H4 -> return "h4"
            SCALE_H5 -> return "h5"
            SCALE_H6 -> return "h6"
            else -> return "p"
        }
    }
}