package com.example.chatscreen.data

import com.example.chatscreen.R
import com.example.chatscreen.model.Message
import com.example.chatscreen.model.Reaction

/**
 * Sample data for testing the chat UI.
 * In a real app, this would be replaced by a repository/data source.
 */
object SampleData {

    private val waveformHeights = listOf(
        4, 4, 4, 4, 4, 4, 6, 12, 17, 12, 6, 4, 4, 4, 4, 6, 8, 6, 4, 4,
        4, 10, 18, 12, 16, 20, 20, 20, 16, 12, 12, 12, 14, 18, 12, 6, 4
    )

    val messages: List<Message> = listOf(
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
            text = "Доброе утро! Напоминаю, что завтра крайний срок по экрану онбординга.\n" +
                    "Игорь, у тебя готов финальный макет? Светлана просила прислать ей сегодня до 14:00.",
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
