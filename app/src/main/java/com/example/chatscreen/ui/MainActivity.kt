package com.example.chatscreen.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatscreen.R
import com.example.chatscreen.data.SampleData
import com.example.chatscreen.databinding.ActivityMainBinding
import com.example.chatscreen.model.Message
import com.example.chatscreen.ui.adapter.MessageAdapter
import com.example.chatscreen.ui.adapter.OnMessageGestureListener
import com.example.chatscreen.ui.contextmenu.ContextMenuBottomSheet
import com.example.chatscreen.ui.contextmenu.MenuAnimationStyle
import com.example.chatscreen.util.CircleOutlineProvider
import com.example.chatscreen.util.Constants

/**
 * Main chat screen activity.
 * Displays a list of messages with gesture-based interactions:
 * - Single tap: Opens context menu
 * - Double tap: Quick reaction (❤️)
 * - Long press: Select message
 */
class MainActivity : AppCompatActivity(), OnMessageGestureListener {

    private lateinit var binding: ActivityMainBinding
    private var isDarkMode = false
    private val vibrator by lazy { getSystemService<Vibrator>() }
    private val circleOutlineProvider = CircleOutlineProvider()
    private val density by lazy { resources.displayMetrics.density }

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

    // region Setup

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.header.setPadding(
                binding.header.paddingLeft,
                systemBars.top + (8 * density).toInt(),
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

    private fun setupMessages() {
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = MessageAdapter(SampleData.messages, this@MainActivity)
            setHasFixedSize(true)
            itemAnimator = null
        }
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

    // endregion

    // region Gesture Callbacks

    override fun onMessageTap(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View) {
        vibrate(Constants.Haptic.TAP_DURATION_MS, Constants.Haptic.TAP_AMPLITUDE)
        ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
            .show(supportFragmentManager, TAG_CONTEXT_MENU)
    }

    override fun onMessageDoubleTap(message: Message, isOutgoing: Boolean, anchorView: View) {
        vibrate(Constants.Haptic.DOUBLE_TAP_DURATION_MS, Constants.Haptic.DOUBLE_TAP_AMPLITUDE)
        showQuickReaction(anchorView, Constants.QuickReaction.DEFAULT_EMOJI)
    }

    override fun onMessageLongPress(message: Message, isOutgoing: Boolean, anchorView: View) {
        vibrate(Constants.Haptic.LONG_PRESS_DURATION_MS, Constants.Haptic.LONG_PRESS_AMPLITUDE)
        toggleMessageSelection(anchorView)
    }

    // endregion

    // region Quick Reaction

    private fun showQuickReaction(anchorView: View, emoji: String) {
        val rootLayout = binding.root as ViewGroup
        val emojiView = createEmojiView(emoji)
        rootLayout.addView(emojiView)
        positionEmojiAtCenter(emojiView, anchorView, rootLayout)
        animateEmojiReaction(emojiView, rootLayout)
    }

    private fun createEmojiView(emoji: String): TextView {
        return TextView(this).apply {
            text = emoji
            textSize = Constants.QuickReaction.EMOJI_SIZE_SP
            alpha = 0f
            scaleX = Constants.QuickReaction.INITIAL_SCALE
            scaleY = Constants.QuickReaction.INITIAL_SCALE
        }
    }

    private fun positionEmojiAtCenter(emojiView: View, anchorView: View, rootLayout: ViewGroup) {
        val anchorLocation = IntArray(2)
        val rootLocation = IntArray(2)
        anchorView.getLocationInWindow(anchorLocation)
        rootLayout.getLocationInWindow(rootLocation)

        emojiView.x = anchorLocation[0] - rootLocation[0] + anchorView.width / 2f - Constants.QuickReaction.POSITION_OFFSET_PX
        emojiView.y = anchorLocation[1] - rootLocation[1] + anchorView.height / 2f - Constants.QuickReaction.POSITION_OFFSET_PX
    }

    private fun animateEmojiReaction(emojiView: View, rootLayout: ViewGroup) {
        emojiView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(Constants.QuickReaction.FADE_IN_DURATION_MS)
            .withEndAction {
                emojiView.animate()
                    .alpha(0f)
                    .scaleX(Constants.QuickReaction.FINAL_SCALE)
                    .scaleY(Constants.QuickReaction.FINAL_SCALE)
                    .translationYBy(Constants.QuickReaction.FLOAT_TRANSLATION_Y)
                    .setDuration(Constants.QuickReaction.FADE_OUT_DURATION_MS)
                    .setStartDelay(Constants.QuickReaction.HOLD_DELAY_MS)
                    .withEndAction { rootLayout.removeView(emojiView) }
                    .start()
            }
            .start()
    }

    // endregion

    // region Message Selection

    private fun toggleMessageSelection(anchorView: View) {
        val isSelected = anchorView.tag == TAG_SELECTED
        if (isSelected) {
            deselectMessage(anchorView)
        } else {
            selectMessage(anchorView)
        }
    }

    private fun selectMessage(anchorView: View) {
        anchorView.tag = TAG_SELECTED
        val rootLayout = binding.root as ViewGroup

        // Add selection overlay
        addSelectionOverlay(anchorView)

        // Add checkmark badge
        val checkmark = createCheckmarkBadge()
        rootLayout.addView(checkmark)
        positionCheckmark(checkmark, anchorView, rootLayout)
        animateCheckmarkIn(checkmark)
    }

    private fun deselectMessage(anchorView: View) {
        anchorView.tag = null
        val rootLayout = binding.root as ViewGroup

        vibrate(Constants.Haptic.DESELECT_DURATION_MS, Constants.Haptic.DESELECT_AMPLITUDE)

        // Remove overlay
        (anchorView as? ViewGroup)?.findViewWithTag<View>(TAG_OVERLAY)?.let { overlay ->
            overlay.animate()
                .alpha(0f)
                .setDuration(Constants.Selection.DISAPPEAR_DURATION_MS)
                .withEndAction { (anchorView as ViewGroup).removeView(overlay) }
                .start()
        }

        // Remove checkmark
        rootLayout.findViewWithTag<View>("$TAG_CHECKMARK${anchorView.hashCode()}")?.let { checkmark ->
            checkmark.animate()
                .alpha(0f)
                .scaleX(Constants.Selection.CHECKMARK_INITIAL_SCALE)
                .scaleY(Constants.Selection.CHECKMARK_INITIAL_SCALE)
                .setDuration(Constants.Selection.DISAPPEAR_DURATION_MS)
                .withEndAction { rootLayout.removeView(checkmark) }
                .start()
        }
    }

    private fun addSelectionOverlay(anchorView: View) {
        val overlay = View(this).apply {
            tag = TAG_OVERLAY
            background = GradientDrawable().apply {
                setColor(Color.parseColor(Constants.Selection.OVERLAY_COLOR))
                cornerRadius = Constants.Selection.BUBBLE_CORNER_RADIUS_DP * density
            }
            alpha = 0f
        }

        (anchorView as? ViewGroup)?.let { parent ->
            overlay.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            parent.addView(overlay, 0)
            overlay.animate()
                .alpha(1f)
                .setDuration(Constants.Selection.APPEAR_DURATION_MS)
                .start()
        }
    }

    private fun createCheckmarkBadge(): FrameLayout {
        val size = (Constants.Selection.CHECKMARK_SIZE_DP * density).toInt()
        val padding = (Constants.Selection.CHECKMARK_PADDING_DP * density).toInt()

        return FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(size, size)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(Constants.Selection.CHECKMARK_COLOR))
            }

            val checkIcon = ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_check_outline)
                setColorFilter(Color.WHITE)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(padding, padding, padding, padding)
            }
            addView(checkIcon, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))

            alpha = 0f
            scaleX = Constants.Selection.CHECKMARK_INITIAL_SCALE
            scaleY = Constants.Selection.CHECKMARK_INITIAL_SCALE
        }
    }

    private fun positionCheckmark(checkmark: View, anchorView: View, rootLayout: ViewGroup) {
        val anchorLocation = IntArray(2)
        val rootLocation = IntArray(2)
        anchorView.getLocationInWindow(anchorLocation)
        rootLayout.getLocationInWindow(rootLocation)

        val checkmarkSize = Constants.Selection.CHECKMARK_SIZE_DP * density
        checkmark.x = anchorLocation[0] - rootLocation[0] + anchorView.width - checkmarkSize / 2f
        checkmark.y = anchorLocation[1] - rootLocation[1] + anchorView.height - checkmarkSize / 2f
        checkmark.tag = "$TAG_CHECKMARK${anchorView.hashCode()}"
    }

    private fun animateCheckmarkIn(checkmark: View) {
        checkmark.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(Constants.Selection.CHECKMARK_APPEAR_DURATION_MS)
            .setInterpolator(OvershootInterpolator(Constants.Animation.OVERSHOOT_TENSION))
            .start()
    }

    // endregion

    // region Utilities

    private fun vibrate(durationMs: Long, amplitude: Int) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
    }

    // endregion

    companion object {
        private const val TAG_CONTEXT_MENU = "ContextMenuBottomSheet"
        private const val TAG_SELECTED = "selected"
        private const val TAG_OVERLAY = "selection_overlay"
        private const val TAG_CHECKMARK = "checkmark_"
    }
}
