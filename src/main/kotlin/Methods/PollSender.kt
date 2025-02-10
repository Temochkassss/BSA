package Methods

import Database.Question.CharForQuestionDb
import Database.Question.DatabaseQuestionHelper
import Database.Quizi.CharForQuiziDb
import Database.Quizi.DatabaseQuiziHelper
import Database.Results.CharForResultsDb
import Database.Results.DatabaseResultsHelper
import Methods.CallbackData.userStates
import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.deleteMessage
import Methods.TextSender.sendMessage
import StringForBot
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

object PollSender {

    data class UserPollState(
        var currentQuestionIndex: Int = 0,
        var rightCurrentCompletion: Double = 0.0,
        var listForCompletionStatistic: MutableList<Int> = mutableListOf(),
        var listOfPollMID: MutableList<Int> = mutableListOf(),
        var mapResultIdFireMID: MutableMap <String, FireMessageInfo> = mutableMapOf()
    )
    val userPollStates = mutableMapOf<Long, UserPollState>()

    data class FireMessageInfo(
        val listOfPollMIDForFire: MutableList<Int> = mutableListOf(),
        val resultOfTakingPollMID: Int = 0,
        val resultOfTakingPollText: String = "",
    )

    private val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)
    private val dbQuiziHelper = DatabaseQuiziHelper(CharForQuiziDb.DATABASE_NAME)
    private val dbResultsHelper = DatabaseResultsHelper(CharForResultsDb.DATABASE_NAME)

    private fun submitPoll(
        bot: TelegramLongPollingBot,
        chatId: Long,
        question: String,
        options: List<String>,
        isAnonymous: Boolean = false,
        allowsMultipleAnswers: Boolean = false,
        type: String = "regular", // тип опроса - латиницой с маленькой буквы
        correctAnswerId: Int? = null,
        explanation: String? = null, // объяснение/подсказка после ответа
        openPeriod: Int? = null,
        protectContent: Boolean = false,
    ): Message? {
        val poll = SendPoll().apply {
            this.chatId = chatId.toString()
            this.type = type
            this.question = question
            this.options = options
            this.allowMultipleAnswers = allowsMultipleAnswers
            this.isAnonymous = isAnonymous
            this.explanation = explanation
            this.openPeriod = openPeriod
            this.protectContent = protectContent
            disableNotification = true // отправка сообщения пройдёт беззвучно

            if (type == "quiz") {
                correctOptionId = correctAnswerId // индекс правильного ответа (начиная с 0)
            }

        }
        return bot.execute(poll)
    }

    fun handleFinishCreatingPoll(bot: TelegramLongPollingBot, update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Процесс создания опроса завершён
        userState.processCreatingPoll = false

        // Обнуление косметического номера вопроса
        userState.questionNumberInCreating = 0

        if (userState.pollCreationState is CallbackData.PollCreationState.WaitingForCorrectAnswer){
            deleteMessage(bot, chatId, userState.chooseYourAnswerMID)
        }

        // Очищаем чат от функционально значимого слова "Завершить"
        deleteMessage(bot, chatId, update.message.messageId)

        val creatingPollIsStoped = sendMessage(
            bot,
            chatId,
            "Создание опроса завершено."
        )?.messageId ?: 0
        MessageManager.addMessageToDelete(chatId,creatingPollIsStoped)

        // Блокировка повторного запуска состояний
        userState.isBackInCreatingPoll = true
        userState.isBackInCreatingPollForChoosingAnswers = true

        // Очистка предыдущий сообщений
        clearPreviousMessages(userState, bot, chatId, userState.keyboardAfterMainMenuMID, userState.mainMenuMID)

        if (userState.countCreatingPollIndividually > 0) {

            // Запрос названия теста
            Generation.nameOrSkipNamingTestGeneration(bot, chatId, userState)

            // Важно! Прерываем выполнение функции здесь, чтобы дать пользователю возможность ввести название
            return
        } else {
            // Очищаем сообщения
            TextSender.deleteMessagesSafely(bot, chatId)

            // Завершаем создание теста
            userState.pollCreationState = null

            // Блок "анимации" по возврату в главное меню
            Animation.homeStickerAnimation(bot, userState, chatId)
        }

    }

    data class UserResultInfo(
        val userResultMID: Int,
        val userResultText: String,
        val userResultLink: String,
    )

    fun analysePollAnswer(
        bot: TelegramLongPollingBot,
        update: Update,
        pollAnswer: PollAnswer
    ) {
        val chatId = pollAnswer.user.id
        val optionIds = pollAnswer.optionIds // массив с выбранными пользователем индексами

        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }
        val authorUserState = userStates.getOrPut(userState.authorChatId) { CallbackData.UserState() }
        val userPollState = userPollStates.getOrPut(chatId) { UserPollState() }

        // Отдельный объект с вопросами по testId
        val questionsFromDb = dbQuestionHelper.readQuestionFromQuestionDb(userState.testId)

                                       // Запись результата в базу данных
        // Получаем название теста, используя безопасный вызов
        val testName = try {
            userState.testId?.let { dbQuestionHelper.readTestName(it) }
        } catch (e: Exception) {
            "Без названия"
        }

        dbResultsHelper.insertResults(
            authorChatId = userState.authorChatId,
            userChatId = chatId,
            authorUsername = dbQuiziHelper.readUsernameByChatId(userState.authorChatId),
            userUsername = dbQuiziHelper.readUsernameByChatId(chatId),
            testName = testName,
            authorTestId = userState.testId ?: "Без названия",
            resultsTestId = userState.generatedResultId,
            choosedIndex = optionIds.firstOrNull() ?: -1,
            timestamp = userState.currentTimestamp,
            dbQuiziHelper = dbQuiziHelper
        )

        // Сравнение выбранного ответа с правильным
        val currentQuestion = questionsFromDb[userPollState.currentQuestionIndex]
        if (optionIds.contains(currentQuestion.indexOfRightAnswer)) {
            // Зачисление, как верно выполненного
            userPollState.rightCurrentCompletion++
            // Переход к следующему вопросу
            userPollState.currentQuestionIndex++
        } else {
            // Переход к следующему вопросу
            userPollState.currentQuestionIndex++
        }

        if (userPollState.currentQuestionIndex < questionsFromDb.size) {
            takePollWithParametr(bot, chatId)
        } else {
            // Получение индивидуальной статистики
            val number: Double = userPollState.rightCurrentCompletion / userPollState.currentQuestionIndex
            val competitionPercentInAll = number * 100 // в целых процентах 00.0
            val testNameFromDb = userState.testId?.let { dbQuestionHelper.readTestName(it) }.toString()

            // Клавиатура для удаление всех пройденных вопросов по желанию
            val keyboard = InlineKeyboardMarkup().apply {
                keyboard = listOf(
                    listOf(
                        InlineKeyboardButton("Стереть все ответы \uD83D\uDD25").apply { callbackData = "cleanAllAnswers_${userState.generatedResultId}" }
                    )
                )
            }
            userState.congratulationsMessage = if (testNameFromDb != "") {
                // Редактирование сообщения результатов (без функционала "стереть всё") при условии начиличия названия теста
                """
                        Поздравляем! 🎉
                        Вы успешно прошли <b>${competitionPercentInAll.toInt()}%</b> теста.
                        Тест: <b><i>"$testNameFromDb"</i></b>
                    """.trimIndent()
            } else {
                // Редактирование сообщения результатов (без функционала "стереть всё") без названия теста
                """
                        Поздравляем! 🎉
                        Вы успешно прошли <b>${competitionPercentInAll.toInt()}%</b> теста.
                    """.trimIndent()
            }
            userState.resultOfTakingPollMID = sendMessage(
                bot,
                chatId,
                userState.congratulationsMessage,
                inlineKeyboard = keyboard,
                parseMode = ParseMode.HTML
            )?. messageId ?: 0

            // Связываем resultId с информацией о сообщениях
            userPollState.mapResultIdFireMID[userState.generatedResultId] = FireMessageInfo(
                listOfPollMIDForFire = userPollState.listOfPollMID.toMutableList(), // Передаем копию списка
                resultOfTakingPollMID = userState.resultOfTakingPollMID, // Сохраняем MID результата
                resultOfTakingPollText = userState.congratulationsMessage // Сохраняем текст результата
            )

            // Очищаем список MID
            userPollState.listOfPollMID.clear()

            // Увеличение количества истраченных попыток
            userState.mapTestIdNumAttempts[userState.testId!!] = Generation.ResultAndSecurity(
                maxNumberOfAttempts = dbQuestionHelper.getAttemptsCount(userState.testId!!) ?: Int.MAX_VALUE,
                currentNumberOfAttempts = 1 + (userState.mapTestIdNumAttempts[userState.testId]?.currentNumberOfAttempts ?: 0)
            )

            // Обнуление пременных-статистики
            userPollState.rightCurrentCompletion = 0.0
            userPollState.currentQuestionIndex = 0

            //Очистка чата и обнуление состояния
            clearPreviousMessages(userState, bot, chatId, userState.supportStartTestTextMID)
            userState.pollCreationState = null

            // Начальная Reply клавиатура
            Generation.somethingElseKeyboardGeneration(bot, chatId, userState)

            // Обновление процента выполения и отправка хозяину теста сообзение о прохождении с информацией
            userState.testId?.let { dbQuestionHelper.updateCompletionPercent(newPercent = competitionPercentInAll, testId = it) }

            // Выдача фоточки при наличии бонусов за успешное прохождение
            val testId = userState.testId
            if (testId != null) {
                val pepperFileId = dbQuestionHelper.getPhotoFileId(testId)
                val targetComPercent = dbQuestionHelper.getTargetComPercent(testId)
                val targetUsername = dbQuestionHelper.getTargetUsername(testId)
                val authorUsername = dbQuiziHelper.readUsernameByChatId(userState.authorChatId)

                if (!pepperFileId.isNullOrEmpty() && targetComPercent != null
                    && competitionPercentInAll >= targetComPercent) {

                    val isTargetUser = targetUsername != null && targetUsername.isNotEmpty() &&
                            pollAnswer.user.userName == targetUsername

                    if (isTargetUser || targetUsername.isNullOrEmpty()) {
                        val message = if (isTargetUser) {
                            """
                                🎉 <b>Ты справился лучше всех!</b>

                                Ты не просто прошел тест, ты покорил его! 💪
                                <b><a href='https://t.me/$authorUsername'>Автор</a></b> приготовил для тебя кое-что особенное... 
                                Загляни под спойлер, если готов к сюрпризу! 👆
                            """.trimIndent()
                        } else {
                            """
                                🎉 <b>Ты молодец!</b>

                                Ты успешно прошел тест и заслужил награду! 🏆
                                <b><a href='https://t.me/$authorUsername'>Автор</a></b> подготовил для тебя небольшой сюрприз... 
                                Осмелишься заглянуть под спойлер? 👆
                            """.trimIndent()
                        }.trimIndent()


                        MediaSender.sendPhoto(
                            bot,
                            chatId,
                            photoUrl = pepperFileId,
                            caption = message,
                            parseMode = ParseMode.HTML,
                            spoiler = true,
                            protectContent = true,
                        )
                    }
                }
            }

            if (userState.authorChatId != 0L) {
                // Функция для экранирования спецсимволов MarkdownV2
                fun escapeMarkdownV2(text: String): String {
                    return text.replace("""([_*\[\]()~`>#+=|{}.!-])""".toRegex()) {
                        "\\${it.value}"
                    }
                }

                // Экранируем username
                val escapedUsername = pollAnswer.user.userName?.let { escapeMarkdownV2(it) } ?: "Пользователь"

                val percentage = competitionPercentInAll.toInt()

                // Экранируем название теста
                val escapedTestName = if (testNameFromDb != "") {
                    escapeMarkdownV2(testNameFromDb)
                } else {
                    "Без названия"
                }

                authorUserState.spoilerResultText = "||*@${escapedUsername}*|| прошёл тест: *\"${escapedTestName}\"* на _${percentage}%_\n"

                authorUserState.linkForReply = "https://t.me/${pollAnswer.user.userName}"

                authorUserState.seeUserResultMID = sendMessage(
                    bot,
                    userState.authorChatId,
                    authorUserState.spoilerResultText,
                    inlineKeyboard = StringForBot.seeSummaryResultIK(userState.generatedResultId, authorUserState.linkForReply),
                    parseMode = ParseMode.MARKDOWNV2
                )?.messageId ?: 0

                // Сохраняем связь resultId с сообщением о результатах
                authorUserState.mapResultIdUserResult[userState.generatedResultId] = UserResultInfo(
                    userResultMID = authorUserState.seeUserResultMID,
                    userResultText = authorUserState.spoilerResultText,
                    userResultLink = authorUserState.linkForReply
                )
            }
        }
    }

    fun takePollWithParametr (
        bot: TelegramLongPollingBot,
        chatId: Long,
    ) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }
        val userPollState = userPollStates.getOrPut(chatId) { UserPollState() }

        val questionsFromDb = dbQuestionHelper.readQuestionFromQuestionDb(userState.testId) // отдельный объект с вопросами по testId
        if (questionsFromDb.isNotEmpty()) {

            val currentQuestion = questionsFromDb[userPollState.currentQuestionIndex]

            val pollMID = submitPoll(
                bot = bot,
                chatId = chatId,
                question = currentQuestion.questionText,
                options = currentQuestion.listOfAnswers,
                type = "quiz",
                correctAnswerId = currentQuestion.indexOfRightAnswer,
                explanation = "Правильный ответ: ${currentQuestion.listOfAnswers[currentQuestion.indexOfRightAnswer]}",
                openPeriod = 90
            )?.messageId ?: 0


            // Добавляем MID опроса в список для последующего удаления
            userPollState.listOfPollMID.add(pollMID)

        } else {
            sendMessage(bot, chatId, "Теста по данной ссылке не существует\nПожалуйста убедитесь, что Вам отправили достоверную ссылку.")
            Animation.homeStickerAnimation(bot, userState, chatId)
        }

    }

    fun sendQuestionsAnswerChoice(
        bot: TelegramLongPollingBot,
        chatId: Long,
    ) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Отдельный объект с вопросами по testId
        val questionsFromDb = dbQuestionHelper.readQuestionFromQuestionDb(userState.testId)
        val currentQuestion = questionsFromDb[userState.currentQuestionIndex]

        val keyboard = InlineKeyboardMarkup().apply {
            keyboard = currentQuestion.listOfAnswers.mapIndexed { index, answer ->
                listOf( // передача верного варианта ответа до optionRMP
                    InlineKeyboardButton(answer).apply { callbackData = "${index + 1}optionRMP${chatId}" }
                )
            }
        }
        userState.choiceTheCorrectAnswerMID = sendMessage(
            bot,
            chatId,
            currentQuestion.questionText,
            inlineKeyboard = keyboard
        )?.messageId ?: 0
    }
}