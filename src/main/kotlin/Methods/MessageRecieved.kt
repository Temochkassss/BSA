package Methods

import Database.Question.CharForQuestionDb
import Database.Question.DatabaseQuestionHelper
import Database.Quizi.CharForQuiziDb
import Database.Quizi.DatabaseQuiziHelper
import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.deleteMessage
import Methods.TextSender.deleteMessagesSafely
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import StringForBot
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton


object MessageRecieved {

    val dbQuiziHelper = DatabaseQuiziHelper(CharForQuiziDb.DATABASE_NAME)
    val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)


    fun onMessageReceived(bot: TelegramLongPollingBot, update: Update) {
        val message = update.message
        val chatId = message.chatId
        val text = message.text

        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        val state = userState.pollCreationState
        /**  Есть ли в словаре информация о процессе СОЗДАНИЯ теста для данного пользователя  **/
        when (state) {
            is CallbackData.PollCreationState.WaitingForQuestion -> handleWaitingForQuestion(bot, chatId, message, text)
            is CallbackData.PollCreationState.WaitingForAnswers -> handleWaitingForAnswers(bot, chatId, state, message, text)
            is CallbackData.PollCreationState.WaitingForCorrectAnswer -> handleWaitingForCorrectAnswer(bot, chatId, text, state)
            is CallbackData.PollCreationState.WaitingForNameTest -> handleWaitingForNameTest(bot, update, chatId, text)
            is CallbackData.PollCreationState.WaitingForUrlOrUsername -> handleWaitingForUrlOrUsername(bot, update, chatId, text)
            is CallbackData.PollCreationState.WaitingCustomDonationSum -> handleWaitingCustomDonationSum(bot, update, chatId, text)
            is CallbackData.PollCreationState.WaitingForTargetUsername -> handleWaitingForTargetUsername(bot, update, chatId, text)
            is CallbackData.PollCreationState.WaitingForPhoto -> deleteMessage(bot, chatId, update.message.messageId)
            else -> {
                // TODO: (Обратотка иного состояния создания вопроса)
            }
        }
    }

    private fun handleWaitingForQuestion (bot: TelegramLongPollingBot, chatId: Long, message: Message, text: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Если мы не в процессе возврата к пред-состояниям
        if (!userState.isBackInCreatingPoll) {
            // Переход к следующему состоянию + передача данных
            userState.pollCreationState = CallbackData.PollCreationState.WaitingForAnswers(text.capitalize())

            val enterInSepateLinesMID = TextSender.sendMessage(
                bot,
                chatId,
                "Пожалуйста, введите ответы на вопрос. Каждый ответ с новой строки",
                replyKeyboard = StringForBot.returnOrFinishRK()
            )?. messageId ?: return
            MessageManager.addMessageToDelete(chatId,enterInSepateLinesMID)
            MessageManager.addMessageToDelete(chatId,message.messageId) // введённое пользователем сообщение
        }
    }

    private fun handleWaitingForAnswers (bot: TelegramLongPollingBot, chatId: Long, state: CallbackData.PollCreationState.WaitingForAnswers, message: Message, text: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Разделение строки по \n в виде списка
        val options = text.split("\n")
            .map { it.trim().capitalize() }
            .filter { it.isNotEmpty() }

        if (options.size < 2) {

            val d3 = sendMessage(
                bot,
                chatId,
                "Пожалуйста, введите еще ответы. Должно быть не менее 2 ответов.",
                replyKeyboard = StringForBot.returnOrFinishRK()
            )?.messageId ?: 0

            MessageManager.addMessageToDelete(chatId,d3)
            MessageManager.addMessageToDelete(chatId,message.messageId) // введённое пользователем сообщение
        } else {

            // Создание клавиатуры выбора верного ответа
            val keyboard = InlineKeyboardMarkup().apply {
                keyboard = options.mapIndexed { index, option -> // определённое действие к каждому элементу списка, используя как сам элемент, так и его индекс, и создать новый список на основе результатов
                    listOf(InlineKeyboardButton(option).apply {
                        callbackData = (index + 1).toString()
                    })
                }
            }

            // Переход к следующему состоянию + передача данных
            userState.pollCreationState = CallbackData.PollCreationState.WaitingForCorrectAnswer(state.question, options)

            userState.chooseYourAnswerMID = TextSender.sendMessage(
                bot,
                chatId,
                "Пожалуйста, выберите верный вариант:",
                inlineKeyboard = keyboard
            )?.messageId ?: 0

            deleteMessage(bot, chatId, message.messageId) // удаление введённого пользователем сообщения
        }
    }

    private fun handleWaitingForCorrectAnswer (bot: TelegramLongPollingBot, chatId: Long, text: String, state: CallbackData.PollCreationState.WaitingForCorrectAnswer) {
        // TODO: (былое внесение верного индекса, сейчас смотри конец onCallbackData)
    }

    fun handleWaitingForNameTest(bot: TelegramLongPollingBot, update: Update? = null, chatId: Long, text: String?) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)
        dbQuestionHelper.updateTestName(
            testName = text,
            testId = userState.generatedTestId
        )

        // Если не было пропуска именования
        if (!userState.skipNamingTest) {
            // Сохраняем название теста
            userState.testName = text
            userState.theTestHaveTitle = true
        }

        // Удаление введённого названия
        if (update != null) {
            deleteMessage(bot, chatId, update.message.messageId)
        }
        // Очищаем сообщения
        clearPreviousMessages(userState, bot, chatId, userState.adviceToSkipNamingTestMID)

        // При условии, что первый вопрос создан до конца
        if (userState.countCreatingPollIndividually > 0) {
            // Отправка сообщения с ссылкой для прохождения
            Generation.htmlUrlForTestGeneration(bot, chatId, userState, text)
            userState.countCreatingPollIndividually = 0
        } else {
            deleteMessagesSafely(bot, chatId)
            userState.keyboardAfterMainMenuMID = sendMessage(
                bot,
                chatId,
                "Вы пока не создали ни одного теста\nПредлагаю посмотреть готовые \uD83D\uDC47",
                inlineKeyboard = StringForBot.createTestIK(),
            )?.messageId ?: 0
        }

        // Завершаем состояния создания вопроса
        userState.pollCreationState = null
        userState.testName = ""
    }

    private fun handleWaitingForUrlOrUsername(bot: TelegramLongPollingBot, update: Update, chatId: Long, text: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Предотвращаем запуски иных состояний
        userState.pollCreationState = null

        if (text.startsWith("\uD83C\uDF1FПоздравляем") || text.startsWith("https://t.me/and_she_will_ask_bot?start=")){
            parseUrlAgain(bot, chatId, text)
        } else {
            // Удаление сообщения пользователя с username автора
            deleteMessage(bot, chatId, update.message.messageId)

            // Функция обработки username
            analyzeUsername(bot, update, chatId, text)
        }
    }



    private fun parseUrlAgain(bot: TelegramLongPollingBot, chatId: Long, message: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }
        val userPollState = PollSender.userPollStates.getOrPut(chatId) { PollSender.UserPollState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.testInstructionsMID)

        when {
            message.startsWith("https://t.me/and_she_will_ask_bot?start=") -> {
                val parts = message.substringAfter("=").split("_")

                // Занесение данных в переменные состояния
                userState.testId = parts[1]
                userState.authorChatId = parts[0].toLong()
                userPollState.currentQuestionIndex = 0 // Необходимость под вопросом

                // Приветственное сообщение о начале теста и анимация начала соот-но
                userState.supportStartTestTextMID = TextSender.sendMessage(
                    bot,
                    chatId,
                    StringForBot.SUPPORT_TESTS_MESSAGE,
                )?.messageId ?: 0
                Animation.startTestAnimation(bot, chatId, userState)
            }
            message.startsWith("\uD83C\uDF1FПоздравляем") -> {

                val clickButtonText = "Нажмите на кнопку \"Пройти тест \uD83C\uDFAF\" выше."

                userState.clickTheButtonTextMID = TextSender.sendMessage(
                    bot,
                    chatId,
                    clickButtonText,
                    parseMode = ParseMode.HTML
                )?.messageId ?: 0
            }
            else -> {
                TextSender.sendMessage(
                    bot,
                    chatId,
                    "Ошибка считывания ссылки.\nУбедитесь, что переданная вами ссылка корректна"
                )
            }
        }

        TextSender.deleteMessagesSafely(bot, chatId)
    }

    private fun analyzeUsername(bot: TelegramLongPollingBot, update: Update, chatId: Long, text: String) {
        // Состояния пользователя, желающего пройти тест
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Username и chatId автора теста
        userState.authorUsername = text.trim('@')
        userState.authorChatId = dbQuiziHelper.readChatIdByUsername(userState.authorUsername)

        // Состояния автора теста
        val authorUserState = CallbackData.userStates.getOrPut(userState.authorChatId) { CallbackData.UserState() }

        // Список testId тестов созданных автором
        authorUserState.usersTestsMutableList = dbQuiziHelper.readArrayOfTestId(userState.authorChatId).filterNotNull()

        println("""
            Данные автора теста:
            userState.authorChatId = ${userState.authorChatId}
            userState.authorUsername = ${userState.authorUsername}
            authorUserState.usersTestsMutableList = ${authorUserState.usersTestsMutableList}
        """.trimIndent())

        // В случае если автор теста не создал ни одного теста
        if (authorUserState.usersTestsMutableList.isEmpty()) {
            // Удаление старого и отправка нового сообщения с последующей анимацией
            clearPreviousMessages(userState, bot, chatId, userState.testInstructionsMID)
            userState.testInstructionsMID = sendMessage(
                bot,
                chatId,
                """
                    Ой! @${userState.authorUsername} еще 
                    не успел создать тесты 🥺💫
                    
                    Создайте свой тест прямо 
                    сейчас и вдохновите других!
                """.trimIndent(),
                inlineKeyboard = StringForBot.createTestOnlyIK(),
                parseMode = ParseMode.HTML
            )?. messageId ?:  0

            // Сращивание параметров для грамотной работы бота
            userState.keyboardAfterMainMenuMID = userState.testInstructionsMID

            return
        }

        // Запускаем анимацию ожидания
        userState.waitingAnimationJob = Animation.startWaitingAnimation(bot, chatId, userState)

        // Сопроводительный текст для открытия доступа
        val authorAlertForChoose = """
            Пользователь <i><b>@${update.message.from.userName}</b></i> желает пройти один из Ваших тестов. Выберете какой именно:
        """.trimIndent()
        val ignoreMessageIfAnonimus = """
              <i>* Запретите прохождение, если Вы против прохождения теста данным человеком!</i>
        """.trimIndent()

        userState.authorAlertForChooseMID = sendMessage(
            bot,
            userState.authorChatId,
            authorAlertForChoose,
            inlineKeyboard = StringForBot.windowGetRuleForTakeTestIK(authorUserState, chatId),
            parseMode = ParseMode.HTML
        )?.messageId ?: 0

        // Временное сообщение об опасности (10 сек.)
        userState.authorIgnoreMessageIfAnonimusMID = sendMessage(
            bot,
            userState.authorChatId,
            ignoreMessageIfAnonimus,
            parseMode = ParseMode.HTML
        )?.messageId ?: 0

    }

    private fun handleWaitingCustomDonationSum(bot: TelegramLongPollingBot, update: Update, chatId: Long, text: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка сообщения пользователя
        deleteMessage(bot, chatId, update.message.messageId)

        // Получение суммы от пользователя
        val price = text.toIntOrNull()

        if (price != null && price > 0) {
            if (price >= 60) {
                // Установка корректной суммы в invoice
//                Payment.donateSetSum(bot, chatId, price.toString())
                // Предотвращаем запуски иных состояний
                userState.pollCreationState = null
            } else {
                deleteMessage(bot, chatId, userState.donateMID)
                userState.donateMID = sendMessage(
                    bot,
                    chatId,
                    """
                        К сожалению, минимальная сумма пожертвования через **Telegram\-бот** составляет **60 RUB**\.\.\.
                        Иные способы поддержки:
                        \- По реквизитам: `5536 9140 5671 8251`
                        \- По номеру телефона: `\+7(911)781-55-36`
                        Либо введите сумму **больше 60 RUB** для пожертвования:
                    """.trimIndent(),
                    replyKeyboard = StringForBot.backRB(),
                    parseMode = ParseMode.MARKDOWNV2,
                )?.messageId ?: 0

                // Оставляем пользователя в состоянии ожидания ввода суммы
                userState.pollCreationState = CallbackData.PollCreationState.WaitingCustomDonationSum()
            }
        } else {
            // Отправляем сообщение с просьбой ввести корректную сумму
            editMessage(
                bot,
                chatId,
                userState.donateMID,
                "Неверно введенное значение. Пожалуйста, введите сумму пожертвования <b>больше 0</b> рублей.",
                parseMode = ParseMode.HTML
            )

            // Оставляем пользователя в состоянии ожидания ввода суммы
            userState.pollCreationState = CallbackData.PollCreationState.WaitingCustomDonationSum()
        }
    }

    private fun handleWaitingForTargetUsername(bot: TelegramLongPollingBot, update: Update, chatId: Long, text: String?) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Удаление введённого значения
        deleteMessage(bot, chatId, update.message.messageId)

        val targetUsername = text?.trim('@')
        val testId = userState.pollCreationState?.let { (it as? CallbackData.PollCreationState.WaitingForTargetUsername)?.testId }
        val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0

        if (targetUsername != null && testId != null) {
            // Сохраняем избранного пользователя
            dbQuestionHelper.saveTagretUsername(testId, targetUsername)
            // Сбрасываем состояние
            userState.pollCreationState = null
            // Возвращаемся к меню
            CallbackData.handleAddPeper(bot, chatId, testId, messageId)
        } else {
            editMessage(
                bot,
                chatId,
                messageId,
                "Некорректный <b>@username</b> пользователя.\nПопробуйте ввести ещё раз!",
                parseMode = ParseMode.HTML
            )
        }
    }
}