package com.example.chatscreen.ui.contextmenu

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.example.chatscreen.R
import com.example.chatscreen.databinding.BottomSheetContextMenuBinding
import com.example.chatscreen.util.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet context menu for message actions.
 *
 * Features:
 * - Reaction bar with quick emoji reactions
 * - Action list (reply, forward, copy, delete, etc.)
 * - Multiple animation styles ([MenuAnimationStyle])
 *
 * Usage:
 * ```kotlin
 * ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
 *     .show(supportFragmentManager, "ContextMenuBottomSheet")
 * ```
 */
class ContextMenuBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetContextMenuBinding? = null
    private val binding get() = _binding!!

    private var messageText: String? = null
    private var isOutgoing: Boolean = false
    private var animationStyle = MenuAnimationStyle.TELEGRAM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener {
                findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.background =
                    ColorDrawable(Color.TRANSPARENT)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            messageText = args.getString(ARG_MESSAGE_TEXT)
            isOutgoing = args.getBoolean(ARG_IS_OUTGOING, false)
            animationStyle = MenuAnimationStyle.valueOf(
                args.getString(ARG_ANIMATION_STYLE, MenuAnimationStyle.TELEGRAM.name)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetContextMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupReactions()
        setupActions()
        view.post { animateEntrance() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // region Animation

    private fun animateEntrance() {
        when (animationStyle) {
            MenuAnimationStyle.TELEGRAM -> animateTelegram()
            MenuAnimationStyle.IMESSAGE -> animateIMessage()
            MenuAnimationStyle.WHATSAPP -> animateWhatsApp()
        }
    }

    private fun animateTelegram() {
        binding.reactionBar.apply {
            alpha = 0f
            translationY = 20f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(Constants.Animation.SLOW_MS)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateIMessage() {
        binding.reactionBar.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(Constants.Animation.MEDIUM_MS)
                .setInterpolator(OvershootInterpolator(Constants.Animation.OVERSHOOT_LIGHT))
                .start()
        }
    }

    private fun animateWhatsApp() {
        // Staggered reaction pop-in
        val reactions = listOf(
            binding.reaction1, binding.reaction2, binding.reaction3,
            binding.reaction4, binding.reaction5, binding.btnAddReaction
        )
        reactions.forEachIndexed { index, view ->
            view.scaleX = 0f
            view.scaleY = 0f
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(Constants.Animation.STANDARD_MS)
                .setStartDelay(index * Constants.Animation.STAGGER_DELAY_MEDIUM_MS)
                .setInterpolator(OvershootInterpolator(Constants.Animation.OVERSHOOT_TENSION))
                .start()
        }

        // Staggered action slide-in
        val actions = listOf(
            binding.actionReply, binding.actionForward, binding.actionComment,
            binding.actionPin, binding.actionCopy, binding.actionAddLabel,
            binding.actionSave, binding.actionViewed, binding.actionSelect, binding.actionDelete
        )
        actions.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = Constants.ContextMenu.SLIDE_OFFSET_X
            view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(Constants.Animation.STANDARD_MS)
                .setStartDelay(Constants.ContextMenu.REACTION_DELAY_START_MS + index * Constants.Animation.STAGGER_DELAY_SHORT_MS)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    // endregion

    // region Setup

    private fun setupReactions() {
        val reactions = mapOf(
            binding.reaction1 to "👍",
            binding.reaction2 to "👎",
            binding.reaction3 to "🔥",
            binding.reaction4 to "👌",
            binding.reaction5 to "🤔"
        )
        reactions.forEach { (view, emoji) ->
            view.setOnClickListener { dismissWithToast(getString(R.string.reaction_format, emoji)) }
        }
        binding.btnAddReaction.setOnClickListener { dismissWithToast(getString(R.string.add_reaction)) }
    }

    private fun setupActions() {
        with(binding) {
            actionReply.setOnClickListener { dismissWithToast(getString(R.string.action_reply)) }
            actionForward.setOnClickListener { dismissWithToast(getString(R.string.action_forward)) }
            actionComment.setOnClickListener { dismissWithToast(getString(R.string.action_comment)) }
            actionPin.setOnClickListener { dismissWithToast(getString(R.string.action_pin)) }
            actionCopy.setOnClickListener { copyToClipboard() }
            actionAddLabel.setOnClickListener { dismissWithToast(getString(R.string.action_add_label)) }
            actionSave.setOnClickListener { dismissWithToast(getString(R.string.action_save)) }
            actionViewed.setOnClickListener { dismissWithToast(getString(R.string.action_viewed)) }
            actionSelect.setOnClickListener { dismissWithToast(getString(R.string.action_select)) }
            actionDelete.setOnClickListener { dismissWithToast(getString(R.string.action_delete)) }
        }
    }

    // endregion

    // region Actions

    private fun copyToClipboard() {
        messageText?.let { text ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            dismissWithToast(getString(R.string.copied))
        } ?: dismissWithToast(getString(R.string.action_copy))
    }

    private fun dismissWithToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    // endregion

    companion object {
        private const val ARG_MESSAGE_TEXT = "message_text"
        private const val ARG_IS_OUTGOING = "is_outgoing"
        private const val ARG_ANIMATION_STYLE = "animation_style"

        fun newInstance(
            messageText: String?,
            isOutgoing: Boolean,
            animationStyle: MenuAnimationStyle = MenuAnimationStyle.TELEGRAM
        ) = ContextMenuBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE_TEXT, messageText)
                putBoolean(ARG_IS_OUTGOING, isOutgoing)
                putString(ARG_ANIMATION_STYLE, animationStyle.name)
            }
        }
    }
}
