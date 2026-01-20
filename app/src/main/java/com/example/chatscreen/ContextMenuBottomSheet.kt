package com.example.chatscreen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.chatscreen.databinding.BottomSheetContextMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

enum class MenuAnimationStyle {
    TELEGRAM,   // Smooth slide with fade
    IMESSAGE,   // Spring physics
    WHATSAPP    // Staggered items
}

class ContextMenuBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = ColorDrawable(Color.TRANSPARENT)
        }
        return dialog
    }

    private var _binding: BottomSheetContextMenuBinding? = null
    private val binding get() = _binding!!

    private var messageText: String? = null
    private var isOutgoing: Boolean = false
    private var animationStyle: MenuAnimationStyle = MenuAnimationStyle.TELEGRAM

    companion object {
        private const val ARG_MESSAGE_TEXT = "message_text"
        private const val ARG_IS_OUTGOING = "is_outgoing"
        private const val ARG_ANIMATION_STYLE = "animation_style"

        fun newInstance(
            messageText: String?,
            isOutgoing: Boolean,
            animationStyle: MenuAnimationStyle = MenuAnimationStyle.TELEGRAM
        ): ContextMenuBottomSheet {
            return ContextMenuBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE_TEXT, messageText)
                    putBoolean(ARG_IS_OUTGOING, isOutgoing)
                    putString(ARG_ANIMATION_STYLE, animationStyle.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            messageText = it.getString(ARG_MESSAGE_TEXT)
            isOutgoing = it.getBoolean(ARG_IS_OUTGOING, false)
            animationStyle = MenuAnimationStyle.valueOf(
                it.getString(ARG_ANIMATION_STYLE, MenuAnimationStyle.TELEGRAM.name)
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

        // Apply entry animation based on style
        view.post {
            when (animationStyle) {
                MenuAnimationStyle.TELEGRAM -> animateTelegramStyle()
                MenuAnimationStyle.IMESSAGE -> animateIMessageStyle()
                MenuAnimationStyle.WHATSAPP -> animateWhatsAppStyle()
            }
        }
    }

    private fun animateTelegramStyle() {
        // Smooth fade-in for reaction bar with slight slide up
        binding.reactionBar.apply {
            alpha = 0f
            translationY = 20f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    private fun animateIMessageStyle() {
        // Spring animation for reaction bar - bouncy entrance
        binding.reactionBar.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f

            animate().alpha(1f).setDuration(150).start()

            SpringAnimation(this, SpringAnimation.SCALE_X, 1f).apply {
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                start()
            }
            SpringAnimation(this, SpringAnimation.SCALE_Y, 1f).apply {
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                start()
            }
        }
    }

    private fun animateWhatsAppStyle() {
        // Staggered animation for each reaction emoji
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
                .setDuration(200)
                .setStartDelay(index * 40L)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
        }

        // Staggered animation for action items
        val actions = listOf(
            binding.actionReply, binding.actionForward, binding.actionComment,
            binding.actionPin, binding.actionCopy, binding.actionAddLabel,
            binding.actionSave, binding.actionViewed, binding.actionSelect,
            binding.actionDelete
        )

        actions.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -30f
            view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(200)
                .setStartDelay(150 + index * 30L)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    private fun setupReactions() {
        val reactions = listOf(
            binding.reaction1 to "👍",
            binding.reaction2 to "👎",
            binding.reaction3 to "🔥",
            binding.reaction4 to "👌",
            binding.reaction5 to "🤔"
        )

        reactions.forEach { (view, emoji) ->
            view.setOnClickListener {
                showToast("Реакция: $emoji")
                dismiss()
            }
        }

        binding.btnAddReaction.setOnClickListener {
            showToast("Добавить реакцию")
            dismiss()
        }
    }

    private fun setupActions() {
        binding.actionReply.setOnClickListener {
            showToast("Ответить")
            dismiss()
        }

        binding.actionForward.setOnClickListener {
            showToast("Переслать")
            dismiss()
        }

        binding.actionComment.setOnClickListener {
            showToast("Прокомментировать")
            dismiss()
        }

        binding.actionPin.setOnClickListener {
            showToast("Закрепить")
            dismiss()
        }

        binding.actionCopy.setOnClickListener {
            messageText?.let { text ->
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("message", text)
                clipboard.setPrimaryClip(clip)
                showToast("Скопировано")
            } ?: showToast("Копировать")
            dismiss()
        }

        binding.actionAddLabel.setOnClickListener {
            showToast("Добавить метку")
            dismiss()
        }

        binding.actionSave.setOnClickListener {
            showToast("В сохраненное")
            dismiss()
        }

        binding.actionViewed.setOnClickListener {
            showToast("Просмотрено")
            dismiss()
        }

        binding.actionSelect.setOnClickListener {
            showToast("Выбрать")
            dismiss()
        }

        binding.actionDelete.setOnClickListener {
            showToast("Удалить")
            dismiss()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
