package com.example.chatscreen.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Custom gesture detector for message bubbles.
 *
 * Handles three gesture types:
 * - **Long press**: Select message (multi-select mode)
 * - **Double tap**: Quick reaction (adds default reaction like ❤️)
 * - **Single tap** (delayed): Opens context menu
 *
 * Also provides touch state callbacks for visual feedback:
 * - [OnGestureListener.onTouchDown]: Finger pressed - scale bubble down
 * - [OnGestureListener.onTouchUp]: Finger lifted - scale bubble back
 *
 * The visual feedback follows the finger state, not the gesture result.
 */
class MessageGestureDetector(
    context: Context,
    private val listener: OnGestureListener
) {
    /** Listener interface for gesture events and touch state */
    interface OnGestureListener {
        /** Called when finger touches down - use for press animation */
        fun onTouchDown()

        /** Called when finger lifts up - use for release animation */
        fun onTouchUp()

        /** Called on long press (~500ms) - used for message selection */
        fun onLongPress()

        /** Called on double tap - used for quick reaction */
        fun onDoubleTap()

        /** Called on single tap (after delay) - used to open context menu */
        fun onSingleTap()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var pendingSingleTap: Runnable? = null
    private var isDoubleTapDetected = false
    private var isLongPressDetected = false
    private var isTouchDown = false

    private val gestureDetector = GestureDetector(context, GestureListener())

    init {
        gestureDetector.setIsLongpressEnabled(true)
    }

    /** Attach this gesture detector to a view */
    fun attachToView(view: View) {
        view.setOnTouchListener { _, event ->
            handleTouchState(event)
            gestureDetector.onTouchEvent(event)
            true
        }
        view.isClickable = true
        view.isFocusable = true
    }

    /** Clean up pending callbacks. Call when view is recycled or destroyed. */
    fun cleanup() {
        cancelPendingSingleTap()
        isDoubleTapDetected = false
        isLongPressDetected = false
        isTouchDown = false
    }

    private fun handleTouchState(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!isTouchDown) {
                    isTouchDown = true
                    isLongPressDetected = false
                    listener.onTouchDown()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isTouchDown) {
                    isTouchDown = false
                    listener.onTouchUp()
                }
            }
        }
    }

    private fun cancelPendingSingleTap() {
        pendingSingleTap?.let {
            handler.removeCallbacks(it)
            pendingSingleTap = null
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTapDetected || isLongPressDetected) {
                isDoubleTapDetected = false
                return true
            }

            cancelPendingSingleTap()
            pendingSingleTap = Runnable {
                if (!isDoubleTapDetected && !isLongPressDetected) {
                    listener.onSingleTap()
                }
                pendingSingleTap = null
            }
            handler.postDelayed(pendingSingleTap!!, Constants.Gesture.SINGLE_TAP_DELAY_MS)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            isDoubleTapDetected = true
            cancelPendingSingleTap()
            listener.onDoubleTap()
            handler.postDelayed({ isDoubleTapDetected = false }, Constants.Gesture.DOUBLE_TAP_RESET_MS)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            isLongPressDetected = true
            cancelPendingSingleTap()
            listener.onLongPress()
        }
    }
}
