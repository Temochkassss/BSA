package Methods

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.*
import kotlin.concurrent.schedule

object TextSender {

    fun sendMessage(
        bot: TelegramLongPollingBot,
        userChatId: Long,
        userText: String,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        parseMode: String? = null,
    ): Message? {
        val message = SendMessage().apply {
            chatId = userChatId.toString()
            text = userText

            this.parseMode = when{ // форматирование текста/текстового блока
                parseMode != null -> parseMode
                else -> null
            }

            this.replyMarkup = when {  // проверяем наличие клавиатур в сообщении
                replyKeyboard != null -> replyKeyboard
                inlineKeyboard != null -> inlineKeyboard
                else -> null
            }

        }

        // Передаём Telegram Bot API для отправки
        return bot.execute(message)
    }

    fun editMessage(
        bot: TelegramLongPollingBot,
        userChatId: Long,
        messageId: Int,
        newText: String,
        parseMode: String? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        delay: Long = 0,
        disableWebPagePreview: Boolean = false
    ) {
        Timer().schedule(delay) {
            val editMessageText = EditMessageText().apply {
                chatId = userChatId.toString()
                this.messageId = messageId
                text = newText

                this.parseMode = when {
                    parseMode != null -> parseMode
                    else -> null
                }

                this.disableWebPagePreview = disableWebPagePreview

                this.replyMarkup = when {
                    inlineKeyboard != null -> inlineKeyboard
                    else -> null
                }
            }
            // Для обратотки исключений, когда сообщение с указанным messageId не существует/ошибочное или возникли сетевые проблемы
            try {
                bot.execute(editMessageText)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }

    }

    

    fun deleteMessage(
        bot: TelegramLongPollingBot, // используем AbsSender, чтобы была поддержка разных типов ботов
        userChatId: Long,
        messageId: Int,
        delay: Long = 0
    ) {
        val deleteMessage = DeleteMessage().apply {
            chatId = userChatId.toString()
            this.messageId = messageId
        }

        Timer().schedule(delay) {
            // Передаём Telegram Bot API для удаления
            bot.execute(deleteMessage)
        }
    }


    fun deleteMessagesSafely(bot: TelegramLongPollingBot, chatId: Long) {
        // Получение списка по ключу chatId
        val messagesToDelete = MessageManager.getMessagesToDelete(chatId)
        for (messageId in messagesToDelete) {
            try {
                deleteMessage(bot, chatId, messageId)
            } catch (e: Exception) {
                println("Failed to delete message with id $messageId: ${e.message}")
            }
        }
        MessageManager.clearMessagesToDelete(chatId)
    }

    fun clearPreviousMessages(userState: CallbackData.UserState, bot: TelegramLongPollingBot, chatId: Long, vararg messageIds: Int) {
        messageIds.forEach { messageId ->
            if (messageId != 0) {
                try {
                    deleteMessage(bot, chatId, messageId)
                } catch (e: TelegramApiException) {
                    // Если сообщение уже было удалено, то игнорируем ошибку
                    if (e.message?.contains("message to delete not found") == true) {
                        // Ничего не делаем
                    } else {
                        throw e
                    }
                }
                // Обнуляем значение поля в userState
                userState.clearMessageId(messageId)
            }
        }
    }



}