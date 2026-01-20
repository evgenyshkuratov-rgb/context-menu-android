package com.example.chatscreen

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
import com.example.chatscreen.databinding.BottomSheetContextMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

enum class MenuAnimationStyle {
    TELEGRAM,
    IMESSAGE,
    WHATSAPP
}

class ContextMenuBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetContextMenuBinding? = null
    private val binding get() = _binding!!

    private var messageText: String? = null
    private var isOutgoing: Boolean = false
    private var animationStyle = MenuAnimationStyle.TELEGRAM

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
        arguments?.let {
            messageText = it.getString(ARG_MESSAGE_TEXT)
            isOutgoing = it.getBoolean(ARG_IS_OUTGOING, false)
            animationStyle = MenuAnimationStyle.valueOf(
                it.getString(ARG_ANIMATION_STYLE, MenuAnimationStyle.TELEGRAM.name)
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            animate().alpha(1f).translationY(0f).setDuration(250).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun animateIMessage() {
        binding.reactionBar.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(OvershootInterpolator(1.2f)).start()
        }
    }

    private fun animateWhatsApp() {
        val reactions = listOf(
            binding.reaction1, binding.reaction2, binding.reaction3,
            binding.reaction4, binding.reaction5, binding.btnAddReaction
        )
        reactions.forEachIndexed { index, view ->
            view.scaleX = 0f
            view.scaleY = 0f
            view.animate().scaleX(1f).scaleY(1f)
                .setDuration(150).setStartDelay(index * 30L)
                .setInterpolator(OvershootInterpolator(1.5f)).start()
        }

        val actions = listOf(
            binding.actionReply, binding.actionForward, binding.actionComment,
            binding.actionPin, binding.actionCopy, binding.actionAddLabel,
            binding.actionSave, binding.actionViewed, binding.actionSelect, binding.actionDelete
        )
        actions.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -30f
            view.animate().alpha(1f).translationX(0f)
                .setDuration(150).setStartDelay(100 + index * 20L)
                .setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun setupReactions() {
        val reactionPairs = listOf(
            binding.reaction1 to "👍", binding.reaction2 to "👎", binding.reaction3 to "🔥",
            binding.reaction4 to "👌", binding.reaction5 to "🤔"
        )
        reactionPairs.forEach { (view, emoji) ->
            view.setOnClickListener { dismissWithToast("Реакция: $emoji") }
        }
        binding.btnAddReaction.setOnClickListener { dismissWithToast("Добавить реакцию") }
    }

    private fun setupActions() {
        binding.actionReply.setOnClickListener { dismissWithToast("Ответить") }
        binding.actionForward.setOnClickListener { dismissWithToast("Переслать") }
        binding.actionComment.setOnClickListener { dismissWithToast("Прокомментировать") }
        binding.actionPin.setOnClickListener { dismissWithToast("Закрепить") }
        binding.actionCopy.setOnClickListener { copyToClipboard() }
        binding.actionAddLabel.setOnClickListener { dismissWithToast("Добавить метку") }
        binding.actionSave.setOnClickListener { dismissWithToast("В сохраненное") }
        binding.actionViewed.setOnClickListener { dismissWithToast("Просмотрено") }
        binding.actionSelect.setOnClickListener { dismissWithToast("Выбрать") }
        binding.actionDelete.setOnClickListener { dismissWithToast("Удалить") }
    }

    private fun copyToClipboard() {
        messageText?.let { text ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            dismissWithToast("Скопировано")
        } ?: dismissWithToast("Копировать")
    }

    private fun dismissWithToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        dismiss()
    }
}
