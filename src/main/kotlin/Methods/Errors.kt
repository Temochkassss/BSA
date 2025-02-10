package Methods

import Methods.MediaSender.sendPhoto
import Methods.MediaSender.sendSticker
import Methods.TextSender.deleteMessage
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import java.io.File
import java.util.*
import kotlin.concurrent.schedule


object Errors {
    fun mistake(
        bot: TelegramLongPollingBot,
        userChatId: Long,
    ) {
        val ooopsItIsBrokenText = sendMessage(bot, userChatId, "Ой, сломався...")

        Timer().schedule(1000){
            val ooopsItIsBrokenPhoto = sendPhoto(
                bot,
                userChatId,
                photoFile = File("src/main/resources/Img/technical_chocolate.jpg"),
                caption = "Наша команда уже работает над этой проблемой!",
            )

            Timer().schedule(10000){
                ooopsItIsBrokenText?.let { deleteMessage(bot, userChatId, ooopsItIsBrokenText.messageId) } // функция отправки содержит ? возврат
                ooopsItIsBrokenPhoto?.let { deleteMessage(bot, userChatId, ooopsItIsBrokenPhoto.messageId) }
            }
        }
    }

    fun goodBoy(
        bot: TelegramLongPollingBot,
        userChatId: Long,
    )
    {
        val hotOneShotText = sendMessage(bot, userChatId, "Да ты действиетльно заслужил награду...")

        val hotOneShotPhoto = sendPhoto(
            bot,
            userChatId,
            photoFile = File("src/main/resources/Img/cactus_penis.jpg"),
            caption = ")))",
            spoiler = true,
            protectContent = true
        )

        Timer().schedule(2000) {
            hotOneShotText?.let { editMessage(
                bot,
                userChatId,
                hotOneShotText.messageId,
                "_Хороших кобелей, ещё щенками разбирают_ \uD83E\uDD2D",
                parseMode = ParseMode.MARKDOWNV2,
            ) }

            val hotOneShotSticker = sendSticker(
                bot,
                userChatId,
                "CAACAgQAAxkBAAEMm0JmsQi_2cMZINysCMEr3hI0GHHXIwACLA0AAobUOFLnQwWiSuO56DUE"
            )

           // Ожидаем N секунд перед удалением фото/текста/стикера
            hotOneShotPhoto?.let { deleteMessage(bot, userChatId, hotOneShotPhoto.messageId, 3000) }
            hotOneShotText?.let { deleteMessage(bot, userChatId, hotOneShotText.messageId, 7000) }
            hotOneShotSticker?.let { deleteMessage(bot, userChatId, hotOneShotSticker.messageId, 8000) }


        }
    }
}