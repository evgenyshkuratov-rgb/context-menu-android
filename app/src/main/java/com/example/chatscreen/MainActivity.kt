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
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatscreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMessageClickListener {

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

    override fun onMessageClick(message: Message, messageText: String?, isOutgoing: Boolean, anchorView: View) {
        animateBubble(anchorView) {
            ContextMenuBottomSheet.newInstance(messageText, isOutgoing, MenuAnimationStyle.WHATSAPP)
                .show(supportFragmentManager, "ContextMenuBottomSheet")
        }
    }

    private fun animateBubble(view: View, onComplete: () -> Unit) {
        vibrator?.vibrate(VibrationEffect.createOneShot(10, 30))

        view.animate()
            .scaleX(0.88f)
            .scaleY(0.88f)
            .setDuration(100)
            .withEndAction {
                SpringAnimation(view, SpringAnimation.SCALE_X, 1f).apply {
                    spring.stiffness = SpringForce.STIFFNESS_LOW
                    spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                    start()
                }
                SpringAnimation(view, SpringAnimation.SCALE_Y, 1f).apply {
                    spring.stiffness = SpringForce.STIFFNESS_LOW
                    spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                    start()
                }
                view.postDelayed(onComplete, 150)
            }
            .start()
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
