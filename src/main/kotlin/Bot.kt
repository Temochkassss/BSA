import Database.Question.CharForQuestionDb
import Database.Question.DatabaseQuestionHelper
import Database.Quizi.CharForQuiziDb
import Database.Quizi.DatabaseQuiziHelper
import Database.Results.CharForResultsDb
import Database.Results.DatabaseResultsHelper
import Methods.*
import Methods.CallbackData.onCallbackData
import Methods.CallbackData.userStates
import Methods.PollSender.analysePollAnswer
import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.deleteMessage
import Methods.TextSender.deleteMessagesSafely
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Основной класс бота, обрабатывающий все входящие сообщения и взаимодействия
 */
class Bot: TelegramLongPollingBot("7110164125:AAFTEP0jd9-peZJDWU6hkQ7v_7qMSZWh7ZU") {

    override fun getBotUsername(): String = "and_she_will_ask_bot"

    override fun onUpdateReceived(update: Update) {

        /**  Создание экземпляров классов, хранящих функционал баз данных **/
        val dbQuiziHelper = DatabaseQuiziHelper(CharForQuiziDb.DATABASE_NAME)
        val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)
        val dbResultsHelper = DatabaseResultsHelper(CharForResultsDb.DATABASE_NAME)

        try {
            when {
                update.hasPreCheckoutQuery() -> {
                    handlePreCheckoutQuery(update.preCheckoutQuery)
                }
                update.hasMessage() -> {
                    if (update.message.hasSuccessfulPayment()) {
                        handleSuccessfulPayment(update.message)
                    } else if (update.message.hasText()) {
                        handleTextMessage(update, dbQuiziHelper, dbQuestionHelper, dbResultsHelper)
                    } else if (update.message.hasPhoto()) { // Добавьте проверку на фото
                        handlePhotoMessage(update, dbQuestionHelper)
                    }
                }
                update.hasCallbackQuery() -> {
                    handleCallbackQuery(update)
                }
                update.hasPollAnswer() -> {
                    handlePollAnswer(update)
                }
            }
        } catch (e: Exception) {
            println("Ошибка в Bot: ${e.message}")
            e.printStackTrace()
        }
    }

    // Функция, которая обрабатывает запрос на предварительную проверку оплаты
    private fun handlePreCheckoutQuery(preCheckoutQuery: PreCheckoutQuery) {
        try {
            println("PreCheckoutQuery received: ${preCheckoutQuery.id}")
            println("Invoice payload: ${preCheckoutQuery.invoicePayload}")
            println("Total amount: ${preCheckoutQuery.totalAmount}")

            // Отправляем ответ сразу, чтобы избежать тайм-аута
            execute(AnswerPreCheckoutQuery.builder()
                .preCheckoutQueryId(preCheckoutQuery.id)
                .ok(true)
                .build())

        } catch (e: Exception) {
            println("PreCheckout error: ${e.message}")
            e.printStackTrace()
            execute(AnswerPreCheckoutQuery.builder()
                .preCheckoutQueryId(preCheckoutQuery.id)
                .ok(false)
                .errorMessage("Ошибка обработки платежа: ${e.message}")
                .build())
        }
    }


    // Функция, которая обрабатывает успешный платеж
    private fun handleSuccessfulPayment(message: Message) {
        val chatId = message.chatId
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        try {
            // Запись в лог
            println("Successful payment: ${message.successfulPayment}")

            // Отправка подтверждения
            userState.successfulPaymentMID = sendMessage(
                this,
                chatId,
                "✅ Платёж успешно проведен! Спасибо за поддержку!"
            )?.messageId ?: 0

            // Дополнительные действия после успешного платежа
            Animation.successfulDonate(this, chatId, userState)

            // Здесь можно добавить логику предоставления доступа к премиум функциям

        } catch (e: Exception) {
            println("Payment processing error: ${e.message}")
            sendMessage(this, chatId, "⚠️ Ошибка обработки платежа. Свяжитесь с поддержкой.")
        }
    }

    private fun handlePhotoMessage(update: Update, dbQuestionHelper: DatabaseQuestionHelper) {
        val message = update.message
        val chatId = message.chatId
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Удаляем отправленное фото
        deleteMessage(this, chatId, message.messageId)

        // Проверяем, находится ли пользователь в состоянии ожидания фото
        if (userState.pollCreationState is CallbackData.PollCreationState.WaitingForPhoto) {
            PhotoRecieved.handleWaitingForPhoto(this, chatId, message)
        } else {
            // Если фото отправлено вне контекста, можно отправить сообщение с подсказкой
            sendMessage(this, chatId, "Пожалуйста, используйте команду \"Добавить перчинки \uD83C\uDF36\" для добавления фото в тест.")
        }
    }


    /**
     * Обработка текстовых сообщений от пользователя
     */
    private fun handleTextMessage(
        update: Update,
        dbQuiziHelper: DatabaseQuiziHelper,
        dbQuestionHelper: DatabaseQuestionHelper,
        dbResultsHelper: DatabaseResultsHelper
    ) {

        val messageText = update.message.text
        val chatId = update.message.chatId
        val user: User? = update.message.from

        // Получаем или создаём экземпляр класса состояния пользователя
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        when (messageText) {
            "/start" -> handleStart(update, dbQuiziHelper, dbQuestionHelper, dbResultsHelper, chatId, user, messageText)

            "Создать тест ✏\uFE0F" -> handleCreateTest(update, chatId)
            "/new_test" -> handleCreateTest(update, chatId)

            "Пройти тест \uD83D\uDCDD" -> handleTakeTest(update, chatId)
            "/play_test" -> handleTakeTest(update, chatId)

            "Мои тесты \uD83D\uDDC2" -> handleCheckMyTests(update, chatId, dbQuiziHelper)
            "/my_tests" -> handleCheckMyTests(update, chatId, dbQuiziHelper)

            "Результаты \uD83D\uDCCA" -> handleCheckMyResults(update, chatId, dbQuiziHelper, dbQuestionHelper, dbResultsHelper)
            "/my_results" -> handleCheckMyResults(update, chatId, dbQuiziHelper, dbQuestionHelper, dbResultsHelper)

            "Оценить ⭐" -> handleRateBot(update, chatId)

            "Отблагодарить \uD83D\uDCB8" -> handleDonate(update, chatId)

            "Главное меню \uD83C\uDFE0" -> handleMainMenu(update, chatId)
            "/menu" -> handleMainMenu(update, chatId)
            "Назад" -> handleBack(update, chatId)
            "Завершить" -> PollSender.handleFinishCreatingPoll(this, update, chatId)

            "/readDb" -> handleReadDb(dbQuiziHelper, dbQuestionHelper)
            "/disDb" -> handleDisconnectDb(dbQuiziHelper, dbQuestionHelper)

            "Лере быстро" -> forLoveAyh()

            else -> {
                if (messageText.startsWith("/start")) {
                    handleStart(update, dbQuiziHelper, dbQuestionHelper, dbResultsHelper, chatId, user, messageText)
                } else {
                    handleDefault(chatId)
                }
            }
        }

        if (!userState.isBackInCreatingPoll) {
            MessageRecieved.onMessageReceived(this, update)
        }
    }

    /**  Обратотка аргумента команды "start"  **/
    private fun handleStartCommand(chatId: Long, messageText: String) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }
        val userPollState = PollSender.userPollStates.getOrPut(chatId) { PollSender.UserPollState() }

        val (parsedChatId, parsedTestId) = parseStartArguments(messageText) ?: run {
            // Если парсинг не удался, обрабатываем как обычную команду start(если передать функцию обработки)
            println("Failed to parse start arguments")
            return
        }

        userState.authorChatId = parsedChatId

        // Дальнейшая обработка chatId и testId
        userState.testId = parsedTestId
        userPollState.currentQuestionIndex = 0

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.testInstructionsMID, userState.clickTheButtonTextMID)

        // Если пользователь хочет пройти свой тест
        if (parsedChatId == chatId) {
            userState.takeMyOwnTestMID = sendMessage(
                this,
                chatId,
                StringForBot.WARNING_PARSE_MY_SELF_TEST,
                inlineKeyboard = StringForBot.takeMyOwnTestIK()
            )?.messageId ?: 0
            return
        }

        // Приветственное сообщение о начале теста
        userState.supportStartTestTextMID = sendMessage(
            this,
            chatId,
            StringForBot.SUPPORT_TESTS_MESSAGE,
        )?.messageId ?: 0
        // Анимация начала соот-но
        Animation.startTestAnimation(this, chatId, userState)
    }

    /**
     * Обработка команды /start и инициализация пользовательской сессии
     */
    private fun handleStart(
        update: Update,
        dbQuiziHelper: DatabaseQuiziHelper,
        dbQuestionHelper: DatabaseQuestionHelper,
        dbResultsHelper: DatabaseResultsHelper,
        chatId: Long,
        user: User?,
        messageText: String
    ) {
        // Инициализация состояния пользователя
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Сброс всех состояний ожидания
        userState.pollCreationState = null

        // Обработка повторного /start
        if (userState.startIsAnnounced) {
            deleteMessage(this, chatId, update.message.messageId)
        }
        userState.startIsAnnounced = true

        // Инициализация баз данных
        initializeDatabases(dbQuiziHelper, dbQuestionHelper, dbResultsHelper)

        // Сохранение первичной информации о пользователе
        saveUserInfo(dbQuiziHelper, chatId, user, userState)

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.keyboardAfterMainMenuMID, userState.startNowMID, userState.homeStickerAnimationMID, userState.somethingElseMID)

        // Проверка на deep-linking
        if (messageText.contains(" ")) {
            handleStartCommand(chatId, messageText)
            return  // Прерываем выполнение обычной обработки start
        }

        // Отправка приветственного сообщения
        Generation.welcomeMessageGeneration(this, chatId, userState)
    }

    /**
     * Инициализация баз данных
     */
    private fun initializeDatabases(
        dbQuiziHelper: DatabaseQuiziHelper,
        dbQuestionHelper: DatabaseQuestionHelper,
        dbResultsHelper: DatabaseResultsHelper,
    ) {
        dbQuiziHelper.apply {
            connectToQuziDb()
            createQuiziTable()
        }
        dbQuestionHelper.apply {
            connectToQuestionDb()
            createQuestionTable()
        }
        dbResultsHelper.apply {
            connectToResultsDb()
            createResultsDb()
        }
    }

    /**
     * Сохранение первичной информации о пользователе
     */
    private fun saveUserInfo(
        dbQuiziHelper: DatabaseQuiziHelper,
        chatId: Long,
        user: User?,
        userState: CallbackData.UserState
    ) {
        val isPremium: Boolean = !(user?.isPremium == false || user?.isPremium == null)
        val isBot: Boolean = !(user?.isBot == false || user?.isBot == null)
        // две идентичные записи: val isBot: Boolean = if (user?.isBot == false || user?.isBot == null) false else true
        userState.userInformation = user

        dbQuiziHelper.insertUser(
            chatId,
            "${user?.userName}",
            "${user?.firstName}",
            "${user?.languageCode}",
            isPremium,
            isBot,
        )
    }

    /**
     * Парсинг аргументов входного сообщения
     */
    private fun parseStartArguments(message: String): Pair<Long, String>? {
        // Разбиваем сообщение на части
        val parts = message.split(" ")
        if (parts.size != 2 || !parts[0].startsWith("/start")) {
            return null
        }

        val startArguments = parts[1]
        val argumentParts = startArguments.split("_")
        if (argumentParts.size != 2) {
            return null
        }

        val parsedChatId = argumentParts[0].toLongOrNull()
        val testId = argumentParts[1]

        return if (parsedChatId != null) {
            Pair(parsedChatId, testId)
        } else {
            null
        }
    }

    private fun handleCreateTest(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }
        userState.processCreatingPoll = true

        // Генерация testId с проверкой
        Generation.randomTestIdGeneration(chatId)

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.mainMenuMID, userState.startNowMID, userState.homeStickerAnimationMID, userState.myTestsMID, userState.somethingElseMID, userState.testIsClosedMID, userState.donateMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        // Косметический номер вопроса
        userState.questionNumberInCreating = 1

        /** Если клавиатура СОЗДАНИЯ теста присутствует, то мы его обновляем
         * (редактируем при присутствии, отправляем при отсутствии) **/
        if (userState.keyboardAfterMainMenuMID != 0) {
            editMessage(
                this,
                chatId,
                userState.keyboardAfterMainMenuMID,
                "Как именно Вы хотите создать?",
                inlineKeyboard = StringForBot.createTestIK(),
            )
        } else {
            userState.keyboardAfterMainMenuMID = sendMessage(
                this,
                chatId,
                "Как именно Вы хотите создать?",
                inlineKeyboard = StringForBot.createTestIK(),
            )?.messageId ?: 0
        }

        // Очищаем чат от функционально значимого слова "Создать тест"
        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleTakeTest(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.mainMenuMID, userState.startNowMID, userState.homeStickerAnimationMID, userState.myTestsMID, userState.somethingElseMID, userState.testIsClosedMID, userState.donateMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        /** Если клавиатура ПРОХОЖДЕНИЯ теста присутствует, то мы его обновляем
         * (редактируем при присутствии, отправляем при отсутствии) **/
        if (userState.keyboardAfterMainMenuMID != 0) {
            editMessage(
                this,
                chatId,
                userState.keyboardAfterMainMenuMID,
                "Какой именно Вы хотите пройти?",
                inlineKeyboard = StringForBot.takeTestIK()
            )
        } else {
            userState.keyboardAfterMainMenuMID = sendMessage(
                this,
                chatId,
                "Какой именно Вы хотите пройти?",
                inlineKeyboard = StringForBot.takeTestIK()
            )?.messageId ?: 0
        }

        // Очищаем чат от функционально значимого слова "Пройти тест"
        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleCheckMyTests(update: Update, chatId: Long, dbQuiziHelper: DatabaseQuiziHelper) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущего окна "Мои тесты"
        clearPreviousMessages(userState, this, chatId, userState.mainMenuMID, userState.homeStickerAnimationMID, userState.somethingElseMID, userState.startNowMID, userState.myTestsMID, userState.keyboardAfterMainMenuMID, userState.testIsClosedMID, userState.donateMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        // Окно с тестами пользователя
        StringForBot.showWindowsWithMyTests(this, chatId, dbQuiziHelper)

        // Удаляем системно значимое слово "Мои тесты"
        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleCheckMyResults(update: Update, chatId: Long, dbQuiziHelper: DatabaseQuiziHelper, dbQuestionHelper: DatabaseQuestionHelper, dbResultsHelper: DatabaseResultsHelper) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState()}

        // Очистка предыдущих сообщений
        clearPreviousMessages(
            userState,
            this,
            chatId,
            userState.mainMenuMID,
            userState.homeStickerAnimationMID,
            userState.somethingElseMID,
            userState.startNowMID,
            userState.myTestsMID,
            userState.keyboardAfterMainMenuMID,
            userState.testIsClosedMID,
            userState.donateMID,
            userState.takeMyOwnTestMID,
            userState.myResultsMID
        )

        // Окно с результатами пользователя
        StringForBot.showWindowOfResults(this, chatId, dbQuiziHelper, dbQuestionHelper, dbResultsHelper)

        // Удаляем системно значимое слово "Результаты"
        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleRateBot(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.rateBotMID, userState.testIsClosedMID, userState.donateMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        val rateText = """
            Мы стремимся сделать нашего бота лучше и будем очень признательны за вашу оценку.
        
            Пожалуйста, оцените нашего бота:
        """.trimIndent()

        userState.rateBotMID = sendMessage(
            this,
            chatId,
            rateText,
            inlineKeyboard = StringForBot.rateBotIK()
        )?.messageId ?: 0

        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleDonate(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.mainMenuMID, userState.startNowMID, userState.homeStickerAnimationMID, userState.myTestsMID, userState.somethingElseMID, userState.testIsClosedMID, userState.donateMID, userState.keyboardAfterMainMenuMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        val supportMessage = """
            🎨 С вашей поддержкой анимации станут более плавными, а мои лапки перестанут путаться в коде 😺
           
            💝 Спасибо, что вы с нами! 
        """.trimIndent()

        userState.donateMID = sendMessage(
            this,
            chatId,
            supportMessage,
            inlineKeyboard = StringForBot.donateSomeSumIK()
        )?.messageId ?: 0

        Generation.somethingElseKeyboardGeneration(this, chatId, userState)

        // очищаем чат после функционально значимого слова "Отблагодарить"
        deleteMessage(this, chatId, update.message.messageId)
    }


    private fun handleMainMenu(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, this, chatId, userState.mainMenuMID, userState.keyboardAfterMainMenuMID, userState.homeStickerAnimationMID, userState.startNowMID, userState.somethingElseMID, userState.myTestsMID, userState.testIsClosedMID, userState.donateMID, userState.takeMyOwnTestMID, userState.myResultsMID)

        // Блок "анимации" по возврату в главное меню
        Animation.homeStickerAnimation(this, userState, chatId)

        // очищаем чат после функционально значимого слова "Главное меню"
        deleteMessage(this, chatId, update.message.messageId)
    }

    private fun handleBack(update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очищаем чат от функционально значимого слова "Назад"
        deleteMessage(this, chatId, update.message.messageId)

        // Блокировка повторного запуска состояний
        userState.isBackInCreatingPoll = true
        userState.isBackInCreatingPollForChoosingAnswers = true

        when (val currentState = userState.pollCreationState) {
            // Обработка начального состояния "Ожидание вопроса"
            is CallbackData.PollCreationState.WaitingForQuestion -> {
                userState.pollCreationState = null
                userState.processCreatingPoll = false

                val creatingPollIsStoped = sendMessage(
                    this,
                    chatId,
                    "Создание вопроса отменено."
                )?.messageId ?: 0
                MessageManager.addMessageToDelete(chatId,creatingPollIsStoped)

                // Удаление сообщений-процесса СОЗДАНИЯ (до завершения создания)
                deleteMessagesSafely(this, chatId)

                clearPreviousMessages(userState, this, chatId, userState.keyboardAfterMainMenuMID)
                userState.keyboardAfterMainMenuMID = sendMessage(
                    this,
                    chatId,
                    "Как именно Вы хотите создать?",
                    inlineKeyboard = StringForBot.createTestIK()
                )?.messageId ?: 0
            }

            // Обработка состояния "Ожидание ответов"
            is CallbackData.PollCreationState.WaitingForAnswers -> {

                // Возврат к предыдущему состоянию
                userState.pollCreationState = CallbackData.PollCreationState.WaitingForQuestion(null)
                val d4 = sendMessage(
                    this,
                    chatId,
                    "Пожалуйста, введите вопрос для опроса.",
                    replyKeyboard = StringForBot.returnOrFinishRK()
                )?.messageId ?: 0
                MessageManager.addMessageToDelete(chatId,d4)
            }

            // Обработка состояния "Ожидание правильного ответа"
            is CallbackData.PollCreationState.WaitingForCorrectAnswer -> {
                // Удаление прошлой клавиатуры-выбора правильного ответа
                deleteMessage(this, chatId, userState.chooseYourAnswerMID)
                // Возврат к предыдущему состоянию с сохранением данных вопроса
                userState.pollCreationState = CallbackData.PollCreationState.WaitingForAnswers(currentState.question)
                if (userState.isBackInCreatingPollForChoosingAnswers) {
                    val enterInSepateLinesMessageId = sendMessage(
                        this,
                        chatId,
                        "Пожалуйста, введите ответы на вопрос. Каждый ответ с новой строки.",
                        replyKeyboard = StringForBot.returnOrFinishRK()
                    )?. messageId ?: return
                    MessageManager.addMessageToDelete(chatId,enterInSepateLinesMessageId)
                }
            }
            is CallbackData.PollCreationState.WaitingForUrlOrUsername -> {
                // Завершаем работу состояний
                userState.pollCreationState = null
                // Очищаем сопроводительные сообщения
                clearPreviousMessages(userState, this, chatId, userState.testInstructionsMID)
                // Возвращемся на главный экран
                Animation.homeStickerAnimation(this, userState, chatId)
            }
            is CallbackData.PollCreationState.WaitingCustomDonationSum -> {
                // Завершаем работу состояний
                userState.pollCreationState = null
                // Очищаем сопроводительные сообщения
                clearPreviousMessages(userState, this, chatId, userState.donateMID)
                // Возвращемся на главный экран
                Animation.homeStickerAnimation(this, userState, chatId)
            }
            else -> {
//                 Обработка иных состояний для reply-кнопки "Назад")
            }
        }
    }

    private fun handleReadDb(dbQuiziHelper: DatabaseQuiziHelper, dbQuestionHelper: DatabaseQuestionHelper) {
        dbQuiziHelper.readUsersFromQuiziDb()
        dbQuestionHelper.readQuestionFromQuestionDb()
    }

    private fun handleDisconnectDb(dbQuiziHelper: DatabaseQuiziHelper, dbQuestionHelper: DatabaseQuestionHelper) {
        dbQuiziHelper.disconnectFromQuiziDb()
        dbQuestionHelper.disconnectFromQuestionDb()
    }

    /**
     * Обработка состояния по умолчанию
     */
    private fun handleDefault(chatId: Long) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Сброс флагов состояния опроса (выключаем возможность возврата к предыдущим состояниям)
        userState.apply {
            isBackInCreatingPoll = false
            isBackInCreatingPollForChoosingAnswers = false
        }
    }

    private fun handleCallbackQuery(update: Update) {
        onCallbackData(this, update, update.callbackQuery)
    }

    private fun handlePollAnswer(update: Update) {
        val pollAnswer = update.pollAnswer //  информация об ответе пользователя на опрос
        analysePollAnswer(this, update, pollAnswer)
    }

    private fun forLoveAyh() {
        sendMessage(this, 857844961, "Hey chponya! Ia tyt reshil ylibnyt tebya i chyt ne slomal yazik. Oh yzh etot poliskiy.\n * Pridumau nam mega svidanie po tvoemy priezde!!!!!")
        MediaSender.sendPhoto(this,857844961, File("src/main/resources/Img/cat12.jpg"), caption = "Не смотря на то что сейчас Вы мягкая, теплая и неболтушка.\nЭти ушки рады слышать Ваше мурчание каждый раз!!!", spoiler = true)
        MediaSender.sendSticker(this, 857844961, "CAACAgQAAxkBAAENc0pnerQDHVRDtCZ5pybaS0nUYwTzUwAC4RAAAkb9CFJ1ox2Px1cdYTYE", delay = 3500)

    }
}


