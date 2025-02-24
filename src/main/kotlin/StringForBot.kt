@file:Suppress("DEPRECATION")

import Database.Question.DatabaseQuestionHelper
import Database.Quizi.DatabaseQuiziHelper
import Database.Results.DatabaseResultsHelper
import Methods.CallbackData
import Methods.CallbackData.callbackStorage
import Methods.CallbackData.dbQuestionHelper
import Methods.CallbackData.dbResultsHelper
import Methods.CallbackData.userStates
import Methods.Generation
import Methods.TextSender
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.text.SimpleDateFormat
import java.util.*

object StringForBot {

    const val HELLO_MESSAGE: String = "Добро пожаловать в мир <b><i>«А она спросит…»</i></b>, где мы создаём тесты " +
            "для парочек.\n\nЗдесь вы найдёте интересные вопросы о ваших отношениях, которые помогут " +
            "укрепить вашу любовь и лучше узнать друг друга.\n\nНачните прямо сейчас!"

    const val HELLO_MESSAGE_V2: String = "Добро пожаловать в мир <b><i>«А она спросит…»</i></b>, где мы создаём тесты " +
            "для парочек.\n\nЗдесь вы найдёте интересные вопросы о ваших отношениях, которые помогут " +
            "укрепить вашу любовь и лучше узнать друг друга."

    const val WARNING_PARSE_MY_SELF_TEST: String = "🤔 Хмм... Кажется, вы пытаетесь пройти свой собственный тест!\n\n" +
            "⚠️ Это не совсем честно по отношению к статистике - результаты могут получиться немного \"подкрученными\" 😉\n\n🎯" +
            " Рекомендуем делиться тестом с друзьями и получать настоящую обратную связь!\n\n" +
            "👇 Но если вы всё-таки хотите проверить, как работает ваш тест глазами пользователя, нажмите кнопку ниже:"

    const val TAKE_LINK_AND_LUCK: String = "\nПусть Вам сопутствует удача \uD83C\uDF40\nИ успех ждёт вас впереди ✨"

    const val TAKE_LINK_AND_LUCK_CONGRATULATIONS: String = "\uD83C\uDF1FПоздравляем Вас с созданием теста!\n"

    const val SUPPORT_TESTS_MESSAGE: String = "Этот тест создан специально  для Вас 💖\n\n"

    const val MY_TESTS_AND_LINK_MESSAGE: String = "🎯 Ваши тесты и их результаты:\n\nНажмите, чтобы получить ссылку для прохождения"

    const val ABOUT_RMP: String = "🎀 <b>Как это работает?</b> 🎀\n\n" +
                                  "Ты создаёшь персонализированный тест:\n" +
                                  "✨ Выбирай <b>свои варианты ответов</b> как правильные\n" +
                                  "💞 Твой партнёр позже попробует угадать их\n" +
                                  "📌 Вопросы могут быть о вас обоих или ваших отношениях\n\n" +
                                  "<i>Создай самый честный тест о <b>вас</b>!</i>"








    /**  Reply-клавиатура функционального слова "start" с кнопкой "Главное меню" **/
    fun mainRK() = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Создать тест ✏\uFE0F"))  //  ✏️️ (карандаш)
                add(KeyboardButton("Пройти тест \uD83D\uDCDD"))   // 📝 (записи)
            },
            KeyboardRow().apply {
                add(KeyboardButton("Главное меню \uD83C\uDFE0"))  // 🏠 (дом)
            },
            KeyboardRow().apply {
                add(KeyboardButton("Мои тесты \uD83D\uDDC2"))     // 🗂 (папка с документами)
                add(KeyboardButton("Результаты \uD83D\uDCCA"))    // 📊 (график)
            },
            KeyboardRow().apply {
                add(KeyboardButton("Оценить \u2B50"))             // ⭐ (звезда)
//                add(KeyboardButton("Отблагодарить \uD83D\uDCB8")) // 💸 (деньги)
            },
        )
        resizeKeyboard = true
        oneTimeKeyboard = true
    }

    /**  Inline-клавиатура "Создание теста" **/
    fun createTestIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("✨ Создать собственный").apply { callbackData = "createYourOwnPoll"}
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDCC2 Посмотреть готовые").apply { callbackData = "seeReadyMadePoll" }
            ),
            listOf(
                InlineKeyboardButton("〈〈〈〈   Назад 〈〈〈〈").apply { callbackData = "backToMainMenu" }
            )
        )
    }

    /**  Inline-клавиатура "Прохождение теста"  **/
    fun takeTestIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("\uD83C\uDF0D Общедоступный").apply { callbackData = "takePublicTest" }
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDD12 Личный").apply { callbackData = "takePersonalTest" }
            ),
            listOf(
                InlineKeyboardButton("〈〈   Назад 〈〈").apply { callbackData = "backToMainMenu" }
            ),
        )
    }

    fun createPageKeyboard(
        userState: CallbackData.UserState,
        currentTests: List<CallbackData.SystemTest>,
        callBackPrefix: String,
        startingTestId: Int = 1
    ): InlineKeyboardMarkup {
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()


        // Вычисляем базовый индекс для текущей страницы
        val baseIndex = userState.pageNumberForRMP * userState.testsPerPage + 1

        // Добавление кнопок с номерами тестов (по 2-3 в ряд)
        currentTests.chunked(3).forEach { rowTests ->
            keyboard.add(
                rowTests.map { test ->
                    // Вычисляем отображаемый номер, вычитая смещение
                    val displayNumber = baseIndex + (test.testId - startingTestId -
                            (userState.pageNumberForRMP * userState.testsPerPage))

                    InlineKeyboardButton(displayNumber.toString()).apply {
                        callbackData = "$callBackPrefix${test.testId}"
                    }
                }
            )
        }

        // Добавление навигационных кнопок
        keyboard.add(listOf(
            InlineKeyboardButton("❮").apply {
                callbackData = "previousTestsNavigation_${callBackPrefix}${startingTestId}"
            },
            InlineKeyboardButton("${userState.pageNumberForRMP + 1}/${userState.totalPages}").apply {
                callbackData = "pageInfo"
            },
            InlineKeyboardButton("❯").apply {
                callbackData = "nextTestsNavigation_${callBackPrefix}${startingTestId}"
            }
        ))

        // Кнопка возврата в главное меню
        keyboard.add(
            listOf(
                InlineKeyboardButton("Главное меню \uD83C\uDFE0").apply { callbackData = "backToMainMenuFromPage" }
            )
        )
        if (callBackPrefix == "testId_")
        keyboard.add(
            listOf(
                InlineKeyboardButton("\uD83C\uDF80 Как это работает? \uD83C\uDF80").apply { callbackData = "howWorkRMP" }
            )
        )

        return InlineKeyboardMarkup().apply {
            this.keyboard = keyboard
        }
    }

    /** Inline-клавиатура панели с "Моими тестами" **/
    fun windowWithMyTestsIK(userState: CallbackData.UserState) = InlineKeyboardMarkup().apply {
        keyboard = userState.usersTestsMutableList.map { testId ->
            val (buttonText, callbackData) = getTestButtonInfo(userState, 0, testId, "show_test")
            listOf(
                InlineKeyboardButton(buttonText).apply {
                    this.callbackData = callbackData
                },
            )
        }.toMutableList()

        // Добавляем кнопку "Удалить тест"
        keyboard.add(
            listOf(
                InlineKeyboardButton("\uD83D\uDDD1\uFE0F Удалить тест").apply {
                    this.callbackData = "deleteMyTests"
                }
            )
        )
    }

    /** Inline-клавиатура панели с "Результаты" **/
    fun windowWithMyResults(
        chatId: Long,
        dbQuestionHelper: DatabaseQuestionHelper,
        dbResultsHelper: DatabaseResultsHelper
    ): InlineKeyboardMarkup {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        userState.usersResultsMutableList.forEach { resultId ->
            val resultInfo = dbResultsHelper.getResultInfo(resultId)

            if (resultInfo != null) {
                val (authorTestId, timestamp) = resultInfo
                val testName = dbQuestionHelper.readTestName(authorTestId)?.takeIf { it.isNotBlank() } ?: "Без названия"

                // Получаем номер текущей попытки
                val currentAttemptNumber = getAttemptNumber(authorTestId, timestamp, dbResultsHelper)

                val buttonText = "📝 $testName | ${currentAttemptNumber}-я поп."

                keyboard.add(
                    listOf(
                        InlineKeyboardButton(buttonText).apply {
                            callbackData = "show_result:$resultId"
                        }
                    )
                )
            }
        }

        return InlineKeyboardMarkup().apply {
            this.keyboard = keyboard
        }
    }

    private fun getAttemptNumber(authorTestId: String, timestamp: Long, dbResultsHelper: DatabaseResultsHelper): Int {
        // Получаем все попытки для данного теста
        val allAttempts = dbResultsHelper.getAllAttemptsForTest(authorTestId)

        // Удаляем дубликаты по resultId
        val uniqueAttempts = allAttempts.distinctBy { it.resultId }

        // Сортируем попытки по времени (от старых к новым)
        val sortedAttempts = uniqueAttempts.sortedBy { it.timestamp }

        // Находим индекс текущей попытки в отсортированном списке
        val attemptIndex = sortedAttempts.indexOfFirst { it.timestamp == timestamp }

        // Если попытка найдена, возвращаем её номер (начиная с 1)
        return if (attemptIndex != -1) attemptIndex + 1 else 1
    }

    /** Inline-клавиатура панели удаления тестов **/
    fun deleteMyTestsIK(userState: CallbackData.UserState) = InlineKeyboardMarkup().apply {
        keyboard = userState.usersTestsMutableList.map { testId ->
            val (buttonText, callbackData) = getTestButtonInfo(userState, 0, testId, "delete_my_test")
            listOf(
                InlineKeyboardButton(buttonText).apply {
                    this.callbackData = callbackData
                },
            )
        }.toMutableList()

        // Добавляем кнопку "〈〈   Назад 〈〈"
        keyboard.add(
            listOf(
                InlineKeyboardButton("〈〈   Назад 〈〈").apply {
                    this.callbackData = "backToMyTestWindow"
                }
            )
        )
    }

    /** Inline-клавиатура панели подтверждения удаления тестов **/
    fun agreeToDeleteTestIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("✅").apply { callbackData = "deleting_agree"},
                InlineKeyboardButton("❌").apply { callbackData = "deleting_disagree"}
            ),
        )
    }

    /** Inline-клавиатура панели доступа к прохождению теста **/
    fun windowGetRuleForTakeTestIK(authorUserState: CallbackData.UserState, chatId: Long) = InlineKeyboardMarkup().apply {
        keyboard = authorUserState.usersTestsMutableList.map { testId ->
            val (buttonText, callbackData) = getTestButtonInfo(authorUserState, chatId, testId, "for")
            listOf(
                InlineKeyboardButton(buttonText).apply {
                    this.callbackData = callbackData
                },
            )
        }.toMutableList()

        // Добавляем кнопку "Запрета прохождения" с сохранением данных
        val disableStorageKey = UUID.randomUUID().toString().take(10)
        callbackStorage[disableStorageKey] = CallbackData.TestData(
            chatId = chatId, // chatId пользователя которому будет введен запрет
        )

        keyboard.add(
            listOf(
                InlineKeyboardButton("Запретить прохождение ❌").apply {
                    this.callbackData = "disableTakingPoll_$disableStorageKey"
                }
            )
        )
    }

    /** Inline-клавиатура c предложением пропуска/генерации названия теста **/
    fun skipNamingTheTestOrGenerateIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("⏭\uFE0F Пропустить").apply { callbackData = "skipNamingTest" }
            ),
            listOf(
                InlineKeyboardButton("⚡ Сгенерировать").apply { callbackData = "generateTestName" }
            )
        )
    }

    /** Inline-клавиатура генерации названия теста **/
    fun generateTestNameIK(generatedName: String = "") = InlineKeyboardMarkup().apply {

        // Генерация ключа для доступа к переменным
        val storageKey = UUID.randomUUID().toString().take(5) // Укороченный уникальный ключ
        callbackStorage[storageKey] = CallbackData.TestData(0, generatedName, "") // Шифрование значений по ключу

        keyboard = listOf(
            listOf(
                InlineKeyboardButton("✅ Использовать").apply {
                    this.callbackData = "useGeneratedName_${storageKey}" // Помещаем сгенерированное название
                }
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDD04 Сгенерировать другое").apply {
                    this.callbackData = "generateTestName"
                }
            ),
            listOf(
                InlineKeyboardButton("⏭\uFE0F Пропустить").apply {
                    this.callbackData = "skipNamingTest"
                }
            )
        )
    }

    /** Inline-клавиатура для просмотра результатов **/
    fun seeSummaryResultIK(resultId: String, url: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("Посмотреть результаты \uD83D\uDCCA").apply { callbackData = "seeTestResult_${resultId}"}
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDCE8 Ответить").apply { this.url = url }
            )
        )
    }

    /** Inline-клавиатура подтверждения прохождения собственного теста **/
    fun takeMyOwnTestIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("🎮 Всё равно хочу пройти свой тест!").apply { callbackData = "startMyOwnTest" }
            )
        )
    }

    /** Inline-клавиатура в случае само-прохождения **/
    fun createTestOnlyIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("Создать тест ✏\uFE0F").apply { callbackData = "createTestOnly" }
            ),
            listOf(
                InlineKeyboardButton("Главное меню \uD83C\uDFE0").apply { callbackData = "backToMainMenu" }
            ),
        )
    }

    /** Inline-клавиатура панели оценки **/
    fun rateBotIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                *Array(5) { index ->
                    InlineKeyboardButton("♡").apply {
                        callbackData = "${CallbackData.RATE_PREFIX}${index + 1}"
                    }
                }
            )
        )
    }

    /** Inline-клавиатура заполненой панели оценки **/
    fun filledRatingIK(rating: Int) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                *Array(5) { index ->
                    InlineKeyboardButton(if (index < rating) "💖" else "♡").apply {
                        callbackData = "${CallbackData.RATE_PREFIX}${index + 1}"
                    }
                }
            )
        )
    }

    /** Inline-клавиатура сообщения с оплатой **/
    fun donateSomeSumIK()  = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton().apply {
                    text = "🎁 Поддержать проект"
                    callbackData = "donateSomeSum"
                }
            )
        )
    }

    /** Inline-клавиатура выбора суммы для доната **/
    fun chooseSumIK() = InlineKeyboardMarkup(). apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("60 RUB").apply { callbackData = "donateSum_60" },
                InlineKeyboardButton("90 RUB").apply { callbackData = "donateSum_90" },
            ),
            listOf(
                InlineKeyboardButton("100 RUB").apply { callbackData = "donateSum_100" },
                InlineKeyboardButton("150 RUB").apply { callbackData = "donateSum_150" },
            ),
            listOf(
                InlineKeyboardButton("200 RUB").apply { callbackData = "donateSum_300" },
                InlineKeyboardButton("300 RUB").apply { callbackData = "donateSum_300" },
            ),
            listOf(
                InlineKeyboardButton("Другая сумма ✍\uFE0F").apply { callbackData = "donateСustomSum" },
            ),
        )
    }

    /** Inline-клавиатура для обработки "подарка"**/
    fun addPepperIK(testId: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("📸 Загрузить фото").apply { callbackData =  "upload_photo_$testId"},
                InlineKeyboardButton("\uD83D\uDCCA Установить порог").apply { callbackData =  "set_threshold_$testId"},
            ),
            listOf(
                InlineKeyboardButton("👤 Указать username").apply { callbackData =  "set_username_$testId"},
                InlineKeyboardButton("❓ Колич-во попыток").apply { callbackData =  "set_num_attempt_$testId"},
            ),
            listOf(
                InlineKeyboardButton("✅ Готово").apply { callbackData =  "pepper_done_$testId"},
            ),
            listOf(
                InlineKeyboardButton("◀\uFE0F Назад к ответам").apply { callbackData =  "back_to_ans_$testId"},
            )
        )
    }

    /** Inline-клавиатура выбора порогового значение**/
    fun chooseThreshold(testId: String, startHold: Int) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("➖").apply { callbackData =  "${testId}_hold_minus_5"},
                InlineKeyboardButton("${startHold}%").apply { callbackData =  "${testId}_hold_current_$startHold"},
                InlineKeyboardButton("➕").apply { callbackData =  "${testId}_hold_plus_5"},
            ),
            listOf(
                InlineKeyboardButton("✅ Установить порог").apply { this.callbackData = "${testId}_hold_finish_$startHold" }
            ),
            listOf(
                InlineKeyboardButton("◀\uFE0F Назад").apply { this.callbackData = "back_to_pepper_menu_$testId" }
            ),
        )
    }

    /** Inline-клавиатура выбора количества попыток **/
    fun chooseAttempts(testId: String, startAttempts: Int) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("➖").apply { callbackData = "${testId}_attempts_minus_1" },
                InlineKeyboardButton("$startAttempts").apply { callbackData = "${testId}_attempts_current_$startAttempts" },
                InlineKeyboardButton("➕").apply { callbackData = "${testId}_attempts_plus_1" },
            ),
            listOf(
                InlineKeyboardButton("✅ Установить попытки").apply { this.callbackData = "${testId}_attempts_finish_$startAttempts" }
            ),
            listOf(
                InlineKeyboardButton("◀\uFE0F Назад").apply { this.callbackData = "back_to_pepper_menu_$testId" },
            ),
        )
    }

    /** Функция открытия панели "Результаты" **/
    fun showWindowOfResults(
        bot: TelegramLongPollingBot,
        chatId: Long,
        dbQuiziHelper: DatabaseQuiziHelper,
        dbQuestionHelper: DatabaseQuestionHelper,
        dbResultsHelper: DatabaseResultsHelper
    ) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Получаем список resultId пользователя
        userState.usersResultsMutableList = dbQuiziHelper.readArrayOfResultId(chatId).filterNotNull()

        // Если список результатов пуст
        if (userState.usersResultsMutableList.isEmpty()) {
            if (userState.keyboardAfterMainMenuMID != 0) {
                editMessage(
                    bot,
                    chatId,
                    userState.keyboardAfterMainMenuMID,
                    "Вы пока не прошли ни одного теста\nКакой именно Вы хотите пройти?",
                    inlineKeyboard = takeTestIK(),
                )
            } else {
                userState.keyboardAfterMainMenuMID = sendMessage(
                    bot,
                    chatId,
                    "Вы пока не прошли ни одного теста\nКакой именно Вы хотите пройти?",
                    inlineKeyboard = takeTestIK(),
                )?.messageId ?: 0
            }

            return
        }

        if (userState.myResultsMID != 0) {
            editMessage(
                bot,
                chatId,
                userState.myResultsMID,
                "📊 Тесты, которые Вы прошли:",
                inlineKeyboard = windowWithMyResults(chatId, dbQuestionHelper, dbResultsHelper),
                parseMode = ParseMode.HTML
            )
        } else {
            userState.myResultsMID = sendMessage(
                bot,
                chatId,
                "📊 Тесты, которые Вы прошли:",
                inlineKeyboard = windowWithMyResults(chatId, dbQuestionHelper, dbResultsHelper),
                parseMode = ParseMode.HTML
            )?.messageId ?: 0
        }

        // Сохранение меню управления при помощи "Что-либо ещё?"
        Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
    }




    /** Функция открытия панели "Мои тесты" **/
    fun showWindowsWithMyTests(bot: TelegramLongPollingBot, chatId: Long, dbQuiziHelper: DatabaseQuiziHelper) {
        val userState = userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Получаем список testId пользователя по chatId
        userState.usersTestsMutableList = dbQuiziHelper.readArrayOfTestId(chatId).filterNotNull()

        if (userState.usersTestsMutableList.isEmpty()) {

            // Очистка предыдущих сообщений
            TextSender.clearPreviousMessages(
                userState,
                bot,
                chatId,
                userState.mainMenuMID,
                userState.startNowMID,
                userState.somethingElseMID,
                userState.myTestsMID,
            )

            if (userState.keyboardAfterMainMenuMID != 0) {
                editMessage(
                    bot,
                    chatId,
                    userState.keyboardAfterMainMenuMID,
                    "Вы пока не создали ни одного теста\nКак именно Вы хотите создать?",
                    inlineKeyboard = createTestIK(),
                )
            } else {
                userState.keyboardAfterMainMenuMID = sendMessage(
                    bot,
                    chatId,
                    "Вы пока не создали ни одного теста\nКак именно Вы хотите создать?",
                    inlineKeyboard = createTestIK(),
                )?.messageId ?: 0
            }

            // Генерация testId с проверкой
            Generation.randomTestIdGeneration(chatId)

            // Завершаем данную функцию
            return
        }

        // Формируем сообщение с информацией о тестах
        userState.myTestsMID = sendMessage(
            bot,
            chatId,
            MY_TESTS_AND_LINK_MESSAGE,
            inlineKeyboard = windowWithMyTestsIK(userState),
        )?.messageId ?: 0

        // Сохранение меню управления при помощи "Что-либо ещё?"
        Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
        // Обнуление параметров состояния
        userState.testName = ""
    }

    /** Функция генерации панели тестов **/
    private fun getTestButtonInfo(userState: CallbackData.UserState, chatId: Long, testId: String, prefix: String): Pair<String, String> {
        // Параметры теста по testId
        userState.testName = dbQuestionHelper.readTestName(testId)
        userState.completionPercent = dbQuestionHelper.readComPercent(testId)

        // Создаем текст и callback.data для кнопки
        val buttonText = if (userState.testName == "") {
            "\"Без названия\" \t\t\uD83D\uDCCA  %.1f%%" .format(userState.completionPercent)
        } else {
            "\"${userState.testName}\" \t\t\uD83D\uDCCA %.1f%%" .format(userState.completionPercent)
        }

        // Генерация ключа для доступа к переменным
        val storageKey = UUID.randomUUID().toString().take(10) // Короткий уникальный ключ
        callbackStorage[storageKey] = CallbackData.TestData(chatId, userState.testName, testId) // Шифрование значений по ключу
        val callbackData = "${prefix}_$storageKey"

        return Pair(buttonText, callbackData)
    }

    /** Функция показа результатов **/
    fun seeResultParametr(resultId: String, reserveUsername: String? = "Анонимный пользователь"): String {
        // Получаем информацию о результате
        val (authorTestId, timestamp) = dbResultsHelper.getResultInfo(resultId) ?: run {
            return "❌ Ошибка загрузки результатов: результат не найден."
        }

        // Получаем основные данные
        val testName = dbQuestionHelper.readTestName(authorTestId) ?: "Неизвестный тест"

        var (authorName, userName) = dbResultsHelper.getPersonInfo(resultId) ?: run {
            return "❌ Ошибка загрузки информации: результат не найден."
        }
        if (authorName.isBlank() || userName.isBlank()) {
            authorName = "SomeAuthor"
            userName = reserveUsername ?: "Анонимный пользователь"
        }

        // Форматирование времени
        val dateFormat = SimpleDateFormat("dd MMMM yyyy • HH:mm", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("Europe/Moscow")
        }
        val formattedDateTime = dateFormat.format(Date(timestamp))

        // Получаем вопросы теста
        val questions = dbQuestionHelper.readQuestionFromQuestionDb(authorTestId)
        if (questions.isEmpty()) {
            return "❌ Ошибка загрузки вопросов: тест не содержит вопросов."
        }

        // Получаем все выбранные индексы для каждого вопроса
        val choosedIndexes = dbResultsHelper.getChoosedIndexes(resultId)

        // Формируем сообщение
        return buildString {
            append("\uD83C\uDF93 <b>Результаты теста</b>\n\n")

            // Шапка с общей информацией
            append("\uD83D\uDCDD <b>Название:</b> \"$testName\"\n")
            append("\uD83D\uDC64 <b>Автор:</b> @$authorName\n")
            append("\uD83D\uDC64 <b>Прошёл:</b> @$userName\n")
            append("⏱️ <b>Завершен:</b> $formattedDateTime\n\n")
            append("${"─".repeat(18)}\n\n")

            // Детализация по вопросам
            questions.forEachIndexed { index, question ->
                append("🔹 <b>Вопрос №${index + 1}</b>\n")
                append("<i>${question.questionText}</i>\n\n")

                question.listOfAnswers.forEachIndexed { answerIndex, answerText ->
                    // Получаем ответ пользователя для текущего вопроса
                    val choosedIndex = choosedIndexes.getOrNull(index) ?: -1

                    val isCorrect = (answerIndex == question.indexOfRightAnswer)
                    val isUserChoice = (answerIndex == choosedIndex)

                    val emoji = when {
                        isCorrect && isUserChoice -> "🟢✅"  // Правильный ответ выбран
                        isCorrect -> "✅"                   // Правильный ответ (не выбран)
                        isUserChoice -> "🔴❌"              // Неправильный выбор
                        else -> "⚪▫️"                      // Нейтральный вариант
                    }

                    append("$emoji  $answerText\n")

                    when {
                        choosedIndex == -1 -> append("\n⚠️ <i><code>Ответ отсутствует</code></i>\n")
                    }
                }

                append("\n${"─".repeat(18)}\n\n")
            }

            // Футер
            append("<i>Отчет сгенерирован автоматически\n")
            append("ID результата: <code>$resultId</code></i>")
        }
    }

    /** Функция создания сообщения для показа ответов **/
    fun createMessageForMyAnswers (testId: String) : String {
        // Получаем вопросы теста
        val questions = dbQuestionHelper.readQuestionFromQuestionDb(testId)
        if (questions.isEmpty()) {
            return "❌ Ошибка загрузки вопросов: тест не содержит вопросов."
        }

        // Формируем сообщение
        return buildString {
            append("\uD83C\uDF93 <b>Ответы на тест</b>\n\n")
            append("${"─".repeat(18)}\n\n")

            // Детализация по вопросам
            questions.forEachIndexed { index, question ->

                append("🔹 <b>Вопрос №${index + 1}</b>\n")
                append("<i>${question.questionText}</i>\n\n")

                question.listOfAnswers.forEachIndexed { answerIndex, answerText ->
                    val isCorrect = answerIndex == question.indexOfRightAnswer

                    val emoji = when {
                        isCorrect  -> "✅"  // Правильный ответ выбран
                        else -> "▫️"                      // Нейтральный вариант
                    }

                    append("$emoji  $answerText\n")
                }

                append("\n${"─".repeat(18)}\n\n")
            }
            // Футер
            append("<i>Отчет сгенерирован автоматически\n")
            append("ID результата: <code>$testId</code></i>")
        }
    }

    /** Inline-кнопка "Пройти тест"**/
    fun takeTestSetUrlIB(url: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("Пройти тест \uD83C\uDFAF").apply { this.url = url }
            ),
        )
    }

    /** Inline-кнопка "Назад к RMP"**/
    fun backToRMPIB() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("⬅\uFE0F Вернуться").apply { this.callbackData = "backToRMP" }
            )
        )
    }

    /** Inline-кнопка "Назад к RMP"**/
    fun backToPepperMenuFromMediaIB(testId: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("◀\uFE0F Назад").apply { callbackData =  "media_back_to_pepper_menu_$testId"},
            )
        )
    }

    /** Inline-кнопка "Назад к меню Pepper"**/
    fun backToPepperMenuIB(testId: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("◀\uFE0F Назад").apply { this.callbackData = "back_to_pepper_menu_$testId" }
            )
        )
    }

    /** Inline-клавиатура "Глянуть ответы"**/
    fun seeMyAnswersIK(testId: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("Глянуть ответы \uD83D\uDC40").apply { this.callbackData = "my_choosed_$testId" }
            ),
            listOf(
                InlineKeyboardButton("Добавить перчинки \uD83C\uDF36").apply { callbackData = "add_some_pepper_to_$testId" }
            )
        )
    }

    /** Reply-кнопка с функциоальным словом "Назад" **/
    fun backRB() = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Назад"))
            },
        )
        resizeKeyboard = true
        oneTimeKeyboard = true
        isPersistent = true
    }

    /** Reply-клавиатуры с функциоальным словом "Назад" и "Завершить" **/
    fun returnOrFinishRK() = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Назад"))
                add(KeyboardButton("Завершить"))
            },
        )
        resizeKeyboard = true
        oneTimeKeyboard = true
        isPersistent = true
    }

    /** Reply-кнопка с функциоальным словом "Завершить" **/
    fun finishRB() = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Завершить"))
            },
        )
        resizeKeyboard = true
        oneTimeKeyboard = true
        isPersistent = true
    }
}