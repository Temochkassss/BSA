package Methods

import Methods.MediaSender.sendSticker
import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.deleteMessage
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import StringForBot
import kotlinx.coroutines.*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

object Animation {
    fun homeStickerAnimation(bot: TelegramLongPollingBot, userState: CallbackData.UserState, chatId: Long) {
        userState.homeStickerAnimationMID = sendMessage(bot, chatId, "\uD83C\uDFE0")?.messageId ?: 0

        CoroutineScope(Dispatchers.Default).launch {
            delay(2300) // Эмпирическое значение задержки, возможно, потребуется корректировка
            withContext(Dispatchers.IO) { //  Dispatchers.IO для сетевых операций (если sendMessage - сетевой вызов)
                editMessage(
                    bot,
                    chatId,
                    userState.homeStickerAnimationMID,
                    StringForBot.HELLO_MESSAGE_V2,
                    parseMode = ParseMode.HTML
                )
                userState.startNowMID = TextSender.sendMessage(
                    bot,
                    chatId,
                    "Начните прямо сейчас!",
                    replyKeyboard = StringForBot.mainRK()
                )?.messageId ?: 0
            }
        }
    }

    fun startCreateTestWithSmile(
        bot: TelegramLongPollingBot,
        chatId: Long,
        userState: CallbackData.UserState,
        smile: String
    ) {

        CoroutineScope(Dispatchers.Default).launch {

            editMessage(bot, chatId, userState.testInstructionsMID, smile)

            delay(2300)

            try {
                editMessage(
                    bot,
                    chatId,
                    userState.testInstructionsMID,
                    "Как именно Вы хотите создать?",
                    inlineKeyboard = StringForBot.createTestIK()
                )
            } catch (e: Exception) {
                println("Error deleting in animation sequence")
            }
        }
    }

    fun openAccessWithSmile(
        bot: TelegramLongPollingBot,
        userState: CallbackData.UserState,
        testName: String,
        smile: String,
    ) {

        CoroutineScope(Dispatchers.Default).launch {

            if (userState.authorAlertForChooseMID != 0) {
                editMessage(
                    bot,
                    userState.authorChatId,
                    userState.authorAlertForChooseMID,
                    smile,
                    parseMode = ParseMode.HTML
                )
            }

            delay(2300)

            try {
                // Редактирование сообщение с информацией о доступе
                val infoAccess = """
                    🎉 <b>Доступ открыт!</b>
                    📝 Тест "<b><i>${testName}</i></b>"
                    👤 Доступен для: <b>@${userState.username}</b>
                    🕒 Время активации: ${userState.currentDate}
                """.trimIndent()
                if (userState.authorAlertForChooseMID != 0) {
                    editMessage(
                        bot,
                        userState.authorChatId,
                        userState.authorAlertForChooseMID,
                        infoAccess,
                        parseMode = ParseMode.HTML
                    )
                }
            } catch (e: Exception) {
                println("Error openAccessWithSmile in animation sequence")
            }

        }
    }

    fun closeAccess(
        bot: TelegramLongPollingBot,
        userState: CallbackData.UserState
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Простая анимация взрыва
                editMessage(
                    bot,
                    userState.authorChatId,
                    userState.authorAlertForChooseMID,
                    "\uD83D\uDDDD\uFE0F"
                )

                delay(2000)

                // Удаляем сообщение
                if (userState.authorAlertForChooseMID != 0) {
                    deleteMessage(
                        bot,
                        userState.authorChatId,
                        userState.authorAlertForChooseMID
                    )
                }

            } catch (e: Exception) {
                println("Error closeAccessWithSimpleExplosion in animation sequence")
            }
        }
    }



    fun startTestAnimation(bot: TelegramLongPollingBot, chatId: Long, userState: CallbackData.UserState) {
        CoroutineScope(Dispatchers.Default).launch {

            // Генерация resultId
            userState.generatedResultId = Generation.randomResultIdGeneration(chatId)

            // Проверка попытки прохождений
            val attemptInfo = userState.mapTestIdNumAttempts[userState.testId]

            if ((attemptInfo?.currentNumberOfAttempts ?: 0) > (attemptInfo?.maxNumberOfAttempts ?: Int.MAX_VALUE)) {
                // Редактируем начальное сообщение
                editMessage(
                    bot,
                    chatId,
                    userState.supportStartTestTextMID,
                    "Вы потратили все свои попытки...\nОбратитесь к автору теста за дополнительными \uD83E\uDEF4 ✨"
                )

                Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
            } else {
                // Улавливаем последнюю попытку
                if ((attemptInfo?.currentNumberOfAttempts ?: 0) == (attemptInfo?.maxNumberOfAttempts ?: Int.MAX_VALUE)) {
                    // Редактируем начальное сообщение
                    withContext(Dispatchers.IO) {
                        editMessage(
                            bot,
                            chatId,
                            userState.supportStartTestTextMID,
                            "\uD83C\uDFC1 Это ваша последняя попытка!"
                        )
                    }
                    delay(1000)
                }
                // Отправляем начальное сообщение
                editMessage(
                    bot,
                    chatId,
                    userState.supportStartTestTextMID,
                    StringForBot.SUPPORT_TESTS_MESSAGE + "➌ Приготовьтесь..."
                )

                delay(1000)

                // Редактируем сообщение - показываем 2
                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        userState.supportStartTestTextMID,
                        StringForBot.SUPPORT_TESTS_MESSAGE + "➋ Почти готово..."
                    )
                }

                delay(1000)

                // Редактируем сообщение - показываем 1
                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        userState.supportStartTestTextMID,
                        StringForBot.SUPPORT_TESTS_MESSAGE + "➊ Поехали! \uD83D\uDE80"
                    )
                }

                delay(1000)

                // Редактируем сообщение - показываем смайлик ракеты
                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        userState.supportStartTestTextMID,
                        "\uD83D\uDE80"
                    )
                }


                delay(1000)

                // Запускаем тест
                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        userState.supportStartTestTextMID,
                        "Прохождение теста ☟"
                    )
                    PollSender.takePollWithParametr(bot, chatId)
                }

                // Генерация текущего времени
                userState.currentTimestamp = System.currentTimeMillis()
            }
        }
    }

    fun generateTestNameWithAnimation(
        bot: TelegramLongPollingBot,
        chatId: Long
    ) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }
        val generatedName = TestNameGenerator.generateName() // Генерируем имя заранее

        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            var currentJob: Job? = null

            try {
                currentJob = launch {
                    val animationFrames = listOf(
                        "🎲 Подбираем идеальное название...",
                        "✨ Добавляем щепотку магии...",
                    )

                    // Анимация процесса генерации
                    for (frame in animationFrames) {
                        try {
                            withTimeout(800) {
                                withContext(Dispatchers.IO) {
                                    editMessage(
                                        bot,
                                        chatId,
                                        userState.adviceToSkipNamingTestMID,
                                        frame
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            throw CancellationException("Animation step failed")
                        }
                        delay(400)
                    }

                    // Оптимизированная анимация печати
                    val revealMessage = "✨ <b>Ваше название:</b>"
                    var displayedName = ""

                    // Разбиваем имя на более крупные части
                    val parts = generatedName.chunked(4)

                    for (part in parts) {
                        displayedName += part
                        try {
                            withTimeout(500) { // Меньший таймаут для каждой операции печати
                                withContext(Dispatchers.IO) {
                                    editMessage(
                                        bot,
                                        chatId,
                                        userState.adviceToSkipNamingTestMID,
                                        """
                                    $revealMessage
                                    
                                    <i><b>"$displayedName▌"</b></i>
                                    """.trimIndent(),
                                        parseMode = ParseMode.HTML
                                    )
                                }
                            }
                            delay(150) // Увеличенная задержка между частями
                        } catch (e: Exception) {
                            // В случае ошибки сразу переходим к финальному сообщению
                            throw CancellationException("Typing animation failed")
                        }
                    }

                    // Финальное сообщение
                    val finalMessage = """
                    ✨ <b>Готово!</b>
                    
                    <i><b>"$generatedName"</b></i>
                    
                    🪄 Нажмите, чтобы использовать это название
                """.trimIndent()

                    withTimeout(1000) {
                        withContext(Dispatchers.IO) {
                            editMessage(
                                bot,
                                chatId,
                                userState.adviceToSkipNamingTestMID,
                                finalMessage,
                                parseMode = ParseMode.HTML,
                                inlineKeyboard = StringForBot.generateTestNameIK(generatedName)
                            )
                        }
                    }
                }

                // Общий таймаут для всей анимации
                withTimeout(4000) {
                    currentJob.join()
                }

            } catch (e: Exception) {
                currentJob?.cancel()

                // Отправляем новое сообщение при любой ошибке
                withContext(Dispatchers.IO) {
                    val finalMessage = """
                    ✨ <b>Ваше название:</b>
                    
                    <i><b>"$generatedName"</b></i>
                    
                    🪄 Нажмите, чтобы использовать это название
                """.trimIndent()

                    userState.adviceToSkipNamingTestMID = sendMessage(
                        bot,
                        chatId,
                        finalMessage,
                        inlineKeyboard = StringForBot.generateTestNameIK(generatedName),
                        parseMode = ParseMode.HTML
                    )?.messageId ?: 0
                }
            }
        }
    }

    fun successfulDonate(
        bot: TelegramLongPollingBot,
        chatId: Long,
        userState: CallbackData.UserState
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(3000) // Увеличенная задержка

                // Отправка стикера с деньгами
                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        userState.successfulPaymentMID,
                        "\uD83D\uDCB8"
                    )
                }
                delay(3000) // Увеличенная задержка

                // Удаление эмодзи
                withContext(Dispatchers.IO) {
                    clearPreviousMessages(userState, bot, chatId, userState.successfulPaymentMID)
                }
                // Отправка стикера с сжиганием денег
                withContext(Dispatchers.IO) {
                    userState.successfulPaymentStickerMID = sendSticker(
                        bot,
                        chatId,
                        "CAACAgIAAxkBAAENckRneWCwWbtOewgx-fc1SpxeTltsNgACSQIAAladvQoqlwydCFMhDjYE"
                    )?.messageId ?: 0
                }
                delay(2500) // Увеличенная задержка

                // Удаление стикера
                withContext(Dispatchers.IO) {
                    clearPreviousMessages(userState, bot, chatId, userState.successfulPaymentStickerMID)
                }

                Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
            } catch (e: Exception) {
                println("Error in successfulDonate animation: ${e.message}")
            }
        }
    }


    fun startWaitingAnimation(
        bot: TelegramLongPollingBot,
        chatId: Long,
        userState: CallbackData.UserState
    ): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            val waitingStates = listOf(
                "⌛️ Ожидаем ответа автора...",
                "⏳ Ожидаем ответа автора...",
                "⌛️ Ожидаем ответа автора...",
                "⏳ Ожидаем ответа автора..."
            )

            var currentIndex = 0
            userState.shouldSendNewMessage = true // Флаг для определения необходимости отправки нового сообщения

            try {
                while (isActive) {
                    withContext(Dispatchers.IO) {
                        val messageText = """
                            🔔 <b>Запрос отправлен!</b>
                        
                            ${waitingStates[currentIndex]}
                        
                            📱 Статус: <i>ожидание разрешения</i>
                            👤 Автор теста: <b>${userState.authorUsername}</b>
                        
                            💡 <i>Вы получите уведомление, как только
                            автор предоставит доступ к тесту</i>
                        """.trimIndent()

                        if (userState.shouldSendNewMessage) {
                            println("// Отправляем новое сообщение")
                            deleteMessage(bot, chatId, userState.testInstructionsMID)
                            userState.testInstructionsMID = sendMessage(
                                bot,
                                chatId,
                                messageText,
                                parseMode = ParseMode.HTML
                            )?.messageId ?: 0
                            userState.shouldSendNewMessage = false
                        } else {
                            if (userState.testInstructionsMID != 0) {
                                try {
                                    // Пробуем отредактировать существующее сообщение
                                    println("// Пробуем отредактировать существующее сообщение")
                                    editMessage(
                                        bot,
                                        chatId,
                                        userState.testInstructionsMID,
                                        messageText,
                                        parseMode = ParseMode.HTML
                                    )
                                    userState.shouldSendNewMessage = false
                                } catch (e: Exception) {
                                    // Если редактирование не удалось, отправим новое сообщение при следующей итерации
                                    userState.shouldSendNewMessage = true
                                    println("Failed to edit message: ${e.message}")
                                }

                            }

                        }
                    }
                    currentIndex = (currentIndex + 1) % waitingStates.size
                    delay(800)
                }
            } catch (e: Exception) {
                println("Animation interrupted: ${e.message}")
            }
        }
    }

    fun startRatingAnimation(bot: TelegramLongPollingBot, chatId: Long, messageId: Int, rating: Int): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            // Различные стили сердец для анимации
            val heartStyles = listOf(
                "♥", "💗", "💓", "💖", "💝", "💘", "💕"
            )

            // Анимационные фреймы для текста
            val animationFrames = listOf(
                "✨ Обработка вашей оценки ✨",
                "⭐ Анализируем отзыв ⭐",
                "🌟 Сохраняем результат 🌟",
                "💫 Почти готово 💫"
            )

            try {
                // Первая фаза - вращающиеся сердца
                repeat(2) {
                    for (heart in heartStyles) {
                        val animatedKeyboard = InlineKeyboardMarkup().apply {
                            keyboard = listOf(
                                listOf(
                                    *Array(5) { index ->
                                        InlineKeyboardButton(
                                            if (index < rating) heart else "♡"
                                        ).apply {
                                            callbackData = "stars_${index + 1}"
                                        }
                                    }
                                )
                            )
                        }

                        withContext(Dispatchers.IO) {
                            editMessage(
                                bot,
                                chatId,
                                messageId,
                                """
                                    ${animationFrames[it % animationFrames.size]}

                                    ${heart.repeat(rating)}

                                    📊 Прогресс: ${(it * 25 + 25)}%
                                """.trimIndent(),
                                parseMode = ParseMode.HTML,
                                inlineKeyboard = animatedKeyboard
                            )
                        }
                        delay(150)
                    }
                }

                // Вторая фаза - волновой эффект
                repeat(2) {
                    for (position in 0..4) {
                        val waveKeyboard = InlineKeyboardMarkup().apply {
                            keyboard = listOf(
                                listOf(
                                    *Array(5) { index ->
                                        InlineKeyboardButton(
                                            when {
                                                index == position -> "💝"
                                                index < rating -> "💖"
                                                else -> "♡"
                                            }
                                        ).apply {
                                            callbackData = "stars_${index + 1}"
                                        }
                                    }
                                )
                            )
                        }

                        withContext(Dispatchers.IO) {
                            editMessage(
                                bot,
                                chatId,
                                messageId,
                                """
                                    ✨ Завершаем обработку...

                                    ${"💖".repeat(rating)}

                                    📊 Прогресс: ${(75 + position * 5)}%
                                """.trimIndent(),
                                parseMode = ParseMode.HTML,
                                inlineKeyboard = waveKeyboard
                            )
                        }
                        delay(200)
                    }
                }

                // Финальное сообщение
                val finalKeyboard = InlineKeyboardMarkup().apply {
                    keyboard = listOf(
                        listOf(
                            *Array(5) { index ->
                                InlineKeyboardButton(
                                    if (index < rating) "💖" else "♡"
                                ).apply {
                                    callbackData = "stars_${index + 1}"
                                }
                            }
                        )
                    )
                }

                withContext(Dispatchers.IO) {
                    editMessage(
                        bot,
                        chatId,
                        messageId,
                        """
                            ✨ Спасибо за вашу оценку! ✨

                            ${"💖".repeat(rating)}

                            🌟 Ваш отзыв очень важен для нас!
                            📊 Итоговая оценка: $rating из 5

                            ${getRandomCompliment(rating)}
                        """.trimIndent(),
                        parseMode = ParseMode.HTML,
                        inlineKeyboard = finalKeyboard
                    )
                }

            } catch (e: Exception) {
                println("Animation error: ${e.message}")
            }
        }
    }

    private fun getRandomCompliment(rating: Int): String {
        return when (rating) {
            5 -> listOf(
                "🎉 Великолепно! Вы сделали наш день!",
                "🌟 Потрясающе! Мы очень ценим вашу поддержку!",
                "💫 Восхитительно! Спасибо за высшую оценку!"
            ).random()

            4 -> listOf(
                "🌟 Отлично! Мы рады, что вам понравилось!",
                "✨ Замечательно! Спасибо за высокую оценку!",
                "💫 Прекрасно! Мы будем стараться стать ещё лучше!"
            ).random()

            3 -> listOf(
                "👍 Спасибо! Мы постараемся стать лучше!",
                "💪 Будем работать над улучшениями!",
                "🌟 Благодарим за честную оценку!"
            ).random()

            else -> listOf(
                "🙏 Спасибо за отзыв! Мы учтём ваше мнение",
                "💫 Благодарим за обратную связь!",
                "✨ Мы обязательно станем лучше!"
            ).random()
        }
    }

}
