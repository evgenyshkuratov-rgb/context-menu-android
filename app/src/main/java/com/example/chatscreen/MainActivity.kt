package com.example.chatscreen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatscreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
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

            // Apply top padding to header
            binding.header.setPadding(
                binding.header.paddingLeft,
                systemBars.top + 8,
                binding.header.paddingRight,
                binding.header.paddingBottom
            )

            // Apply bottom padding to input panel
            binding.inputPanel.setPadding(
                binding.inputPanel.paddingLeft,
                binding.inputPanel.paddingTop,
                binding.inputPanel.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }

    private fun setupHeader() {
        // Set header avatar to be circular
        binding.ivHeaderAvatar.clipToOutline = true
        binding.ivHeaderAvatar.outlineProvider = CircleOutlineProvider()

        // Back button click
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Theme toggle click
        binding.btnThemeToggle.setOnClickListener {
            toggleTheme()
        }
    }

    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        updateThemeIcon()
    }

    private fun updateThemeIcon() {
        val iconRes = if (isDarkMode) {
            android.R.drawable.ic_menu_day
        } else {
            android.R.drawable.ic_menu_month
        }
        binding.btnThemeToggle.setImageResource(iconRes)
    }

    private fun setupMessages() {
        val messages = createSampleMessages()
        val adapter = MessageAdapter(messages)

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }
    }

    private fun createSampleMessages(): List<Message> {
        // Waveform heights pattern (simulating voice message visualization)
        val waveformHeights = listOf(
            4, 4, 4, 4, 4, 4, 6, 12, 17, 12, 6, 4, 4, 4, 4, 6, 8, 6, 4, 4,
            4, 10, 18, 12, 16, 20, 20, 20, 16, 12, 12, 12, 14, 18, 12, 6, 4
        )

        return listOf(
            // Message 1: Outgoing text
            Message.OutgoingText(
                text = "Добрый день! Работа почти завершена. Осталось внести последние правки",
                time = "10:15",
                isRead = true
            ),

            // Message 2: Incoming voice message
            Message.Voice(
                senderName = "Тимур Петрович",
                senderAvatarIndex = 0,
                duration = "00:07",
                time = "10:37",
                waveformHeights = waveformHeights
            ),

            // Message 3: Incoming text
            Message.IncomingText(
                senderName = "Анна Ковалёва",
                senderAvatarIndex = 1,
                text = "Доброе утро! Напоминаю, что завтра крайний срок по экрану онбординга.\nИгорь, у тебя готов финальный макет? Светлана просила прислать ей сегодня до 14:00.",
                time = "13:26"
            ),

            // Date separator
            Message.DateSeparator(date = "Сегодня"),

            // Message 4: Outgoing text (edited)
            Message.OutgoingText(
                text = "Здравствуйте! Да, почти закончил. Осталось поправить шрифты и иконку. Вышлю до 12:00.",
                time = "10:15",
                isEdited = true,
                isRead = true
            ),

            // Message 5: Incoming image with reactions
            Message.Image(
                senderName = "Анна Ковалёва",
                senderAvatarIndex = 1,
                imageResId = R.drawable.photo_sample,
                time = "11:23",
                reactions = listOf(Reaction("👌", 2))
            )
        )
    }
}
