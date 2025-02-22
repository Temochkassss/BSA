package Methods

import Database.Question.CharForQuestionDb
import Database.Question.DatabaseQuestionHelper
import Database.Quizi.CharForQuiziDb
import Database.Quizi.DatabaseQuiziHelper
import Database.Results.CharForResultsDb
import Database.Results.DatabaseResultsHelper
import Methods.MediaSender.sendMediaGroupFromUrls
import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.deleteMessage
import Methods.TextSender.deleteMessagesSafely
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import StringForBot
import kotlinx.coroutines.Job
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*


object CallbackData {

    /**  Класс глобальных переменных  **/
    data class UserState(
        // Группа базовой информации о пользователе
        var userInformation: User? = null,
        var connection: Connection? = null,

        // Группа состояния создания опроса
        var pollCreationState: PollCreationState? = null,
        var processCreatingPoll: Boolean = true,
        var currentQuestionIndex: Int = 0,

        // Группа ID сообщений для управления интерфейсом
        var startIsAnnounced: Boolean = false,
        var mainMenuMID: Int = 0,
        var somethingElseMID: Int = 0,
        var keyboardAfterMainMenuMID: Int = 0,
        var startNowMID: Int = 0,
        var homeStickerAnimationMID: Int = 0,
        var chooseYourAnswerMID: Int = 0,
        var supportStartTestTextMID: Int = 0,

        // Группа параметров процесса создания теста
        var isBackInCreatingPoll: Boolean = false,
        var isBackInCreatingPollForChoosingAnswers: Boolean = true,
        var countCreatingPollIndividually: Int = 0,
        var questionNumberInCreating: Int = 1,
        var theTestHaveTitle: Boolean = false,
        var testName: String? = "",
        var testId: String? = null,
        var generatedTestId: String? = null,
        var generatedResultId: String = "",
        var existingTestIdCount: Int? = null,
        var existingResultIdCount: Int? = null,
        var skipNamingTest: Boolean = false,
        var adviceToSkipNamingTestMID: Int = 0,
        var seeMyAnswersMID: Int = 0,
        var aboutRMPMID: Int = 0,
        var urlMID: Int = 0,
        var theLastPhotoMID: Int = 0,

        // Группа параметров процесса прохождения теста
        var takingTestName: String? = "",
        var totalSystemTests: Int = 0,
        var testsPerPage: Int = 0,
        var totalPages: Int = 0,
        var authorChatId: Long = 0,
        var authorUsername: String? = "",
        var username: String = "",
        var currentDate: String = "",
        var testInstructionsMID: Int = 0,
        var clickTheButtonTextMID: Int = 0,
        var authorAlertForChooseMID: Int = 0,
        var authorIgnoreMessageIfAnonimusMID: Int = 0,
        var userThatWantToTakeMyTestChatId: Long = 0,
        var seeUserResultMID: Int = 0,
        var waitingAnimationJob: Job? = null,
        var resultOfTakingPollMID: Int = 0,
        var congratulationsMessage: String = "",
        var spoilerResultText: String = "",
        var linkForReply: String = "",
        var shouldSendNewMessage: Boolean = false,
        var testIsClosedMID: Int = 0,
        var takeMyOwnTestMID: Int = 0,
        var currentTimestamp: Long = 0L,
        var needImageForTest: Boolean = false,
        var systemTestId: Int = 0,
        var currentPageTests: List<SystemTest> = emptyList(),

        // Группа для работы с тестами пользователя
        var usersTestsMutableList: List<String> = emptyList(),
        var usersResultsMutableList: List<String> = emptyList(),

        var mapResultIdUserResult: MutableMap<String, PollSender.UserResultInfo> = mutableMapOf(),
        var mapTestIdLinkAnswerInfo: MutableMap<String, Generation.UrlAnswersInfo> = mutableMapOf(),
        var mapTestIdNumAttempts: MutableMap<String, Generation.ResultAndSecurity> = mutableMapOf(),

        var completionPercent: Double = 0.0,
        var myTestsMID: Int = 0,
        var myResultsMID: Int = 0,

        // Группа для работы с удалением теста
        var testIdForDeleting: String = "",
        var resultIdForRemove: String = "",

        // Группа ID сообщений для управления готовыми опросами
        var chooseReadyMadePhotoList: MutableList<Int> = mutableListOf(),
        var chooseReadyMadePollKeyboardMID: Int = 0,
        var choiceTheCorrectAnswerMID: Int = 0,
        var pageNumberForRMP: Int = 1,

        // Группа для работы с оценкой бота
        var rateBotMID: Int = 0,

        // Группа для работы донатом
        var donateMID: Int = 0,
        var donationSum: Int = 100,
        var successfulPaymentMID: Int = 0,
        var successfulPaymentStickerMID: Int = 0,

        // Группа для работы с фотографией
        var currentHold: Int = 70,
        var currentAttempt: Int = 3,
        ) {
        fun clearMessageId(messageId: Int) {
            when (messageId) {
                mainMenuMID -> mainMenuMID = 0
                somethingElseMID -> somethingElseMID = 0
                keyboardAfterMainMenuMID -> keyboardAfterMainMenuMID = 0
                startNowMID -> startNowMID = 0
                homeStickerAnimationMID -> homeStickerAnimationMID = 0
                chooseYourAnswerMID -> chooseYourAnswerMID = 0
                supportStartTestTextMID -> supportStartTestTextMID = 0
                testInstructionsMID -> testInstructionsMID = 0
                clickTheButtonTextMID -> clickTheButtonTextMID = 0
                authorAlertForChooseMID -> authorAlertForChooseMID = 0
                myTestsMID -> myTestsMID = 0
                myResultsMID -> myResultsMID = 0
                chooseReadyMadePollKeyboardMID -> chooseReadyMadePollKeyboardMID = 0
                choiceTheCorrectAnswerMID -> choiceTheCorrectAnswerMID = 0
                seeUserResultMID -> seeUserResultMID = 0
                rateBotMID -> rateBotMID = 0
                resultOfTakingPollMID -> resultOfTakingPollMID = 0
                testIsClosedMID -> testIsClosedMID = 0
                adviceToSkipNamingTestMID -> adviceToSkipNamingTestMID = 0
                takeMyOwnTestMID -> takeMyOwnTestMID = 0
                donateMID -> donateMID = 0
                successfulPaymentMID -> successfulPaymentMID = 0
                successfulPaymentStickerMID -> successfulPaymentStickerMID = 0
                seeMyAnswersMID -> seeMyAnswersMID = 0
                aboutRMPMID -> aboutRMPMID = 0
                urlMID -> urlMID = 0
                theLastPhotoMID -> theLastPhotoMID = 0
            }
        }
    }
    /**  Словарь для хранения глобальных переменных пользователей  **/
    val userStates = mutableMapOf<Long, UserState>()

    data class SystemTest(
        val testId: Int,
        val testName: String,
        val imageUrl: String
    )

    /**  Переменные для работы с базой данных + анализом опросов  **/
    val dbQuiziHelper = DatabaseQuiziHelper(CharForQuiziDb.DATABASE_NAME)
    val dbQuestionHelper = DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME)
    val dbResultsHelper = DatabaseResultsHelper(CharForResultsDb.DATABASE_NAME)

    const val RATE_PREFIX = "stars_"  // Единый префикс для всех callback'ов рейтинга

    /**Различные состояния процесса СОЗДАНИЯ опроса
     *
     *PollCreationState - состояние создания опроса  **/
    sealed class PollCreationState {
        data class WaitingForQuestion(val question: String? = null) : PollCreationState() // ожидание ввода вопроса
        data class WaitingForAnswers(val question: String) : PollCreationState() // ожидание ввода вариантов ответов и хранение введённого вопроса
        data class WaitingForCorrectAnswer(val question: String, val options: List<String> ) : PollCreationState() // ожидание выбора правильного ответа и хранение предшествующих данный
        data class WaitingForNameTest(val testName: String? = "") : PollCreationState()
        data class WaitingForUrlOrUsername(val message: String? = "") : PollCreationState()
        data class WaitingCustomDonationSum(val sum: Int? = 0) : PollCreationState()
        data class WaitingForPhoto(val testId: String) : PollCreationState()
        data class WaitingForTargetUsername(val testId: String) : PollCreationState()
    }

    // Создаем Map для хранения данных
    val callbackStorage = mutableMapOf<String, TestData>()

    // Класс для хранения данных
    data class TestData(
        val chatId: Long = 0,
        val testName: String? = "",
        val testId: String = ""
    )

    fun onCallbackData(bot: TelegramLongPollingBot, update: Update, callbackQuery: CallbackQuery) {

        val chatId = callbackQuery.message?.chatId ?: return
        val data = callbackQuery.data

        try {

            when (data) {
                "createYourOwnPoll" -> handleCreateYourOwnPoll(bot, chatId, callbackQuery)
                "seeReadyMadePoll" -> handleSeeReadyMadePoll(bot, chatId)
                "howWorkRMP" -> handleShowRMPInfo(bot, chatId)
                "backToRMP" -> handleBackToRMP(bot, chatId)
                "backToMainMenu" -> handleBackToMainMenu(bot, chatId)
                "backToMainMenuFromPage" -> handleBackToMainMenuFromPage(bot, chatId)
                "takePublicTest" -> handleTakePublicTest(bot, chatId)
                "takePersonalTest" -> handleTakePersonalTest(bot, chatId)
                "skipNamingTest" -> handleSkipNamingTest(bot, update, chatId)
                "generateTestName" -> Animation.generateTestNameWithAnimation(bot, chatId)
                "deleteMyTests" -> handleDeleteMyTests(bot, chatId)
                "backToMyTestWindow" -> handleBackToMyTestWindow(bot, chatId)
                "back_to_results" -> handleBackToResults(bot, chatId)
                "createTestOnly" -> handleCreateTestOnly(bot, chatId)
                "startMyOwnTest" -> handleStartMyOwnTest(bot, chatId)
                "enterManualName" -> handleEnterManualName(bot, chatId)
//                "donateSomeSum" -> Payment.сhooseSum(bot, chatId)
//                "donateСustomSum" -> Payment.donateСustomPrice(bot, chatId)
                else -> handleKeyboardPollAnswer(bot, chatId, callbackQuery)
            }

            // Ответ на callback запрос для грамотной работы tg с всплываюшим окном
            val answerCallbackQuery = AnswerCallbackQuery()
            answerCallbackQuery.callbackQueryId = callbackQuery.id
//            answerCallbackQuery.text = "аккуратно, личное"
//            answerCallbackQuery.cacheTime = 10000
////            answerCallbackQuery.url = "https://t.me/Tem_tam_Tema"
//            answerCallbackQuery.showAlert = true
            bot.execute(answerCallbackQuery)

        } catch (e: Exception) {
            println("Ошибка в onCallbackData: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun handleStartMyOwnTest(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.takeMyOwnTestMID)

        // Приветственное сообщение о начале теста
        userState.supportStartTestTextMID = sendMessage(
            bot,
            chatId,
            StringForBot.SUPPORT_TESTS_MESSAGE,
        )?.messageId ?: 0
        // Анимация начала соот-но
        Animation.startTestAnimation(bot, chatId, userState)
    }

    private fun handleEnterManualName(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        // Устанавливаем состояние ожидания названия теста
        userState.pollCreationState = CallbackData.PollCreationState.WaitingForNameTest()
    }

    private fun handleCreateYourOwnPoll(bot: TelegramLongPollingBot, chatId: Long, callbackQuery: CallbackQuery) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.keyboardAfterMainMenuMID)

        // Установка начального состояния: "Ожидание ввода вопроса" по ключу
        userState.pollCreationState = PollCreationState.WaitingForQuestion()

        val replyKeyboard = if (userState.countCreatingPollIndividually > 0) {
            StringForBot.finishRB()
        } else {
            StringForBot.returnOrFinishRK()
        }

        val messageId = sendMessage(
            bot,
            chatId,
            "Пожалуйста, введите текст вопроса ниже",
            replyKeyboard = replyKeyboard
        )?.messageId ?: 0

        MessageManager.addMessageToDelete(chatId, messageId)

    }

    private fun handleSeeReadyMadePoll(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.keyboardAfterMainMenuMID)

        // Инициализация параметров навигации
        userState.apply {
            totalSystemTests = dbQuestionHelper.countOriginalSystemTestIds()
            testsPerPage = 5
            totalPages = (totalSystemTests + testsPerPage - 1) / testsPerPage
            pageNumberForRMP = 0
        }

        // Загрузка тестов для текущей страницы
        displayTestsForCurrentPage(bot, chatId, userState, "testId_", 1)
    }

    private fun handleShowRMPInfo(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        try {
            editMessage(
                bot,
                chatId,
                userState.chooseReadyMadePollKeyboardMID,
                StringForBot.ABOUT_RMP,
                parseMode = ParseMode.HTML,
                inlineKeyboard = StringForBot.backToRMPIB()
            )
        } catch (e: TelegramApiRequestException) {
            // Обработка ошибок API Telegram, например, если сообщение уже было изменено или удалено
            println("Ошибка при редактировании сообщения: ${e.message}")
        } catch (e: IllegalStateException) {
            // Обработка ошибок, связанных с некорректным состоянием, например, если userState.chooseReadyMadePollKeyboardMID недействителен
            println("Некорректное состояние: ${e.message}")
        } catch (e: Exception) {
            // Общий обработчик для всех других исключений
            println("Произошла ошибка: ${e.message}")
        }
    }

    private fun handleBackToRMP(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        try {
            editMessage(
                bot,
                chatId,
                userState.chooseReadyMadePollKeyboardMID,
                "Выберите понравившийся тест:",
                inlineKeyboard = StringForBot.createPageKeyboard(userState, userState.currentPageTests, "testId_", 1)
            )
        } catch (e: TelegramApiRequestException) {
            // Обработка ошибок API Telegram, например, если сообщение уже было изменено или удалено
            println("Ошибка при редактировании сообщения: ${e.message}")
        } catch (e: IllegalStateException) {
            // Обработка ошибок, связанных с некорректным состоянием, например, если userState.chooseReadyMadePollKeyboardMID недействителен
            println("Некорректное состояние: ${e.message}")
        } catch (e: Exception) {
            // Общий обработчик для всех других исключений
            println("Произошла ошибка: ${e.message}")
        }
    }

    private fun displayTestsForCurrentPage(
        bot: TelegramLongPollingBot,
        chatId: Long,
        userState: UserState,
        callBackPrefix: String,
        startingTestId: Int = 1
    ) {
        // Получение тестов для текущей страницы
        val startIndex = startingTestId + (userState.pageNumberForRMP * userState.testsPerPage)
        val endIndex = minOf(startIndex + userState.testsPerPage - 1, startingTestId + userState.totalSystemTests - 1)

        userState.currentPageTests = (startIndex..endIndex).mapNotNull { testId ->
            val testName = dbQuestionHelper.readTestName(testId.toString())
            val imageUrl = dbQuestionHelper.readImageUrl(testId.toString())

            if (imageUrl != null) {
                SystemTest(
                    testId = testId,
                    testName = testName ?: "Default Test Name",
                    imageUrl = imageUrl
                )
            } else {
                null
            }
        }

        // Отправка медиагруппы
        val mediaUrls = userState.currentPageTests.map {
            Pair(it.imageUrl, MediaSender.MediaType.PHOTO)
        }
        userState.chooseReadyMadePhotoList = sendMediaGroupFromUrls(bot, chatId, mediaUrls)
            ?.map { it.messageId }
            ?.toMutableList() ?: mutableListOf()

        // Отправка клавиатуры
        userState.chooseReadyMadePollKeyboardMID = sendMessage(
            bot,
            chatId,
            "Выберите понравившийся тест:",
            inlineKeyboard = StringForBot.createPageKeyboard(userState, userState.currentPageTests, callBackPrefix, startingTestId)
        )?.messageId ?: 0

        // Генерация testId с проверкой
        Generation.randomTestIdGeneration(chatId)
    }

    private fun handleStartPublicTest(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        val userPollState = PollSender.userPollStates.getOrPut(chatId) { PollSender.UserPollState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.chooseReadyMadePollKeyboardMID)
        val mediaGroupToDelete = userState.chooseReadyMadePhotoList.toMutableList()
        mediaGroupToDelete.forEach { messageId ->
            deleteMessage(bot, chatId, messageId)
        }
        userState.chooseReadyMadePhotoList.clear()

        userState.testId = data.removePrefix("publicTestId_")
        userPollState.currentQuestionIndex = 0


        // Приветственное сообщение о начале теста
        userState.supportStartTestTextMID = sendMessage(
            bot,
            chatId,
            StringForBot.SUPPORT_TESTS_MESSAGE,
        )?.messageId ?: 0
        // Анимация начала соот-но
        Animation.startTestAnimation(bot, chatId, userState)
    }


    private fun handleCreateReadyMadePollAnswer(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Открыли первую страницу с выборкой
        userState.pageNumberForRMP == 1

        userState.testId = data.substringAfter("testId_")

        if (userState.testId != null) {
            userState.currentQuestionIndex = 0
            userState.systemTestId = userState.testId!!.toInt()

            // Удаляем медиа группу с изображениями тестов, кроме выбранного
            val mediaGroupToDelete = userState.chooseReadyMadePhotoList.toMutableList()
            var indexToKeep = userState.testId!!.toInt() - 1
            if(mediaGroupToDelete.size > indexToKeep){     //Проверяем, есть ли кнопка с таким ID
                userState.theLastPhotoMID = mediaGroupToDelete[indexToKeep] // Сохраняем MID для словаря
                mediaGroupToDelete.removeAt(indexToKeep)    // Удаляем элемент по индексу, соответствующему testId
                mediaGroupToDelete.forEach { messageId ->
                    deleteMessage(bot, chatId, messageId)
                }
            } else while (mediaGroupToDelete.size < indexToKeep) {
                indexToKeep -= mediaGroupToDelete.size
                userState.theLastPhotoMID = mediaGroupToDelete[indexToKeep] // Сохраняем MID для
                mediaGroupToDelete.removeAt(indexToKeep)    // Удаляем элемент по индексу, соответствующему testId
                mediaGroupToDelete.forEach { messageId ->
                    deleteMessage(bot, chatId, messageId)
                }
            }
            userState.chooseReadyMadePhotoList.clear()

            // Флаг о необходимости фотосопровождения
            userState.needImageForTest = true

            // Удаляем сообщение с кнопками выбора теста
            clearPreviousMessages(userState, bot, chatId, userState.chooseReadyMadePollKeyboardMID, userState.aboutRMPMID)

            // Отправляем первый вопрос выбранного теста, запуская цепочку
            PollSender.sendQuestionsAnswerChoice(bot, chatId)
        }
    }

    private fun handleAnalyseReadyMadeAnswer(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        val questionsFromDb = dbQuestionHelper.readQuestionFromQuestionDb(userState.testId)
        val currentQuestion = questionsFromDb[userState.currentQuestionIndex]
        val rightAnswerIndex = data.substringBefore("optionRMP").toIntOrNull()?.minus(1)

        if (rightAnswerIndex != null) {

            // Сохранение в базу данных
            savePollToDatabase(chatId, currentQuestion = currentQuestion, rightIndex = rightAnswerIndex, testId = userState.generatedTestId, completionPercent = 666.0)
            saveTestIdForUserDatabase(chatId, userState.generatedTestId)

            // Обновляем сообщение с правильным ответом
            updateOptionsInQuestion(
                bot,
                chatId,
                currentQuestion.listOfAnswers,
                rightAnswerIndex,
                userState.currentQuestionIndex.plus(1),
                currentQuestion.questionText,
                userState.choiceTheCorrectAnswerMID,
            )

        } else {
            println("Некорректный формат callback data: $data")
        }

        // Прогон оставшихся вопросов
        userState.currentQuestionIndex++

        if (userState.currentQuestionIndex < questionsFromDb.size) {
            PollSender.sendQuestionsAnswerChoice(bot, chatId)

            // Один вопрос создан
            userState.countCreatingPollIndividually++
        } else {
            // Обнуление параметров
            userState.currentQuestionIndex = 0

            // Именуем тест, аналогчино RMP
            val systemTestName = dbQuestionHelper.readTestName(userState.systemTestId.toString())
            dbQuestionHelper.updateTestName(
                testName = systemTestName,
                testId = userState.generatedTestId
            )
            Generation.htmlUrlForTestGeneration(bot, chatId, userState, systemTestName, userState.generatedTestId)
            // Запрос на именование теста
//            Generation.nameOrSkipNamingTestGeneration(bot, chatId, userState)

            // Важно! Прерываем выполнение функции здесь, чтобы дать пользователю возможность ввести название
            return
        }

    }

    private fun updateOptionsInQuestion(
        bot: TelegramLongPollingBot,
        chatId: Long,
        listForMapIndexed: List<String>,
        rightIndex: Int,
        questionNumber: Int,
        questionText: String,
        messageId: Int
    ) {
        val updatedOptions = listForMapIndexed.mapIndexed { index, option ->
            if (index == rightIndex) {
                "✅ $option"
            } else {
                "▫\uFE0F $option"
            }
        }
        val updatedMessage = "$questionNumber. <i><b>$questionText</b></i>\n\n${updatedOptions.joinToString("\n")}"
        editMessage(
            bot,
            chatId,
            messageId,
            updatedMessage,
            parseMode = ParseMode.HTML
        )

        // Запись сообщения в список под удаление
        MessageManager.addMessageToDelete(chatId, messageId)
    }

    private fun handleBackToMainMenu(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.mainMenuMID)

        if (userState.keyboardAfterMainMenuMID != 0) {
            editMessage(
                bot,
                chatId,
                userState.keyboardAfterMainMenuMID,
                StringForBot.HELLO_MESSAGE_V2,
                parseMode = ParseMode.HTML
            )
            userState.startNowMID = sendMessage(
                bot,
                chatId,
                "Начните прямо сейчас!",
                replyKeyboard = StringForBot.mainRK()
            )?.messageId ?: 0
        }

    }

    private fun handleBackToMainMenuFromPage(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        if (userState.chooseReadyMadePhotoList != emptyList<Int>()) {
            // Удаляем медиа группу с изображениями тестов
            userState.chooseReadyMadePhotoList.forEach { messageId ->
                deleteMessage(bot, chatId, messageId)
            }
            userState.chooseReadyMadePhotoList.clear()
        }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.chooseReadyMadePollKeyboardMID, userState.aboutRMPMID)

        // Анимация возврата на главное меню
        Animation.homeStickerAnimation(bot, userState, chatId)
    }

    private fun handleTakePublicTest(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.keyboardAfterMainMenuMID)

        // Инициализация параметров навигации
        userState.apply {
            totalSystemTests = dbQuestionHelper.countOriginalPublicTestIds()
            testsPerPage = 5
            totalPages = (totalSystemTests + testsPerPage - 1) / testsPerPage
            pageNumberForRMP = 0
        }

        // Загрузка тестов для текущей страницы
        displayTestsForCurrentPage(bot, chatId, userState, "publicTestId_", 101)
    }

    private fun handleTakePersonalTest(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Очистка предыдущего сообщения
        clearPreviousMessages(userState, bot, chatId, userState.keyboardAfterMainMenuMID)

        val instructionMessage = """
            Перешлите ссылку для прохождения теста. 
            Либо введите имя пользователя автора опроса (например, @TemochkaMik). 
            После подтверждения, тест станет доступен.
        """.trimIndent()
        userState.testInstructionsMID = sendMessage(
            bot,
            chatId,
            instructionMessage,
            replyKeyboard = StringForBot.backRB(),
            parseMode = ParseMode.HTML
        )?.messageId ?: 0

        // Ожидание решения пользователя за тот или иной вариант
        userState.pollCreationState = PollCreationState.WaitingForUrlOrUsername()
    }

    private fun handleCleanAllAnswers(bot: TelegramLongPollingBot, chatId: Long, resultId: String) {
        val userPollState = PollSender.userPollStates.getOrPut(chatId) { PollSender.UserPollState() }

        // Получаем информацию о сообщениях, связанных с resultId
        val fireMessageInfo = userPollState.mapResultIdFireMID[resultId]

        if (fireMessageInfo != null) {

            // Удаляем все сообщения с опросами
            fireMessageInfo.listOfPollMIDForFire.forEach { messageId ->
                deleteMessage(bot, chatId, messageId)
            }

            // Редактируем сообщение с результатом
            editMessage(
                bot,
                chatId,
                fireMessageInfo.resultOfTakingPollMID,
                fireMessageInfo.resultOfTakingPollText,
                parseMode = ParseMode.HTML
            )

            // Очищаем словарь FireMessageInfo
            userPollState.mapResultIdFireMID.remove(resultId)
        } else {
            println("Информация о сообщениях для resultId $resultId не найдена.")
        }
    }

    private fun handleKeyboardPollAnswer(bot: TelegramLongPollingBot, chatId: Long, callbackQuery: CallbackQuery) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        val data = callbackQuery.data
        when {
            data.startsWith("testId_") -> {
                handleCreateReadyMadePollAnswer(bot, chatId, data)
            }
            data.startsWith("cleanAllAnswers_") -> {
                val resultId = data.removePrefix("cleanAllAnswers_")
                handleCleanAllAnswers(bot, chatId, resultId)

            }
            data.contains("optionRMP") -> {
                handleAnalyseReadyMadeAnswer(bot, chatId, data)
            }
            data.contains("TestsNavigation") -> {
                handleTestsNavigation(bot, chatId, callbackQuery)
            }
            data.contains("show_test_") -> {
                handleShowMyTests(bot, chatId, data)
            }
            data.startsWith("show_result:") -> {
                val resultId = data.removePrefix("show_result:")
                handleShowResults(bot, chatId, resultId)
            }
            data.startsWith("seeTestResult_") -> {
                val resultId = data.removePrefix("seeTestResult_")
                val messageId = userState.mapResultIdUserResult[resultId]?.userResultMID ?: 0
                if (messageId != 0) {
                    handleShowUserResult(bot, chatId, messageId, resultId) // Передаем messageId
                }
            }
            data.startsWith("user_results_") -> {
                val resultId = data.removePrefix("user_results_")
                handleBackToUserResults(bot, chatId, resultId)
            }
            data.startsWith("my_choosed_") -> {
                val testId = data.removePrefix("my_choosed_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != "") handleShowMyAnswers(bot, chatId, messageId, testId)
            }
            data.startsWith("swap_ans_") -> {
                val testId = data.removePrefix("swap_ans_")
                if (testId != "") handleBackToLink(bot, chatId, testId)
            }
            data.contains("for_") -> {
                handleAgreeForTakeTest(bot, chatId, data)
            }
            data.startsWith("disableTakingPoll_") -> {
                handleDisableTakingPoll(bot, chatId, data)
            }
            data.startsWith("useGeneratedName_") -> {
                handleUseGeneratedTestName(bot, chatId, data)
            }
            data.startsWith(RATE_PREFIX) -> {
                handleRateBot(bot, chatId, data)
            }
            data.startsWith("delete_my_test_") -> {
                handleDeleteChoosedTest(bot, chatId, data)
            }
            data.startsWith("deleting_") -> {
                handleAgreeOrDisAgreeDeleteTest(bot, chatId, data)
            }
            data.startsWith("donateSum_") -> {
//                Payment.donateSetSum(bot, chatId, data)
            }
            data.startsWith("publicTestId_") -> {
                handleStartPublicTest(bot, chatId, data)
            }
            data.startsWith("add_some_pepper_to_") -> {
                val testId = data.removePrefix("add_some_pepper_to_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleAddPeper(bot, chatId, testId, messageId)

            }
            data.startsWith("upload_photo_") -> {
                val testId = data.removePrefix("upload_photo_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleUploadPhoto(bot, chatId, testId, messageId)

            }
            data.startsWith("set_threshold_") -> {
                val testId = data.removePrefix("set_threshold_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleSetThreshold(bot, chatId, testId, messageId)

            }
            data.contains("_hold_") -> {
                val command = data.substringAfter("_hold_")
                val testId = data.substringBefore("_hold_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleHoldCommand(bot, chatId, testId, messageId, command, callbackQuery)
            }
            data.startsWith("set_username_") -> {
                val testId = data.removePrefix("set_username_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleSetUsername(bot, chatId, testId, messageId)

            }
            data.startsWith("set_num_attempt_") -> {
                val testId = data.removePrefix("set_num_attempt_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleSetNumAttempt(bot, chatId, testId, messageId)
            }
            data.contains("_attempts_") -> {
                val command = data.substringAfter("_attempts_")
                val testId = data.substringBefore("_attempts_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleAttemptsCommand(bot, chatId, testId, messageId, command, callbackQuery)
            }
            data.startsWith("pepper_done_") -> {
                val testId = data.removePrefix("pepper_done_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handlePepperDone(bot, chatId, testId, messageId, callbackQuery)

            }
            data.startsWith("back_to_ans_") -> {
                val testId = data.removePrefix("back_to_ans_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleBackToAnswers(bot, chatId, testId, messageId)
            }
            data.startsWith("back_to_pepper_menu_") -> {
                val testId = data.removePrefix("back_to_pepper_menu_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleAddPeper(bot, chatId, testId, messageId)
            }
            data.startsWith("media_back_to_pepper_menu_") -> {
                val testId = data.removePrefix("media_back_to_pepper_menu_")
                val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
                if (testId != null) handleBackToPepperMenuMedia(bot, chatId, testId, messageId)
            }
        }

        // Финальное состояние создания собственного теста (Выбор inline-кнопки-ответа)
        if (!userState.isBackInCreatingPollForChoosingAnswers) {

            val state = userState.pollCreationState

            if (state is PollCreationState.WaitingForCorrectAnswer) {

                // Разыменовывание индекса из callbackdata
                val rightAnswerIndex = data.toIntOrNull()?.minus(1)

                if (rightAnswerIndex != null && rightAnswerIndex >= 0 && rightAnswerIndex < state.options.size) {

                    // Сохранение в базу данных
                    savePollToDatabase(chatId, state, rightAnswerIndex, testId = userState.generatedTestId, completionPercent = 666.0)
                    saveTestIdForUserDatabase(chatId, userState.generatedTestId)

                    // Обновляем сообщение с правильным ответом
                    updateOptionsInQuestion(
                        bot,
                        chatId,
                        state.options,
                        rightAnswerIndex,
                        userState.questionNumberInCreating,
                        state.question,
                        callbackQuery.message?.messageId ?: return,
                    )
                    userState.questionNumberInCreating++

                    // Один вопрос создан
                    userState.countCreatingPollIndividually++

                    //Обнуление MID в конце создания
                    userState.keyboardAfterMainMenuMID = 0
                    userState.startNowMID = 0

                    // Очистка предыдущих сообщений
                    clearPreviousMessages(userState, bot, chatId, userState.mainMenuMID)

                    // Состояние создания именно вопроса завершено
                    userState.pollCreationState = PollCreationState.WaitingForNameTest()
                    if (userState.processCreatingPoll) {
                        handleCreateYourOwnPoll(bot, chatId, callbackQuery)
                    }
                }
            }
        }
    }

    private fun handleTestsNavigation(
        bot: TelegramLongPollingBot,
        chatId: Long,
        callbackQuery: CallbackQuery
    ) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        val command = callbackQuery.data.substringBefore("TestsNavigation")
        when (command) {
            "previous" -> {
                if (userState.pageNumberForRMP <= 0) {
                    Generation.windowAlertGeneration(
                        bot,
                        "Добро пожаловать на первую страницу!\nДальше бо-о-о-о-ольше 😉",
                        callbackQuery
                    )
                    return
                }
                userState.pageNumberForRMP--
            }
            "next" -> {
                if (userState.pageNumberForRMP >= userState.totalPages - 1) {
                    Generation.windowAlertGeneration(
                        bot,
                        "Котики работают над новыми вопросами!\nОставайтесь с наа-а-а-а-ами 🫶🏻",
                        callbackQuery
                    )
                    return
                }
                userState.pageNumberForRMP++
            }
        }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.chooseReadyMadePollKeyboardMID)
        userState.chooseReadyMadePhotoList.forEach { messageId ->
            deleteMessage(bot, chatId, messageId)
        }

        // Получение нужного постфикса и стартового testId для inline-клавиатуры
        val callBackPrefix = callbackQuery.data.split("_")[1]
        val startingTestId = callbackQuery.data.split("_")[2].toIntOrNull()

        // Отображение новой страницы
        startingTestId?.let { displayTestsForCurrentPage(bot, chatId, userState, "${callBackPrefix}_", it) }
    }

    private fun handleDeleteMyTests(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        if (userState.myTestsMID != 0) {
            editMessage(
                bot,
                chatId,
                userState.myTestsMID,
                "Выберите тесты для удаления:",
                inlineKeyboard = StringForBot.deleteMyTestsIK(userState),
            )
        } else {
            println("❌ Не найдено сообщение с ID: userState.myTestsMID")
        }

    }
    
    private fun handleDeleteChoosedTest(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Расшифровывание параметров callback.data
        val storageKey = data.substringAfter("delete_my_test_")
        val testData = callbackStorage[storageKey] ?: return

        // Запись в параметры состояний теста под удаление
        userState.testIdForDeleting = testData.testId

        // Формирование окна для удаления теста
        val buttonText = if (testData.testName == "") {
            "Удалить тест <b><i>\"Без названия\"</i></b>?"
        } else {
            "Удалить тест <b><i>\"${testData.testName}\"</i></b>?"
        }
        editMessage(
            bot,
            chatId,
            userState.myTestsMID,
            buttonText,
            parseMode = ParseMode.HTML,
            inlineKeyboard = StringForBot.agreeToDeleteTestIK()
        )

    }

    private fun handleAgreeOrDisAgreeDeleteTest(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Расшифровывание команды из callback.data
        val command = data.substringAfter("deleting_")
        when (command) {
            "agree" -> {
                // Удаление testId из тестов пользователя
                dbQuiziHelper.removeTestIdFromUserArray(chatId, userState.testIdForDeleting)
                // Удаление testId из базы данных тестов
                dbQuestionHelper.deleteTest(userState.testIdForDeleting)
                // Удаление resultId из результатов пользователя
                dbQuiziHelper.deleteResultsByAuthorTestId(userState, dbResultsHelper)
                // Удаление testId и resultId из базы результатов
                dbResultsHelper.deleteResults(userState.testIdForDeleting)

                // Удаление связанных данных из mapTestIdLinkAnswerInfo
                val urlAnswersInfo = userState.mapTestIdLinkAnswerInfo[userState.testIdForDeleting]
                urlAnswersInfo?.let {
                    // Удаляем сообщение с ссылкой
                    deleteMessage(bot, chatId, it.linkMessageId)
                    // Удаляем сообщение с ответами
                    deleteMessage(bot, chatId, it.answerMessageId)
                    // Удаляем фотографию, если она есть
                    it.imageMID?.let { messageId ->
                        deleteMessage(bot, chatId, it.imageMID)
                    }
                }
                // Очистка словаря
                userState.mapTestIdLinkAnswerInfo.remove(userState.testIdForDeleting)

                // Обновление списка тестов пользователя
                userState.usersTestsMutableList = dbQuiziHelper.readArrayOfTestId(chatId).filterNotNull()

                if (userState.usersTestsMutableList.isEmpty()) {
                    // Возврат к понели создания теста
                    StringForBot.showWindowsWithMyTests(bot, chatId, dbQuiziHelper)
                } else {
                    handleDeleteMyTests(bot, chatId)
                }
            }
            "disagree" -> {
                handleDeleteMyTests(bot, chatId)
            }
        }
    }

    private fun handleShowResults(bot: TelegramLongPollingBot, chatId: Long, resultId: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Получение параметров для показа результатов теста
        val message = StringForBot.seeResultParametr(resultId)

        // Создаем кнопку для возврата к списку результатов
        val backButton = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("◀️ К списку результатов").apply {
                        callbackData = "back_to_results"
                    }
                )
            )
        )

        // Отправляем сообщение с результатами
        editMessage(
            bot,
            chatId,
            userState.myResultsMID,
            message,
            inlineKeyboard = backButton,
            parseMode = ParseMode.HTML
        )
    }

    private fun handleShowUserResult(bot: TelegramLongPollingBot, chatId: Long, messageId: Int, resultId: String) {
        // Получение параметров для показа результатов теста
        val message = StringForBot.seeResultParametr(resultId)

        // Создаем кнопку для возврата к списку результатов
        val backButton = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("⌵    Свернуть").apply {
                        callbackData = "user_results_$resultId"
                    }
                )
            )
        )

        // Отправляем сообщение с результатами
        editMessage(
            bot,
            chatId,
            messageId,
            message,
            inlineKeyboard = backButton,
            parseMode = ParseMode.HTML
        )
    }

    private fun handleBackToResults(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId){ UserState() }
        if (userState.myResultsMID != 0) {
            editMessage(
                bot,
                chatId,
                userState.myResultsMID,
                "📊 Выберите тест для просмотра результатов:",
                inlineKeyboard = StringForBot.windowWithMyResults(chatId, dbQuestionHelper, dbResultsHelper),
                parseMode = ParseMode.HTML
            )
        } else {
            userState.myResultsMID = sendMessage(
                bot,
                chatId,
                "📊 Выберите тест для просмотра результатов:",
                inlineKeyboard = StringForBot.windowWithMyResults(chatId, dbQuestionHelper, dbResultsHelper),
                parseMode = ParseMode.HTML
            )?.messageId ?: 0
        }
    }

    private fun handleBackToUserResults(bot: TelegramLongPollingBot, chatId: Long, resultId: String) {
        val userState = userStates.getOrPut(chatId){ UserState() }
        val messageId = userState.mapResultIdUserResult[resultId]?.userResultMID ?: 0
        val spoilerResultText = userState.mapResultIdUserResult[resultId]?.userResultText ?: "❌ Ошибка считывания, результаты закрыты пользователем"
        val linkForReply = userState.mapResultIdUserResult[resultId]?.userResultLink ?: "https://t.me/TemochkaMik"
        if (messageId != 0) {
            editMessage(
                bot,
                chatId,
                messageId, // Используем конкретный messageId
                spoilerResultText,
                inlineKeyboard = StringForBot.seeSummaryResultIK(resultId, linkForReply),
                parseMode = ParseMode.MARKDOWNV2
            )
        }
    }

    private fun handleShowMyAnswers(bot: TelegramLongPollingBot, chatId: Long, messageId: Int, testId: String) {

        // Сообщение с ответами на тест
        val message = StringForBot.createMessageForMyAnswers(testId)

        // Создаем кнопку для возврата к списку результатов
        val backButton = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("⌵    Cвернуть").apply {
                        callbackData = "swap_ans_$testId"
                    }
                )
            )
        )

        // Отправляем сообщение с результатами
        editMessage(
            bot,
            chatId,
            messageId,
            message,
            inlineKeyboard = backButton,
            parseMode = ParseMode.HTML
        )

    }

    private fun handleBackToLink(bot: TelegramLongPollingBot, chatId: Long, testId: String?) {
        val userState = userStates.getOrPut(chatId){ UserState() }
        val messageId = userState.mapTestIdLinkAnswerInfo[testId]?.answerMessageId ?: 0
        val answerText = userState.mapTestIdLinkAnswerInfo[testId]?.answerText ?: "||Засекреченные ответы\\:||"

        if (messageId != 0) {
            editMessage(
                bot,
                chatId,
                messageId,
                answerText,
                inlineKeyboard = StringForBot.seeMyAnswersIK(testId ?: ""),
                parseMode = ParseMode.MARKDOWNV2
            )
        }
    }

    private fun handleBackToAnswers(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId){ UserState() }
        val answerText = userState.mapTestIdLinkAnswerInfo[testId]?.answerText ?: "||Засекреченные ответы\\:||"
        if (editMID != 0) {
            editMessage(
                bot,
                chatId,
                editMID,
                answerText,
                inlineKeyboard = StringForBot.seeMyAnswersIK(testId ?: ""),
                parseMode = ParseMode.MARKDOWNV2
            )
        }
    }

    private fun handleBackToMyTestWindow(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        if (userState.myTestsMID != 0) {
            editMessage(
                bot,
                chatId,
                userState.myTestsMID,
                StringForBot.MY_TESTS_AND_LINK_MESSAGE,
                inlineKeyboard = StringForBot.windowWithMyTestsIK(userState),
            )
        } else {
            println("❌ Не найдено сообщение с ID: userState.myTestsMID")
        }
    }

    private fun handleShowMyTests(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Расшифровка по callback ключу значений
        val storageKey = data.substringAfter("show_test_")
        val testData = callbackStorage[storageKey] ?: return
        userState.testName = testData.testName
        userState.testId = testData.testId

        // Генерация ссылки-сообщения на прохождения
        Generation.htmlUrlForTestGeneration(bot, chatId, userState, userState.testName, userState.testId)

        // Удаляем использованные данные
        callbackStorage.remove(storageKey)
        // Очистка параметров состояния
        userState.testName = ""
    }

    private fun handleAgreeForTakeTest( bot: TelegramLongPollingBot, chatId: Long, data: String) {
        // Состояния автора для открытия доступа к прохождению теста
        val authorUserState = userStates.getOrPut(chatId) { UserState() }

        // Расшифроввывание данных по ключу
        val storageKey = data.substringAfter("for_")
        val testData = callbackStorage[storageKey] ?: return
        authorUserState.userThatWantToTakeMyTestChatId = testData.chatId // chatId пользователя пожелавшего пройти теста
        authorUserState.testName = testData.testName // Название теста
        authorUserState.testId = testData.testId // id теста

        println("""
            authorUserState.userThatWantToTakeMyTestChatId = ${authorUserState.userThatWantToTakeMyTestChatId} // chatId пользователя пожелавшего пройти теста
            authorUserState.testName = ${authorUserState.testName} // Название теста
            authorUserState.testId = ${authorUserState.testId} // id теста
        """.trimIndent())

        // Состояния пользователя
        val userState = userStates.getOrPut(authorUserState.userThatWantToTakeMyTestChatId) { UserState() }

        // Остановка анимации ожидания для пользователя
        userState.waitingAnimationJob?.cancel()
        userState.waitingAnimationJob = null
        // Удаление сообщения с анимацией
        clearPreviousMessages(userState, bot, testData.chatId, userState.testInstructionsMID)

        // Username пользователя
        userState.username = dbQuiziHelper.readUsernameByChatId(testData.chatId)
        // Уравнивание параметра testId теста для прозождения сего пользователем
        userState.testId = authorUserState.testId

        // Получение текущей даты и времени
        val dateFormat = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale("ru"))
        userState.currentDate = dateFormat.format(Date())

        // Редактирвоание названия теста
        val testName = if (authorUserState.testName == "") {"Без названия" } else { authorUserState.testName }

        // Анимация открытия доступа для автора
        Animation.openAccessWithSmile(bot, userState, testName ?: "Без названия", "\uD83D\uDD10")

        // Удаление сообщения об опасности
        deleteMessage(bot, chatId, userState.authorIgnoreMessageIfAnonimusMID)

        // Приветственное сообщение о начале теста и анимация начала соот-но
        userState.supportStartTestTextMID = sendMessage(
            bot,
            authorUserState.userThatWantToTakeMyTestChatId,
            StringForBot.SUPPORT_TESTS_MESSAGE,
        )?.messageId ?: 0
        Animation.startTestAnimation(bot, authorUserState.userThatWantToTakeMyTestChatId, userState)

        // Удаляем использованные данные
        callbackStorage.remove(storageKey)
        // Обнуление параметров состояний автора
        authorUserState.userThatWantToTakeMyTestChatId = 0
        authorUserState.testName = ""
        authorUserState.testId = ""
    }

    private fun handleSkipNamingTest(bot: TelegramLongPollingBot, update: Update, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Именование теста будет пропущено
        userState.skipNamingTest = true

        // Очищаем сообщения
        deleteMessagesSafely(bot, chatId)
        clearPreviousMessages(userState, bot, chatId, userState.adviceToSkipNamingTestMID)

        if (userState.countCreatingPollIndividually > 0) {
            // Отправка сообщения с ссылкой для прохождения
            Generation.htmlUrlForTestGeneration(bot, chatId, userState, userState.testName)
            userState.countCreatingPollIndividually = 0
        } else {
            userState.keyboardAfterMainMenuMID = sendMessage(
                bot,
                chatId,
                "Вы пока не создали ни одного теста\nПредлагаю посмотреть готовые \uD83D\uDC47",
                inlineKeyboard = StringForBot.createTestIK(),
            )?.messageId ?: 0        }

        // Обнуление состояния создания теста
        userState.pollCreationState = null

    }

    private fun handleCreateTestOnly(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        // Анимация со смайликом мини-сердечко с перехожом на создание inline клавиатуру создания теста
        Animation.startCreateTestWithSmile(bot, chatId, userState, "\uD83E\uDEF0")
    }

    private fun handleDisableTakingPoll(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        // Состояния автора для закрытия доступа к прохождению теста
        val authorUserState = userStates.getOrPut(chatId) { UserState() }

        // Расшифроввывание данных по ключу
        val disableStorageKey = data.substringAfter("disableTakingPoll_")
        val testData = callbackStorage[disableStorageKey] ?: return
        authorUserState.userThatWantToTakeMyTestChatId = testData.chatId // chatId пользователя пожелавшего пройти теста

        // Состояния пользователя
        val userState = userStates.getOrPut(authorUserState.userThatWantToTakeMyTestChatId) { UserState() }

        // Остановка анимации ожидания для пользователя
        userState.waitingAnimationJob?.cancel()
        userState.waitingAnimationJob = null
        // Удаление сообщения с анимацией
        clearPreviousMessages(userState, bot, testData.chatId, userState.testInstructionsMID)

        // Username пользователя
        userState.username = dbQuiziHelper.readUsernameByChatId(testData.chatId)
        // Уравнивание параметра testId теста для прозождения сего пользователем
        userState.testId = authorUserState.testId

        // Удаление сообщения об опасности
        deleteMessage(bot, chatId, userState.authorIgnoreMessageIfAnonimusMID)

        // Удаление сообщений выбора теста с анимацией закрытия
        Animation.closeAccess(bot, userState)

        // Сообщение об закрытии доступа пользователю
        userState.testIsClosedMID = sendMessage(
            bot,
            authorUserState.userThatWantToTakeMyTestChatId,
            """
                🔐 <b>Упс! Кажется, произошли изменения</b>

                Автор решил временно ограничить доступ к прохождению.
    
                💫 Возможно, тест обновляется или дорабатывается!
                ✨ Попробуйте получить доступ позже или выберите другой тест.
        """.trimIndent(),
            parseMode = ParseMode.HTML
        )?.messageId ?: 0

        Generation.somethingElseKeyboardGeneration(bot, authorUserState.userThatWantToTakeMyTestChatId, userState)

        // Удаляем использованные данные
        callbackStorage.remove(disableStorageKey)
        // Обнуление параметров состояний автора
        authorUserState.userThatWantToTakeMyTestChatId = 0
        authorUserState.testName = ""
        authorUserState.testId = ""
    }

    private fun handleUseGeneratedTestName(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        
        // Расшифроввывание данных по ключу
        val storageKey = data.substringAfter("useGeneratedName_")
        val testData = callbackStorage[storageKey] ?: return
        
        // Передача названия в обход состояний пользователя
        MessageRecieved.handleWaitingForNameTest(bot, null, chatId, testData.testName)
        
        // Предотвращаем запуски иных состояний
        userState.pollCreationState = null
        
        // Удаляем использованные данные
        callbackStorage.remove(storageKey)
    }

    private fun handleRateBot(bot: TelegramLongPollingBot, chatId: Long, data: String) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        // Получение оценки из callback.data
        val rating = data.removePrefix(RATE_PREFIX).toInt()

        // Запись оценки в базу данных
        dbQuiziHelper.updateRating(chatId, rating)

        val thankYouText = """
            Спасибо за вашу оценку!
            Ваш отзыв очень важен для нас!
            Оценка: $rating из 5 🫶
        """.trimIndent()

        editMessage(
            bot,
            chatId,
            userState.rateBotMID,
            thankYouText,
            parseMode = ParseMode.HTML,
            inlineKeyboard = StringForBot.filledRatingIK(rating)
        )

        Animation.startRatingAnimation(bot, chatId, userState.rateBotMID, rating)
    }

    fun handleAddPeper(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        userState.pollCreationState = null // Обнуление любого состояния ожидания
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID) // Очистка системных "Что-либо ещё?"

        // Получаем значения по ходу заполнения
        val comPercent = dbQuestionHelper.getTargetComPercent(testId)
        val username = dbQuestionHelper.getTargetUsername(testId)
        val attempts = dbQuestionHelper.getAttemptsCount(testId)
        val photoFileId = dbQuestionHelper.getPhotoFileId(testId)
        val hasPhoto = photoFileId != null

        // Сохранение значений в словарь
        userState.mapTestIdLinkAnswerInfo[testId] = Generation.UrlAnswersInfo(
            answerMessageId = editMID,
            targetComPercent = comPercent ?: 0,
            targetUsername = username,
            targetNumAttempts = attempts ?: 0,
            photoFileId = photoFileId
        )

        val paperText = """
               ✨ <b>Добавьте перчинку вашему тесту!</b>

                ${if (hasPhoto) "✅" else "❌"} Загрузите <b>фото</b> (пикантное или смешное) — доступ к нему будет только по ссылке выше 🛡️
                ${if (comPercent != null && comPercent != 0) "✅" else "❌"} Установите <b>порог прохождения</b> (например, 70%) — сюрприз откроется только при успешном результате.
                ${if (username != null && username != "vsex") "✅" else "❌"} Укажите <b><i>username</i> партнера</b> — сюрприз увидят только он/она.
                ${if (attempts != null && attempts != 0 && attempts != Int.MAX_VALUE) "✅" else "❌"} Укажите <b>количество попыток</b> — сколько раз можно пройти тест.
            
               Заполните оставшиеся поля, чтобы сделать тест особенным! 🌟
        """.trimIndent()

        editMessage(
            bot,
            chatId,
            editMID,
            paperText,
            inlineKeyboard = StringForBot.addPepperIK(testId),
            parseMode = ParseMode.HTML
        )

        Generation.somethingElseKeyboardGeneration(bot, chatId, userState) // Востановление "Что-либо ещё?"
    }

    private fun handleUploadPhoto(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID) // Очистка системных "Что-либо ещё?"

        val uploadPhotoText = "Отправьте фото, чтобы добавить его.\nОно станет сюрпризом для вашего партнера! \uD83D\uDCF8"
        editMessage(
            bot,
            chatId,
            editMID,
            uploadPhotoText,
            inlineKeyboard = StringForBot.backToPepperMenuIB(testId),
            parseMode = ParseMode.HTML
        )

        // Состояние ожидание фотографии
        userState.pollCreationState = PollCreationState.WaitingForPhoto(testId)
    }

    private fun handleSetThreshold(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID) // Очистка системных "Что-либо ещё?"

        val setThresholdText = "Укажите порог прохождения теста (например, 70%) — сюрприз откроется только при успешном результате! 🎯"
        editMessage(
            bot,
            chatId,
            editMID,
            setThresholdText,
            inlineKeyboard = StringForBot.chooseThreshold(testId, userState.currentHold),
            parseMode = ParseMode.HTML
        )
    }

    private fun handleHoldCommand(bot: TelegramLongPollingBot, chatId: Long, testId: String, messageId: Int, command: String, callbackQuery: CallbackQuery) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        val setThresholdText = "Укажите порог прохождения теста (например, 70%) — сюрприз откроется только при успешном результате! 🎯"
        when {
            command.startsWith("plus_") -> {
                val value = command.removePrefix("plus_").toInt()
                userState.currentHold += value
                editMessage(
                    bot,
                    chatId,
                    messageId,
                    setThresholdText,
                    inlineKeyboard = StringForBot.chooseThreshold(testId, userState.currentHold),
                    parseMode = ParseMode.HTML
                )
            }
            command.startsWith("minus_") -> {
                val value = command.removePrefix("minus_").toInt()
                userState.currentHold -= value
                editMessage(
                    bot,
                    chatId,
                    messageId,
                    setThresholdText,
                    inlineKeyboard = StringForBot.chooseThreshold(testId, userState.currentHold),
                    parseMode = ParseMode.HTML
                )
            }
            command.startsWith("finish_") -> {
                val finalHold = command.removePrefix("finish_").toInt()

                if (finalHold > 100 || finalHold < 0) {
                    val alertText = """
                        Значение должно быть от 0 до 100.
                        Иначе:
                        • Это либо слишком просто ✨
                        • Либо за гранью фанатстики!🚀
                    """.trimIndent()
                    Generation.windowAlertGeneration(bot, alertText, callbackQuery)
                    return
                }

                // Сохраняем порог прохождения для награды
                dbQuestionHelper.saveTagretComPercent(testId, finalHold)
                // Сбрасываем состояние
                userState.pollCreationState = null
                // Возвращаемся к меню
                handleAddPeper(bot, chatId, testId, messageId)
            }
        }
    }

    private fun handleSetUsername(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID) // Очистка системных "Что-либо ещё?"

        val setUsernameText = "Введите username партнера — сюрприз будет доступен только ему/ей! 👤"
        editMessage(
            bot,
            chatId,
            editMID,
            setUsernameText,
            inlineKeyboard = StringForBot.backToPepperMenuIB(testId),
            parseMode = ParseMode.HTML
        )
        // Cостояние ожидания username
        userState.pollCreationState = PollCreationState.WaitingForTargetUsername(testId)
    }

    private fun handleSetNumAttempt(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID) // Очистка системных "Что-либо ещё?"

        val setAttemptsText = "Укажите количество попыток для прохождения теста — сколько раз можно попробовать? 🔢"
        editMessage(
            bot,
            chatId,
            editMID,
            setAttemptsText,
            inlineKeyboard = StringForBot.chooseAttempts(testId, userState.currentAttempt),
            parseMode = ParseMode.HTML
        )
    }

    private fun handleAttemptsCommand(bot: TelegramLongPollingBot, chatId: Long, testId: String, messageId: Int, command: String, callbackQuery: CallbackQuery) {
        val userState = userStates.getOrPut(chatId) { UserState() }
        val setAttemptsText = "Укажите количество попыток для прохождения теста — сколько раз можно попробовать? 🔢"
        when {
            command.startsWith("plus_") -> {
                val value = command.removePrefix("plus_").toInt()
                userState.currentAttempt += value
                editMessage(
                    bot,
                    chatId,
                    messageId,
                    setAttemptsText,
                    inlineKeyboard = StringForBot.chooseAttempts(testId, userState.currentAttempt),
                    parseMode = ParseMode.HTML
                )
            }
            command.startsWith("minus_") -> {
                val value = command.removePrefix("minus_").toInt()
                userState.currentAttempt -= value
                editMessage(
                    bot,
                    chatId,
                    messageId,
                    setAttemptsText,
                    inlineKeyboard = StringForBot.chooseAttempts(testId, userState.currentAttempt),
                    parseMode = ParseMode.HTML
                )
            }
            command.startsWith("finish_") -> {
                val finalAttempts = command.removePrefix("finish_").toInt()

                if (finalAttempts < 1 || finalAttempts > 100) {
                    val alertText = """
                        Количество попыток должно быть от 1 до 100.
                        Иначе:
                        • Это либо слишком строго! 😢
                        • Либо слишком легко! 🎉
                    """.trimIndent()
                    Generation.windowAlertGeneration(bot, alertText, callbackQuery)
                    return
                }

                // Сохраняем количество попыток
                dbQuestionHelper.saveTagretNumAttempts(testId, finalAttempts)
                // Сбрасываем состояние
                userState.pollCreationState = null
                // Возвращаемся к меню
                handleAddPeper(bot, chatId, testId, messageId)
            }
        }
    }

    private fun handlePepperDone(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int, callbackQuery: CallbackQuery) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        val testInfo = userState.mapTestIdLinkAnswerInfo[testId] ?: return

        // Значения из баз данных
        val comPercentDB = dbQuestionHelper.getTargetComPercent(testId)
        val usernameDB = dbQuestionHelper.getTargetUsername(testId)
        val attemptsDB = dbQuestionHelper.getAttemptsCount(testId)
        val photoFileIdDB = dbQuestionHelper.getPhotoFileId(testId)

        // Заполняем фильтруя
        val comPercent = if (comPercentDB == 0 || comPercentDB == null) 60 else comPercentDB
        val attempts = if (attemptsDB == 0 || attemptsDB == null) Int.MAX_VALUE else attemptsDB
        val username = usernameDB ?: "BCEX"
        val photoFileId = photoFileIdDB

        // Обрабатываем критичные случаи
        if (testInfo.targetComPercent == 0) {
            Generation.windowAlertGeneration(bot, "Вы не установили пороговое значение.\nБот примет значение по умолчанию (60%)\nВы можете изменить это в настройках теста", callbackQuery)
        } else if (photoFileId == null) {
            Generation.windowAlertGeneration(bot, "Но Вы не загрузили даже фотографии котика...\nБез этого \"перчинка\" не сыграет \uD83D\uDE14", callbackQuery)
        }
        // Обновляем шаблонные настройки теста
        dbQuestionHelper.saveTagretComPercent(testId, comPercent)
        dbQuestionHelper.saveTagretNumAttempts(testId, attempts)

        val resultText = """
            🎉 <b>Настройки теста успешно сохранены!</b>
    
            📊 <b>Порог прохождения:</b> $comPercent%
            👤 <b>Доступно для:</b> <i>@$username</i>
            🔄 <b>Количество попыток:</b> ${if (attempts == Int.MAX_VALUE) "Неограниченно" else "$attempts"}
            📷 <b>Фото:</b> ${if (photoFileId != null) "Загружено" else "Не загружено"}
        """.trimIndent()

        if (photoFileId != null) {
            // Удаляем старое сообщение без фото
            deleteMessage(bot, chatId, editMID)
            // Отправляем сообщение с фотографией
            val newMID = MediaSender.sendPhoto(
                bot,
                chatId,
                photoUrl = photoFileId,
                caption = resultText,
                parseMode = ParseMode.HTML,
                inlineKeyboard = StringForBot.backToPepperMenuFromMediaIB(testId),
                protectContent = true,
                spoiler = true,
            )?.messageId ?: 0

            // Заменяем связь MID и testId
            userState.mapTestIdLinkAnswerInfo[testId] = Generation.UrlAnswersInfo(
                answerMessageId = newMID,
                resultText = resultText
            )
        } else {
            println("fileId = $photoFileId null")
            editMessage(
                bot,
                chatId,
                editMID,
                resultText,
                inlineKeyboard = StringForBot.backToPepperMenuIB(testId),
                parseMode = ParseMode.HTML
            )
        }
    }

    private fun handleBackToPepperMenuMedia(bot: TelegramLongPollingBot, chatId: Long, testId: String, editMID: Int) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        val testInfo = userState.mapTestIdLinkAnswerInfo[testId] ?: return
        val resultText = testInfo.resultText ?: "Ошибка считывания парметров \uD83C\uDF36"

        // Удаляем старое сообщение c фото и системное сообщение
        deleteMessage(bot, chatId, editMID)
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID)
        // Отправляем сообщение без фотографии
        val newMID = sendMessage(
            bot,
            chatId,
            resultText,
            parseMode = ParseMode.HTML,
            inlineKeyboard = StringForBot.addPepperIK(testId),
        )?.messageId ?: 0

        // Возвращаем связь MID и testId
        userState.mapTestIdLinkAnswerInfo[testId] = Generation.UrlAnswersInfo(
            answerMessageId = newMID,
        )
        Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
    }

    private fun saveTestIdForUserDatabase(chatId: Long, testId: String?) {
        DatabaseQuiziHelper(CharForQuiziDb.DATABASE_NAME).apply {
            updateTestId(
                chatId,
                testId.toString()
            )
        }
    }

    private fun savePollToDatabase(
        chatId: Long,
        state: PollCreationState? = null,
        rightIndex: Int,
        currentQuestion: DatabaseQuestionHelper.Question? = null,
        testId: String? = "0",
        completionPercent: Double
    ) {
        val userState = userStates.getOrPut(chatId) { UserState() }

        when (state) {
            is PollCreationState.WaitingForAnswers -> {
                val question = state.question
                val options = listOf<String>() // Здесь вы должны использовать options, которые ввел пользователь
                val correctAnswerIndex = -1 // Пока оставляем -1, будет обновлено позже
                DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME).apply {
                    insertQuestionToQuestionDb(
                        testID = "0",
                        question = question,
                        arrayOfAnswers = options,
                        indexOfRightAnswer = correctAnswerIndex,
                        completionPercent = 0.0,
                        numUpdate = 0

                    )
                }
            }
            is PollCreationState.WaitingForCorrectAnswer -> {
                val question = state.question
                val options = state.options
                DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME).apply {
                    if (testId != null) {
                        insertQuestionToQuestionDb(
                            testId,
                            question,
                            options,
                            rightIndex,
                            0.0,
                            numUpdate = 0
                        )
                    }
                }
            }
            else -> {
                if (currentQuestion != null) {
                    val question = currentQuestion.questionText
                    val options = currentQuestion.listOfAnswers
                    DatabaseQuestionHelper(CharForQuestionDb.DATABASE_NAME).apply {
                        if (testId != null) {
                            insertQuestionToQuestionDb(
                                testId,
                                question,
                                options,
                                rightIndex,
                                0.0,
                                numUpdate = 0
                            )
                        }
                    }
                } else {
                    println("Error in savaPollToDatabase in `else`...")
                }
            }
        }
    }
}