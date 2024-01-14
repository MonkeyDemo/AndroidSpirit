package com.monkeydemo.androidspirit.base

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import java.util.Random

sealed class MessageEvent(val what: Int, val screenRect: Rect) {
    abstract fun getGestureDescription(): GestureDescription
}

/**
 * 点击事件
 */
class ClickEvent(val clickX: Int, val clickY: Int, screenRect: Rect) :
    MessageEvent(what = 0x01, screenRect) {
    override fun getGestureDescription(): GestureDescription {
        val path = Path()
        path.moveTo(clickX.toFloat(), clickY.toFloat())
        val strokeDescription: GestureDescription.StrokeDescription =
            GestureDescription.StrokeDescription(path, 10, 10)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription)
        return gestureDescription.build()
    }
}

/**
 * 滑动事件
 */
class SlideEvent(
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int,
    val speed: Long,
    screenRect: Rect
) : MessageEvent(what = 0x02, screenRect) {
    override fun getGestureDescription(): GestureDescription {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        val random = Random()
        random.nextInt(60)
        path.lineTo(endX.toFloat(), endY.toFloat())
        val strokeDescription: GestureDescription.StrokeDescription =
            GestureDescription.StrokeDescription(path, 0, speed)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription)
        return gestureDescription.build()
    }
}

