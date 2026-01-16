package com.example.chatscreen

/**
 * Sealed class representing different types of messages in a chat.
 */
sealed class Message {

    /**
     * Outgoing text message (sent by the current user).
     */
    data class OutgoingText(
        val text: String,
        val time: String,
        val isEdited: Boolean = false,
        val isRead: Boolean = true
    ) : Message()

    /**
     * Incoming text message from another user.
     */
    data class IncomingText(
        val senderName: String,
        val senderAvatarIndex: Int,
        val text: String,
        val time: String
    ) : Message()

    /**
     * Voice message from another user.
     */
    data class Voice(
        val senderName: String,
        val senderAvatarIndex: Int,
        val duration: String,
        val time: String,
        val waveformHeights: List<Int>
    ) : Message()

    /**
     * Image message with optional reactions.
     */
    data class Image(
        val senderName: String,
        val senderAvatarIndex: Int,
        val imageResId: Int,
        val time: String,
        val reactions: List<Reaction>? = null
    ) : Message()

    /**
     * Date separator (e.g., "Today", "24 ноября").
     */
    data class DateSeparator(
        val date: String
    ) : Message()
}

/**
 * Represents a reaction on a message.
 */
data class Reaction(
    val emoji: String,
    val count: Int
)
