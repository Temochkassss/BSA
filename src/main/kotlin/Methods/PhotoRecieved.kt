package Methods

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Message

object PhotoRecieved {
    fun handleWaitingForPhoto(bot: TelegramLongPollingBot, chatId: Long, message: Message) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        val photo = message.photo?.lastOrNull() // Берем последнее фото
        if (photo != null) {
            val fileId = photo.fileId
            val testId = userState.pollCreationState?.let { (it as? CallbackData.PollCreationState.WaitingForPhoto)?.testId }
            val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0

            if (testId != null) {
                // Сохраняем file_id в базу данных
                MessageRecieved.dbQuestionHelper.saveFileId(testId, fileId)
                // Сбрасываем состояние
                userState.pollCreationState = null

                // TODO: (Блоки текста закрываются по мере заполнения и кнопки галочками синими)

//                // Получаем fileId из базы данных (для проверки)
//                val savedFileId = MessageRecieved.dbQuestionHelper.getPhotoFileId(testId)
//                if (savedFileId != null) {
//                    // Отправляем фото пользователю
//                    sendPhotoToUser(bot, chatId, savedFileId)
//                } else {
//                    println("Фото не найдено в базе данных.")
//                }

                // Возвращаемся к меню
                CallbackData.handleAddPeper(bot, chatId, testId, messageId)
            } else {
                println("Ошибка: не удалось определить testId.")
            }
        } else {
            println("Отправлено не фото")
        }
    }

    private fun sendPhotoToUser(bot: TelegramLongPollingBot, chatId: Long, fileId: String) {
        try {
            MediaSender.sendPhoto(bot, chatId, photoUrl = fileId)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Ошибка при отправке фото: ${e.message}")
        }
    }
}