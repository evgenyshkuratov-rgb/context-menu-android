package com.example.chatscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatscreen.databinding.ItemDateSeparatorBinding
import com.example.chatscreen.databinding.ItemMessageImageBinding
import com.example.chatscreen.databinding.ItemMessageIncomingBinding
import com.example.chatscreen.databinding.ItemMessageOutgoingBinding
import com.example.chatscreen.databinding.ItemMessageVoiceBinding

/**
 * Listener for message gesture events.
 * - onMessageTap: Single tap (delayed) - opens context menu
 * - onMessageDoubleTap: Double tap - quick reaction
 * - onMessageLongPress: Long press - select message
 */
interface OnMessageGestureListener {
    /** Single tap - opens context menu (has small delay to differentiate from double tap) */
    fun onMessageTap(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View)

    /** Double tap - add quick reaction (e.g., ❤️) */
    fun onMessageDoubleTap(message: Message, isOutgoing: Boolean, anchorView: View)

    /** Long press - select message for multi-select */
    fun onMessageLongPress(message: Message, isOutgoing: Boolean, anchorView: View)
}

/** Animation constants for bubble press feedback */
private object BubbleAnimation {
    const val PRESSED_SCALE = 0.96f
    const val NORMAL_SCALE = 1f
    const val PRESS_DURATION = 80L
    const val RELEASE_DURATION = 150L
}

/** Animate bubble to pressed state */
private fun View.animatePress() {
    animate()
        .scaleX(BubbleAnimation.PRESSED_SCALE)
        .scaleY(BubbleAnimation.PRESSED_SCALE)
        .setDuration(BubbleAnimation.PRESS_DURATION)
        .start()
}

/** Animate bubble back to normal state */
private fun View.animateRelease() {
    animate()
        .scaleX(BubbleAnimation.NORMAL_SCALE)
        .scaleY(BubbleAnimation.NORMAL_SCALE)
        .setDuration(BubbleAnimation.RELEASE_DURATION)
        .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
        .start()
}

class MessageAdapter(
    private val messages: List<Message>,
    private val gestureListener: OnMessageGestureListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val circleOutlineProvider = CircleOutlineProvider()

    private object ViewType {
        const val OUTGOING_TEXT = 0
        const val INCOMING_TEXT = 1
        const val VOICE = 2
        const val IMAGE = 3
        const val DATE_SEPARATOR = 4
    }

    private object Avatar {
        val colors = intArrayOf(
            R.color.avatar_0, R.color.avatar_1, R.color.avatar_2, R.color.avatar_3,
            R.color.avatar_4, R.color.avatar_5, R.color.avatar_6, R.color.avatar_7,
            R.color.avatar_8, R.color.avatar_9, R.color.avatar_10
        )
        val drawables = intArrayOf(R.drawable.avatar_1, R.drawable.avatar_2)
    }

    override fun getItemViewType(position: Int) = when (messages[position]) {
        is Message.OutgoingText -> ViewType.OUTGOING_TEXT
        is Message.IncomingText -> ViewType.INCOMING_TEXT
        is Message.Voice -> ViewType.VOICE
        is Message.Image -> ViewType.IMAGE
        is Message.DateSeparator -> ViewType.DATE_SEPARATOR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.OUTGOING_TEXT -> OutgoingTextViewHolder(ItemMessageOutgoingBinding.inflate(inflater, parent, false))
            ViewType.INCOMING_TEXT -> IncomingTextViewHolder(ItemMessageIncomingBinding.inflate(inflater, parent, false))
            ViewType.VOICE -> VoiceViewHolder(ItemMessageVoiceBinding.inflate(inflater, parent, false))
            ViewType.IMAGE -> ImageViewHolder(ItemMessageImageBinding.inflate(inflater, parent, false))
            ViewType.DATE_SEPARATOR -> DateSeparatorViewHolder(ItemDateSeparatorBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val message = messages[position]) {
            is Message.OutgoingText -> (holder as OutgoingTextViewHolder).bind(message)
            is Message.IncomingText -> (holder as IncomingTextViewHolder).bind(message)
            is Message.Voice -> (holder as VoiceViewHolder).bind(message)
            is Message.Image -> (holder as ImageViewHolder).bind(message)
            is Message.DateSeparator -> (holder as DateSeparatorViewHolder).bind(message)
        }
    }

    override fun getItemCount() = messages.size

    private fun setupAvatar(imageView: ImageView, avatarIndex: Int) {
        imageView.setImageResource(Avatar.drawables[avatarIndex % Avatar.drawables.size])
        imageView.clipToOutline = true
        imageView.outlineProvider = circleOutlineProvider
    }

    private fun getSenderColor(context: Context, avatarIndex: Int): Int {
        return ContextCompat.getColor(context, Avatar.colors[avatarIndex % Avatar.colors.size])
    }

    private fun setupSender(nameView: TextView, avatarView: ImageView, name: String, avatarIndex: Int) {
        nameView.text = name
        nameView.setTextColor(getSenderColor(nameView.context, avatarIndex))
        setupAvatar(avatarView, avatarIndex)
    }

    inner class OutgoingTextViewHolder(
        private val binding: ItemMessageOutgoingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var gestureDetector: MessageGestureDetector? = null

        fun bind(message: Message.OutgoingText) {
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.time
            binding.ivEdit.visibility = if (message.isEdited) View.VISIBLE else View.GONE
            binding.ivCheckmark.visibility = if (message.isRead) View.VISIBLE else View.GONE

            gestureDetector?.cleanup()
            gestureDetector = MessageGestureDetector(
                binding.root.context,
                object : MessageGestureDetector.OnMessageGestureListener {
                    override fun onTouchDown() {
                        binding.bubbleContainer.animatePress()
                    }
                    override fun onTouchUp() {
                        binding.bubbleContainer.animateRelease()
                    }
                    override fun onSingleTap() {
                        gestureListener?.onMessageTap(message, message.text, true, binding.bubbleContainer)
                    }
                    override fun onDoubleTap() {
                        gestureListener?.onMessageDoubleTap(message, true, binding.bubbleContainer)
                    }
                    override fun onLongPress() {
                        gestureListener?.onMessageLongPress(message, true, binding.bubbleContainer)
                    }
                }
            ).also { it.attachToView(binding.root) }
        }
    }

    inner class IncomingTextViewHolder(
        private val binding: ItemMessageIncomingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var gestureDetector: MessageGestureDetector? = null

        fun bind(message: Message.IncomingText) {
            setupSender(binding.tvSenderName, binding.ivAvatar, message.senderName, message.senderAvatarIndex)
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.time

            gestureDetector?.cleanup()
            gestureDetector = MessageGestureDetector(
                binding.root.context,
                object : MessageGestureDetector.OnMessageGestureListener {
                    override fun onTouchDown() {
                        binding.bubbleContainer.animatePress()
                    }
                    override fun onTouchUp() {
                        binding.bubbleContainer.animateRelease()
                    }
                    override fun onSingleTap() {
                        gestureListener?.onMessageTap(message, message.text, false, binding.bubbleContainer)
                    }
                    override fun onDoubleTap() {
                        gestureListener?.onMessageDoubleTap(message, false, binding.bubbleContainer)
                    }
                    override fun onLongPress() {
                        gestureListener?.onMessageLongPress(message, false, binding.bubbleContainer)
                    }
                }
            ).also { it.attachToView(binding.root) }
        }
    }

    inner class VoiceViewHolder(
        private val binding: ItemMessageVoiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val density = binding.root.context.resources.displayMetrics.density
        private val barWidthPx = (3 * density).toInt()
        private val barSpacingPx = (2 * density).toInt()
        private val barColor = ContextCompat.getColor(binding.root.context, R.color.text_primary_30)
        private var gestureDetector: MessageGestureDetector? = null

        fun bind(message: Message.Voice) {
            setupSender(binding.tvSenderName, binding.ivAvatar, message.senderName, message.senderAvatarIndex)
            binding.tvDuration.text = message.duration
            binding.tvTime.text = message.time
            setupWaveform(message.waveformHeights)

            gestureDetector?.cleanup()
            gestureDetector = MessageGestureDetector(
                binding.root.context,
                object : MessageGestureDetector.OnMessageGestureListener {
                    override fun onTouchDown() {
                        binding.bubbleContainer.animatePress()
                    }
                    override fun onTouchUp() {
                        binding.bubbleContainer.animateRelease()
                    }
                    override fun onSingleTap() {
                        gestureListener?.onMessageTap(message, null, false, binding.bubbleContainer)
                    }
                    override fun onDoubleTap() {
                        gestureListener?.onMessageDoubleTap(message, false, binding.bubbleContainer)
                    }
                    override fun onLongPress() {
                        gestureListener?.onMessageLongPress(message, false, binding.bubbleContainer)
                    }
                }
            ).also { it.attachToView(binding.root) }
        }

        private fun setupWaveform(heights: List<Int>) {
            binding.waveformContainer.removeAllViews()
            heights.forEach { height ->
                View(binding.root.context).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(barWidthPx, (height * density).toInt()).apply {
                        marginEnd = barSpacingPx
                    }
                    setBackgroundColor(barColor)
                    binding.waveformContainer.addView(this)
                }
            }
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemMessageImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var gestureDetector: MessageGestureDetector? = null

        fun bind(message: Message.Image) {
            setupSender(binding.tvSenderName, binding.ivAvatar, message.senderName, message.senderAvatarIndex)
            binding.tvTime.text = message.time
            binding.ivPhoto.setImageResource(message.imageResId)

            message.reactions?.firstOrNull()?.let { reaction ->
                binding.reactionsContainer.visibility = View.VISIBLE
                binding.tvEmoji.text = reaction.emoji
                binding.tvReactionCount.text = reaction.count.toString()
            } ?: run {
                binding.reactionsContainer.visibility = View.GONE
            }

            gestureDetector?.cleanup()
            gestureDetector = MessageGestureDetector(
                binding.root.context,
                object : MessageGestureDetector.OnMessageGestureListener {
                    override fun onTouchDown() {
                        binding.bubbleContainer.animatePress()
                    }
                    override fun onTouchUp() {
                        binding.bubbleContainer.animateRelease()
                    }
                    override fun onSingleTap() {
                        gestureListener?.onMessageTap(message, null, false, binding.bubbleContainer)
                    }
                    override fun onDoubleTap() {
                        gestureListener?.onMessageDoubleTap(message, false, binding.bubbleContainer)
                    }
                    override fun onLongPress() {
                        gestureListener?.onMessageLongPress(message, false, binding.bubbleContainer)
                    }
                }
            ).also { it.attachToView(binding.root) }
        }
    }

    inner class DateSeparatorViewHolder(
        private val binding: ItemDateSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.DateSeparator) {
            binding.tvDate.text = message.date
        }
    }
}
