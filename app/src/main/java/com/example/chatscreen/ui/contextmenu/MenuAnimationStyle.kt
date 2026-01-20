package com.example.chatscreen.ui.contextmenu

/**
 * Animation styles for the context menu bottom sheet.
 * Each style provides a different visual entrance animation.
 */
enum class MenuAnimationStyle {
    /** Telegram-style: Fade in with slight upward translation */
    TELEGRAM,

    /** iMessage-style: Scale up with overshoot from center */
    IMESSAGE,

    /** WhatsApp-style: Staggered pop-in for reactions, slide-in for actions */
    WHATSAPP
}
