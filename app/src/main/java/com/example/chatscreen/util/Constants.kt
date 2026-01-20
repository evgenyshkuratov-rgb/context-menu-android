package com.example.chatscreen.util

/**
 * Centralized constants for the chat screen app.
 * Mirrors the iOS Constants.swift for consistency across platforms.
 */
object Constants {

    /** Animation timing and values */
    object Animation {
        const val QUICK_MS = 80L
        const val STANDARD_MS = 150L
        const val MEDIUM_MS = 200L
        const val SLOW_MS = 250L
        const val LONG_MS = 400L

        const val STAGGER_DELAY_SHORT_MS = 20L
        const val STAGGER_DELAY_MEDIUM_MS = 30L

        const val OVERSHOOT_TENSION = 1.5f
        const val OVERSHOOT_LIGHT = 1.2f
    }

    /** Bubble press feedback animation */
    object BubbleAnimation {
        const val PRESSED_SCALE = 0.96f
        const val NORMAL_SCALE = 1f
        const val PRESS_DURATION_MS = 80L
        const val RELEASE_DURATION_MS = 150L
    }

    /** Gesture detection timing */
    object Gesture {
        const val SINGLE_TAP_DELAY_MS = 200L
        const val DOUBLE_TAP_RESET_MS = 100L
    }

    /** Haptic feedback */
    object Haptic {
        const val TAP_DURATION_MS = 8L
        const val TAP_AMPLITUDE = 25
        const val DOUBLE_TAP_DURATION_MS = 5L
        const val DOUBLE_TAP_AMPLITUDE = 20
        const val LONG_PRESS_DURATION_MS = 15L
        const val LONG_PRESS_AMPLITUDE = 50
        const val DESELECT_DURATION_MS = 5L
        const val DESELECT_AMPLITUDE = 20
    }

    /** Quick reaction animation */
    object QuickReaction {
        const val EMOJI_SIZE_SP = 48f
        const val INITIAL_SCALE = 0.3f
        const val FINAL_SCALE = 1.5f
        const val FLOAT_TRANSLATION_Y = -100f
        const val FADE_IN_DURATION_MS = 150L
        const val HOLD_DELAY_MS = 200L
        const val FADE_OUT_DURATION_MS = 400L
        const val POSITION_OFFSET_PX = 60f
        const val DEFAULT_EMOJI = "❤️"
    }

    /** Message selection state */
    object Selection {
        const val OVERLAY_COLOR = "#2A2196F3" // Primary blue 16% opacity
        const val CHECKMARK_COLOR = "#2196F3" // Primary blue
        const val CHECKMARK_SIZE_DP = 28
        const val CHECKMARK_PADDING_DP = 6
        const val BUBBLE_CORNER_RADIUS_DP = 14
        const val APPEAR_DURATION_MS = 200L
        const val DISAPPEAR_DURATION_MS = 150L
        const val CHECKMARK_APPEAR_DURATION_MS = 250L
        const val CHECKMARK_INITIAL_SCALE = 0.5f
    }

    /** Context menu bottom sheet */
    object ContextMenu {
        const val REACTION_DELAY_START_MS = 100L
        const val SLIDE_OFFSET_X = -30f
    }
}
