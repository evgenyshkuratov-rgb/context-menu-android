package com.example.chatscreen

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatscreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMessageGestureListener {

    private lateinit var binding: ActivityMainBinding
    private var isDarkMode = false
    private val vibrator by lazy { getSystemService<Vibrator>() }
    private val circleOutlineProvider = CircleOutlineProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupHeader()
        setupMessages()
        updateThemeIcon()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.header.setPadding(
                binding.header.paddingLeft,
                systemBars.top + 8,
                binding.header.paddingRight,
                binding.header.paddingBottom
            )
            binding.bottomSpacer.layoutParams = binding.bottomSpacer.layoutParams.apply {
                height = systemBars.bottom
            }
            insets
        }
    }

    private fun setupHeader() {
        binding.ivHeaderAvatar.clipToOutline = true
        binding.ivHeaderAvatar.outlineProvider = circleOutlineProvider
        binding.btnBack.setOnClickListener { finish() }
        binding.btnThemeToggle.setOnClickListener { toggleTheme() }
    }

    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        updateThemeIcon()
    }

    private fun updateThemeIcon() {
        binding.btnThemeToggle.setImageResource(
            if (isDarkMode) R.drawable.ic_theme_light else R.drawable.ic_theme_dark
        )
    }

    private fun setupMessages() {
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = MessageAdapter(SampleData.messages, this@MainActivity)
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    // Single tap - open context menu (has small delay to differentiate from double tap)
    override fun onMessageTap(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View) {
        // Haptic feedback on tap
        vibrator?.vibrate(VibrationEffect.createOneShot(8, 25))

        // Open context menu (bubble animation is handled by touch feedback in adapter)
        ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
            .show(supportFragmentManager, "ContextMenuBottomSheet")
    }

    // Double tap - quick reaction (❤️)
    override fun onMessageDoubleTap(message: Message, isOutgoing: Boolean, anchorView: View) {
        // Light haptic feedback
        vibrator?.vibrate(VibrationEffect.createOneShot(5, 20))

        // Show heart reaction animation
        showQuickReaction(anchorView, "❤️")
    }

    // Long press - select message
    override fun onMessageLongPress(message: Message, isOutgoing: Boolean, anchorView: View) {
        // Medium haptic feedback for selection
        vibrator?.vibrate(VibrationEffect.createOneShot(15, 50))

        // Toggle selection visual state
        toggleMessageSelection(anchorView)
    }

    private fun showQuickReaction(anchorView: View, emoji: String) {
        // Create floating emoji view
        val emojiView = android.widget.TextView(this).apply {
            text = emoji
            textSize = 48f
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
        }

        // Add to root layout
        val rootLayout = binding.root as android.view.ViewGroup
        rootLayout.addView(emojiView)

        // Position emoji at center of the message bubble
        val location = IntArray(2)
        anchorView.getLocationInWindow(location)
        val rootLocation = IntArray(2)
        rootLayout.getLocationInWindow(rootLocation)

        emojiView.x = location[0] - rootLocation[0] + anchorView.width / 2f - 60f
        emojiView.y = location[1] - rootLocation[1] + anchorView.height / 2f - 60f

        // Animate: scale up + fade in, then scale up more + fade out + float up
        emojiView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(150)
            .withEndAction {
                emojiView.animate()
                    .alpha(0f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .translationYBy(-100f)
                    .setDuration(400)
                    .setStartDelay(200)
                    .withEndAction {
                        rootLayout.removeView(emojiView)
                    }
                    .start()
            }
            .start()
    }

    private fun toggleMessageSelection(anchorView: View) {
        val isCurrentlySelected = anchorView.tag == "selected"
        val rootLayout = binding.root as android.view.ViewGroup

        if (isCurrentlySelected) {
            // Deselect: remove highlight and checkmark
            anchorView.tag = null

            // Remove selection overlay
            (anchorView as? android.view.ViewGroup)?.let { parent ->
                parent.findViewWithTag<View>("selection_overlay")?.let { overlay ->
                    overlay.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .withEndAction { parent.removeView(overlay) }
                        .start()
                }
            }

            // Remove checkmark
            rootLayout.findViewWithTag<View>("checkmark_${anchorView.hashCode()}")?.let { checkmark ->
                checkmark.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(150)
                    .withEndAction { rootLayout.removeView(checkmark) }
                    .start()
            }

            // Light haptic for deselect
            vibrator?.vibrate(VibrationEffect.createOneShot(5, 20))

        } else {
            // Select: add highlight overlay and checkmark
            anchorView.tag = "selected"

            // Create selection overlay with primary color tint
            val overlay = View(this).apply {
                tag = "selection_overlay"
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#2A2196F3")) // Primary blue with 16% opacity
                    cornerRadius = 14 * resources.displayMetrics.density
                }
                alpha = 0f
            }

            (anchorView as? android.view.ViewGroup)?.let { parent ->
                overlay.layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                parent.addView(overlay, 0) // Add at index 0 so it's behind content

                // Animate overlay appearance
                overlay.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }

            // Create checkmark indicator
            val checkmarkSize = (28 * resources.displayMetrics.density).toInt()
            val checkmark = android.widget.FrameLayout(this).apply {
                tag = "checkmark_${anchorView.hashCode()}"
                layoutParams = android.view.ViewGroup.LayoutParams(checkmarkSize, checkmarkSize)

                // Circle background
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(android.graphics.Color.parseColor("#2196F3")) // Primary blue
                }

                // Checkmark icon
                val checkIcon = android.widget.ImageView(this@MainActivity).apply {
                    setImageResource(R.drawable.ic_check_outline)
                    setColorFilter(android.graphics.Color.WHITE)
                    scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                    val padding = (6 * resources.displayMetrics.density).toInt()
                    setPadding(padding, padding, padding, padding)
                }
                addView(checkIcon, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))

                alpha = 0f
                scaleX = 0.5f
                scaleY = 0.5f
            }

            rootLayout.addView(checkmark)

            // Position checkmark at bottom-right of the bubble
            val location = IntArray(2)
            anchorView.getLocationInWindow(location)
            val rootLocation = IntArray(2)
            rootLayout.getLocationInWindow(rootLocation)

            checkmark.x = location[0] - rootLocation[0] + anchorView.width - checkmarkSize / 2f
            checkmark.y = location[1] - rootLocation[1] + anchorView.height - checkmarkSize / 2f

            // Animate checkmark appearance with spring effect
            checkmark.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
                .start()
        }
    }

}

private object SampleData {
    private val waveformHeights = listOf(
        4, 4, 4, 4, 4, 4, 6, 12, 17, 12, 6, 4, 4, 4, 4, 6, 8, 6, 4, 4,
        4, 10, 18, 12, 16, 20, 20, 20, 16, 12, 12, 12, 14, 18, 12, 6, 4
    )

    val messages = listOf(
        Message.OutgoingText(
            text = "Добрый день! Работа почти завершена. Осталось внести последние правки",
            time = "10:15",
            isRead = true
        ),
        Message.Voice(
            senderName = "Тимур Петрович",
            senderAvatarIndex = 0,
            duration = "00:07",
            time = "10:37",
            waveformHeights = waveformHeights
        ),
        Message.IncomingText(
            senderName = "Анна Ковалёва",
            senderAvatarIndex = 1,
            text = "Доброе утро! Напоминаю, что завтра крайний срок по экрану онбординга.\nИгорь, у тебя готов финальный макет? Светлана просила прислать ей сегодня до 14:00.",
            time = "13:26"
        ),
        Message.DateSeparator(date = "Сегодня"),
        Message.OutgoingText(
            text = "Здравствуйте! Да, почти закончил. Осталось поправить шрифты и иконку. Вышлю до 12:00.",
            time = "10:15",
            isEdited = true,
            isRead = true
        ),
        Message.Image(
            senderName = "Анна Ковалёва",
            senderAvatarIndex = 1,
            imageResId = R.drawable.photo_sample,
            time = "11:23",
            reactions = listOf(Reaction("👌", 2))
        )
    )
}
