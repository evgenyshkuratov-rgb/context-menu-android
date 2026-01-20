package com.example.chatscreen.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * ViewOutlineProvider that creates a circular outline for views.
 * Used primarily for avatar images to create circular clipping.
 *
 * Usage:
 * ```kotlin
 * imageView.clipToOutline = true
 * imageView.outlineProvider = CircleOutlineProvider()
 * ```
 */
class CircleOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val size = minOf(view.width, view.height)
        outline.setOval(0, 0, size, size)
    }
}
