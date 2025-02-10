package Methods

import Database.Question.CharForQuestionDb
import Database.Question.DatabaseQuestionHelper
import Database.Results.CharForResultsDb
import Database.Results.DatabaseResultsHelper
import Methods.TextSender.deleteMessagesSafely
import Methods.TextSender.sendMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import java.util.*

object Generation {

    private val dbResultsHelper = DatabaseResultsHelper(CharForResultsDb.DATABASE_NAME)
    private val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)


    /**
     * Генерация и отправка приветственного сообщения
     */

    fun welcomeMessageGeneration(bot: TelegramLongPollingBot, chatId: Long, userState: CallbackData.UserState) {
        if (userState.mainMenuMID != 0) {
            TextSender.deleteMessage(bot, chatId, userState.mainMenuMID)
        }
        userState.mainMenuMID = sendMessage(
            bot,
            chatId,
            StringForBot.HELLO_MESSAGE,
            replyKeyboard = StringForBot.mainRK(), // главная Reply-клавиатура
            parseMode = ParseMode.HTML
        )?.messageId ?: 0
    }

    fun somethingElseKeyboardGeneration (bot: TelegramLongPollingBot, chatId: Long, userState: CallbackData.UserState) {
        userState.somethingElseMID = sendMessage(
            bot,
            chatId,
            "Что-либо ещё?",
            replyKeyboard = StringForBot.mainRK(),
            parseMode = ParseMode.HTML
        )?.messageId ?: 0

    }

    /**
     * Генерация testId с проверкой
     */
    fun randomTestIdGeneration(chatId: Long) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }
        while (userState.existingTestIdCount == null) {
            // Генерация testId
            userState.generatedTestId = UUID.randomUUID().toString().replace("-", "")

            // Проверка аналогичных testId в базе данных
            val selectQuery = CharForQuestionDb.COUNT_TEST_ID
            val selectStatement = userState.connection?.prepareStatement(selectQuery)
            selectStatement?.setString(1, userState.generatedTestId)
            val resultSet = selectStatement?.executeQuery()
            resultSet?.next()
            userState.existingTestIdCount = resultSet?.getInt(1)

            if(userState.existingTestIdCount == null) {
                println("Данный testId уникален")
                break
            }
        }
    }

    data class ResultAndSecurity(
        val maxNumberOfAttempts: Int = Int.MAX_VALUE,
        val currentNumberOfAttempts: Int = 0,
    )

    fun randomResultIdGeneration(chatId: Long): String {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        while (userState.existingResultIdCount == null) {
            // Генерация testId
            userState.generatedResultId = UUID.randomUUID().toString().replace("-", "")

            if(dbResultsHelper.countResultId(userState.generatedResultId) == 0) {
                println("Данный ResultId: ${userState.generatedResultId}  уникален")

                // Получение значений из базы данных для словаря
                val maxNumberOfAttempts = dbQuestionHelper.getAttemptsCount(userState.testId!!)
                if (maxNumberOfAttempts != null) {
                    val currentNumberOfAttempts = (userState.mapTestIdNumAttempts[userState.testId]?.currentNumberOfAttempts ?: 0)

                    userState.mapTestIdNumAttempts[userState.testId!!] = ResultAndSecurity(
                        maxNumberOfAttempts = maxNumberOfAttempts,
                        currentNumberOfAttempts = currentNumberOfAttempts
                    )
                }
                break
            }
        }
        return userState.generatedResultId
    }

    data class UrlAnswersInfo(
        val linkMessageId: Int = 0, // ID сообщения с ссылкой
        val answerText: String? = null, // Текст ссылки
        val answerMessageId: Int, // ID сообщения с ответами
        val imageMID: Int = 0,// ID фотографии
        val targetComPercent: Int = 70,
        val targetUsername: String? = null,
        val targetNumAttempts: Int = 100,
        val photoFileId: String? = null,
        val resultText: String? = null,
    )
    fun htmlUrlForTestGeneration(bot: TelegramLongPollingBot, chatId: Long, userState: CallbackData.UserState, textName: String?, testId: String? = null) {

        // Формирование ссылки для прохождения теста
        val url: String = if (testId != "" && testId != null) {
            "https://t.me/and_she_will_ask_bot?start=${chatId}_${testId}"
        } else {
            "https://t.me/and_she_will_ask_bot?start=${chatId}_${userState.generatedTestId}"
        }

        // Формируем корректный текст с HTML-разметкой
        val messageText = if (textName == null || textName == "") {
            """
                    |${StringForBot.TAKE_LINK_AND_LUCK_CONGRATULATIONS}
                    |Ссылка: <a href="$url"><b>From-${userState.userInformation?.userName ?: "Love"}-To-You❦</b></a>
                    |${StringForBot.TAKE_LINK_AND_LUCK}
                """.trimMargin()
        } else {
            """
                    |${StringForBot.TAKE_LINK_AND_LUCK_CONGRATULATIONS}
                    |Название: <i><b>"${textName}"</b></i>
                    |Ссылка: <a href="$url"><b>From-${userState.userInformation?.userName ?: "Love"}-To-You❦</b></a>
                    |${StringForBot.TAKE_LINK_AND_LUCK}
                """.trimMargin()
        }

        val currentTestId = testId ?: userState.generatedTestId

        currentTestId?.let {

            // Блок кода для отправки фотографии к RMP
            if (userState.needImageForTest) {
                // Получение фотографии для теста
                val imageUrl = userState.testId?.let { it1 -> dbQuestionHelper.readImageUrl(it1) }
                // Cообщение с ссылкой
                userState.urlMID = sendMessage(
                    bot,
                    chatId,
                    messageText,
                    inlineKeyboard = StringForBot.takeTestSetUrlIB(url),
                    parseMode = ParseMode.HTML
                )?. messageId ?: 0
                userState.needImageForTest = false
                // Обновление поля с фотографией
                userState.testId?.let { it1 -> dbQuestionHelper.updateImageUrlFromSystemTest(targetTestId = currentTestId , systemTestId = it1) }
            } else {
                // Сообщение с ссылкой
                userState.urlMID = sendMessage(
                    bot,
                    chatId,
                    messageText,
                    inlineKeyboard = StringForBot.takeTestSetUrlIB(url),
                    parseMode = ParseMode.HTML
                )?. messageId ?: 0
            }

            val answerMessage = "||Засекреченные ответы\\:||"

            userState.seeMyAnswersMID = sendMessage(
                bot,
                chatId,
                answerMessage,
                inlineKeyboard = StringForBot.seeMyAnswersIK(it),
                parseMode = ParseMode.MARKDOWNV2
            )?.messageId ?: 0

            // Сохраняем данные в мапы
            saveMessageData(userState, it, answerMessage)
        }

        somethingElseKeyboardGeneration(bot, chatId, userState)

        // Удаление сообщений создания теста (финальных блоков)
        deleteMessagesSafely(bot, chatId)
    }


    private fun saveMessageData(
        userState: CallbackData.UserState,
        testId: String,
        messageText: String,
    ) {
        userState.mapTestIdLinkAnswerInfo[testId] = UrlAnswersInfo(
            linkMessageId = userState.urlMID,
            answerText = messageText,
            answerMessageId = userState.seeMyAnswersMID,
            imageMID = userState.theLastPhotoMID
        )
    }

    fun nameOrSkipNamingTestGeneration (bot: TelegramLongPollingBot, chatId: Long, userState: CallbackData.UserState) {
        userState.adviceToSkipNamingTestMID = sendMessage(
            bot,
            chatId,
            "Придумайте название теста:",
            inlineKeyboard = StringForBot.skipNamingTheTestOrGenerateIK()
        )?.messageId ?: 0

        // Устанавливаем состояние ожидания названия теста
        userState.pollCreationState = CallbackData.PollCreationState.WaitingForNameTest()
    }

    fun windowAlertGeneration(bot: TelegramLongPollingBot, textAlert: String, callbackQuery: CallbackQuery) {
//        Выплывающее уведомление
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackQuery.id
        answerCallbackQuery.text = textAlert
        answerCallbackQuery.showAlert = true
        bot.execute(answerCallbackQuery)
    }
}