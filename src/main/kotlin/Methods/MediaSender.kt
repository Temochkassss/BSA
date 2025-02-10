package Methods

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import java.io.File

object MediaSender {
    fun sendPhoto(
        bot: TelegramLongPollingBot,
        userChatId: Long,
        photoFile: File? = null,
        photoUrl: String? = null,
        caption: String? = null,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        spoiler: Boolean = false,
        protectContent: Boolean = false,
        parseMode: String? = null,
    ): Message? {
        val photoMessage = SendPhoto().apply {
            chatId = userChatId.toString()
            hasSpoiler = spoiler // по умолчанию спойлера НЕТ
            this.caption = caption
            this.parseMode = parseMode
            photo = when { // выявляем носитель фотографии и подгружаем
                photoFile != null -> InputFile(photoFile)
                photoUrl != null -> InputFile(photoUrl)
                else -> throw IllegalArgumentException("Either photoFile or photoUrl must be provided") // исключение используется, когда функция получает недопустимые аргументы
            }

            this.
            replyMarkup = when {
                replyKeyboard != null -> replyKeyboard
                inlineKeyboard != null -> inlineKeyboard
                else -> null
            }

            this.protectContent = protectContent // установление защиты контента исключительно на данное сообщение
        }
        return bot.execute(photoMessage)
    }

    fun sendVideo(
        bot: TelegramLongPollingBot,
        chatId: Long,
        videoFile: File? = null,
        videoUrl: String? = null,
        caption: String? = null,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        spoiler: Boolean = false,
        protectContent: Boolean = false,
    ): Message? {
        val videoMessage = SendVideo().apply {
            this.chatId = chatId.toString()
            hasSpoiler = spoiler
            this.caption = caption

            video = when {
                videoFile != null -> InputFile(videoFile)
                videoUrl != null -> InputFile(videoUrl)
                else -> throw IllegalArgumentException("Either videoFile or videoUrl must be provided")
            }

            replyMarkup = when {
                replyKeyboard != null -> replyKeyboard
                inlineKeyboard != null -> inlineKeyboard
                else -> null
            }

            this.protectContent = protectContent
        }
        return bot.execute(videoMessage)
    }

    fun sendMediaGroupFromUrls(
        bot: TelegramLongPollingBot,
        chatId: Long,
        mediaUrls: List<Pair<String, MediaType>>, //Изменено: теперь пара URL и типа медиа
    ): MutableList<Message>? {

        val inputMedia: List<InputMedia> = mediaUrls.map { (url, type) ->
            when (type) {
                MediaType.PHOTO -> InputMediaPhoto(url)
                MediaType.VIDEO -> InputMediaVideo(url) // Временно не работает
            }
        }

        if (inputMedia.isEmpty()) {
            println("Список URL-адресов медиафайлов пуст. Ничего не отправлено.")
            return null
        }

        val mediaGroup = SendMediaGroup().apply {
            this.chatId = chatId.toString()
            medias = inputMedia
        }

        return try {
            bot.execute(mediaGroup)
        } catch (e: Exception) {
            println("Ошибка отправки медиагруппы: ${e.message}")
            println(e.stackTraceToString())
            null
        }
    }

    enum class MediaType {
        PHOTO, VIDEO
    }

    fun sendSticker(
        bot: TelegramLongPollingBot,
        chatId: Long,
        stickerId: String,
        delay: Long = 0,
        replyToMessageId: ReplyKeyboardMarkup? = null
    ): Message? {

        val stickerMessage = SendSticker().apply {
            this.chatId = chatId.toString()
            sticker = InputFile(stickerId) // передаём stickerId полученный от https://t.me/idstickerbot
            this.replyMarkup = replyToMessageId
        }
        return bot.execute(stickerMessage)
    }
}
