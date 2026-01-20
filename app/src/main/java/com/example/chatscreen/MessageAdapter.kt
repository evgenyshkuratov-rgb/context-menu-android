package com.example.chatscreen

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatscreen.databinding.ItemDateSeparatorBinding
import com.example.chatscreen.databinding.ItemMessageImageBinding
import com.example.chatscreen.databinding.ItemMessageIncomingBinding
import com.example.chatscreen.databinding.ItemMessageOutgoingBinding
import com.example.chatscreen.databinding.ItemMessageVoiceBinding

/**
 * Callback interface for message click events.
 */
interface OnMessageClickListener {
    fun onMessageClick(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View)
}

/**
 * RecyclerView adapter for displaying chat messages with different view types.
 */
class MessageAdapter(
    private val messages: List<Message>,
    private val onMessageClickListener: OnMessageClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OUTGOING_TEXT = 0
        private const val VIEW_TYPE_INCOMING_TEXT = 1
        private const val VIEW_TYPE_VOICE = 2
        private const val VIEW_TYPE_IMAGE = 3
        private const val VIEW_TYPE_DATE_SEPARATOR = 4
    }

    // Avatar color resource IDs
    private val avatarColors = intArrayOf(
        R.color.avatar_0, R.color.avatar_1, R.color.avatar_2, R.color.avatar_3,
        R.color.avatar_4, R.color.avatar_5, R.color.avatar_6, R.color.avatar_7,
        R.color.avatar_8, R.color.avatar_9, R.color.avatar_10
    )

    // Avatar drawable resource IDs
    private val avatarDrawables = intArrayOf(
        R.drawable.avatar_1, R.drawable.avatar_2
    )

    override fun getItemViewType(position: Int): Int {
        return when (messages[position]) {
            is Message.OutgoingText -> VIEW_TYPE_OUTGOING_TEXT
            is Message.IncomingText -> VIEW_TYPE_INCOMING_TEXT
            is Message.Voice -> VIEW_TYPE_VOICE
            is Message.Image -> VIEW_TYPE_IMAGE
            is Message.DateSeparator -> VIEW_TYPE_DATE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_OUTGOING_TEXT -> {
                val binding = ItemMessageOutgoingBinding.inflate(inflater, parent, false)
                OutgoingTextViewHolder(binding)
            }
            VIEW_TYPE_INCOMING_TEXT -> {
                val binding = ItemMessageIncomingBinding.inflate(inflater, parent, false)
                IncomingTextViewHolder(binding)
            }
            VIEW_TYPE_VOICE -> {
                val binding = ItemMessageVoiceBinding.inflate(inflater, parent, false)
                VoiceViewHolder(binding)
            }
            VIEW_TYPE_IMAGE -> {
                val binding = ItemMessageImageBinding.inflate(inflater, parent, false)
                ImageViewHolder(binding)
            }
            VIEW_TYPE_DATE_SEPARATOR -> {
                val binding = ItemDateSeparatorBinding.inflate(inflater, parent, false)
                DateSeparatorViewHolder(binding)
            }
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

    override fun getItemCount(): Int = messages.size

    // ViewHolder for outgoing text messages
    inner class OutgoingTextViewHolder(
        private val binding: ItemMessageOutgoingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.OutgoingText) {
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.time
            binding.ivEdit.visibility = if (message.isEdited) View.VISIBLE else View.GONE
            binding.ivCheckmark.visibility = if (message.isRead) View.VISIBLE else View.GONE

            // Click listener for context menu - use root for larger tap area
            binding.root.setOnClickListener {
                onMessageClickListener?.onMessageClick(message, message.text, true, binding.bubbleContainer)
            }
        }
    }

    // ViewHolder for incoming text messages
    inner class IncomingTextViewHolder(
        private val binding: ItemMessageIncomingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.IncomingText) {
            val context = binding.root.context
            val senderColor = ContextCompat.getColor(
                context,
                avatarColors[message.senderAvatarIndex % avatarColors.size]
            )

            binding.tvSenderName.text = message.senderName
            binding.tvSenderName.setTextColor(senderColor)
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.time

            // Set avatar
            val avatarResId = avatarDrawables.getOrElse(message.senderAvatarIndex % avatarDrawables.size) {
                R.drawable.avatar_1
            }
            binding.ivAvatar.setImageResource(avatarResId)
            binding.ivAvatar.clipToOutline = true
            binding.ivAvatar.outlineProvider = CircleOutlineProvider()

            // Click listener for context menu - use root for larger tap area
            binding.root.setOnClickListener {
                onMessageClickListener?.onMessageClick(message, message.text, false, binding.bubbleContainer)
            }
        }
    }

    // ViewHolder for voice messages
    inner class VoiceViewHolder(
        private val binding: ItemMessageVoiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.Voice) {
            val context = binding.root.context
            val senderColor = ContextCompat.getColor(
                context,
                avatarColors[message.senderAvatarIndex % avatarColors.size]
            )

            binding.tvSenderName.text = message.senderName
            binding.tvSenderName.setTextColor(senderColor)
            binding.tvDuration.text = message.duration
            binding.tvTime.text = message.time

            // Set avatar
            val avatarResId = avatarDrawables.getOrElse(message.senderAvatarIndex % avatarDrawables.size) {
                R.drawable.avatar_1
            }
            binding.ivAvatar.setImageResource(avatarResId)
            binding.ivAvatar.clipToOutline = true
            binding.ivAvatar.outlineProvider = CircleOutlineProvider()

            // Create waveform bars
            setupWaveform(message.waveformHeights)

            // Click listener for context menu - use root for larger tap area
            binding.root.setOnClickListener {
                onMessageClickListener?.onMessageClick(message, null, false, binding.bubbleContainer)
            }
        }

        private fun setupWaveform(heights: List<Int>) {
            val context = binding.root.context
            binding.waveformContainer.removeAllViews()

            val barWidthDp = 3
            val barSpacingDp = 2
            val density = context.resources.displayMetrics.density
            val barWidthPx = (barWidthDp * density).toInt()
            val barSpacingPx = (barSpacingDp * density).toInt()
            val barColor = ContextCompat.getColor(context, R.color.text_primary_30)

            for (height in heights) {
                val bar = View(context).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(barWidthPx, (height * density).toInt()).apply {
                        marginEnd = barSpacingPx
                    }
                    setBackgroundColor(barColor)
                    background.cornerRadius = 2 * density
                }
                binding.waveformContainer.addView(bar)
            }
        }
    }

    // ViewHolder for image messages
    inner class ImageViewHolder(
        private val binding: ItemMessageImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.Image) {
            val context = binding.root.context
            val senderColor = ContextCompat.getColor(
                context,
                avatarColors[message.senderAvatarIndex % avatarColors.size]
            )

            binding.tvSenderName.text = message.senderName
            binding.tvSenderName.setTextColor(senderColor)
            binding.tvTime.text = message.time
            binding.ivPhoto.setImageResource(message.imageResId)

            // Set avatar
            val avatarResId = avatarDrawables.getOrElse(message.senderAvatarIndex % avatarDrawables.size) {
                R.drawable.avatar_1
            }
            binding.ivAvatar.setImageResource(avatarResId)
            binding.ivAvatar.clipToOutline = true
            binding.ivAvatar.outlineProvider = CircleOutlineProvider()

            // Set up reactions
            val reactions = message.reactions
            if (reactions.isNullOrEmpty()) {
                binding.reactionsContainer.visibility = View.GONE
            } else {
                binding.reactionsContainer.visibility = View.VISIBLE
                val firstReaction = reactions.first()
                binding.tvEmoji.text = firstReaction.emoji
                binding.tvReactionCount.text = firstReaction.count.toString()
            }

            // Click listener for context menu - use root for larger tap area
            binding.root.setOnClickListener {
                onMessageClickListener?.onMessageClick(message, null, false, binding.bubbleContainer)
            }
        }
    }

    // ViewHolder for date separators
    inner class DateSeparatorViewHolder(
        private val binding: ItemDateSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message.DateSeparator) {
            binding.tvDate.text = message.date
        }
    }
}

// Extension property for setting corner radius on background drawable
private var android.graphics.drawable.Drawable.cornerRadius: Float
    get() = 0f
    set(value) {
        if (this is android.graphics.drawable.GradientDrawable) {
            this.cornerRadius = value
        }
    }
