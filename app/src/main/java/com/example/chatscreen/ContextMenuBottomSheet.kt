package com.example.chatscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatscreen.databinding.BottomSheetContextMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ContextMenuBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetContextMenuBinding? = null
    private val binding get() = _binding!!

    private var messageText: String? = null
    private var isOutgoing: Boolean = false

    companion object {
        private const val ARG_MESSAGE_TEXT = "message_text"
        private const val ARG_IS_OUTGOING = "is_outgoing"

        fun newInstance(messageText: String?, isOutgoing: Boolean): ContextMenuBottomSheet {
            return ContextMenuBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE_TEXT, messageText)
                    putBoolean(ARG_IS_OUTGOING, isOutgoing)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            messageText = it.getString(ARG_MESSAGE_TEXT)
            isOutgoing = it.getBoolean(ARG_IS_OUTGOING, false)
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
