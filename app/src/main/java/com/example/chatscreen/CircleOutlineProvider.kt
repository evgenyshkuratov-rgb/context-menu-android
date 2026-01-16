package com.example.chatscreen

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * OutlineProvider that creates a circular outline for views (used for avatar images).
 */
class CircleOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val size = minOf(view.width, view.height)
        outline.setOval(0, 0, size, size)
    }
}
