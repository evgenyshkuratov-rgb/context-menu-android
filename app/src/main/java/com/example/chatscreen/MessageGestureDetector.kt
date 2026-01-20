package com.example.chatscreen

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Custom gesture detector for message bubbles that handles:
 * - Long press: Select message (like multi-select in messengers)
 * - Double tap: Quick reaction (adds default reaction like ❤️)
 * - Single tap (delayed): Opens context menu
 *
 * Also provides touch state callbacks for visual feedback:
 * - onTouchDown: Finger pressed - scale bubble down
 * - onTouchUp: Finger lifted - scale bubble back
 *
 * The visual feedback follows the finger, not the gesture result.
 */
class MessageGestureDetector(
    context: Context,
    private val listener: OnMessageGestureListener
) {
    companion object {
        // Delay before single tap is confirmed (to wait for potential double tap)
        private const val SINGLE_TAP_DELAY_MS = 200L
    }

    interface OnMessageGestureListener {
        /** Called when finger touches down - use for scale down animation */
        fun onTouchDown()

        /** Called when finger lifts up - use for scale up animation */
        fun onTouchUp()

        /** Called on long press - used for message selection */
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

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Don't process if double tap or long press was detected
            if (isDoubleTapDetected || isLongPressDetected) {
                isDoubleTapDetected = false
                return true
            }

            // Cancel any pending single tap
            cancelPendingSingleTap()

            // Schedule single tap with delay to wait for potential double tap
            pendingSingleTap = Runnable {
                if (!isDoubleTapDetected && !isLongPressDetected) {
                    listener.onSingleTap()
                }
                pendingSingleTap = null
            }
            handler.postDelayed(pendingSingleTap!!, SINGLE_TAP_DELAY_MS)

            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            isDoubleTapDetected = true
            // Cancel pending single tap since this is a double tap
            cancelPendingSingleTap()
            listener.onDoubleTap()

            // Reset flag after a short delay
            handler.postDelayed({ isDoubleTapDetected = false }, 100)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            isLongPressDetected = true
            // Cancel pending single tap on long press
            cancelPendingSingleTap()
            listener.onLongPress()
        }

        override fun onDown(e: MotionEvent): Boolean {
            // Must return true to receive subsequent events
            return true
        }
    })

    init {
        gestureDetector.setIsLongpressEnabled(true)
    }

    private fun cancelPendingSingleTap() {
        pendingSingleTap?.let {
            handler.removeCallbacks(it)
            pendingSingleTap = null
        }
    }

    /**
     * Attach this gesture detector to a view.
     * Handles both gesture detection and touch state for visual feedback.
     */
    fun attachToView(view: View) {
        view.setOnTouchListener { _, event ->
            // Handle touch state for visual feedback
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

            // Pass to gesture detector for gesture recognition
            gestureDetector.onTouchEvent(event)

            // Return true to consume the event
            true
        }

        view.isClickable = true
        view.isFocusable = true
    }

    /**
     * Clean up any pending callbacks.
     * Call this when the view is recycled or destroyed.
     */
    fun cleanup() {
        cancelPendingSingleTap()
        isDoubleTapDetected = false
        isLongPressDetected = false
        isTouchDown = false
    }
}
