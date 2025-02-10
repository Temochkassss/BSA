package Methods

import Methods.TextSender.clearPreviousMessages
import Methods.TextSender.editMessage
import Methods.TextSender.sendMessage
import StringForBot
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object Payment {
    const val YOOKASSA_SHOP_ID = "1019095"
    const val YOOKASSA_SECRET_KEY = "live_CjlL6CRpKzjajEHVOV5-fVKiZh3Csp0ohM0luJMIfq8"
    const val PROVIDER_TOKEN = "381764678:TEST:109230"
    const val CURRENCY = "RUB"
    private const val DESCRIPTION = "Пожертвование на развитие этой малышки \uD83E\uDD18"

    fun сhooseSum(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Редактируем сообщение с новым содержанием и inline-клавиатурой
        editMessage(
            bot,
            chatId,
            userState.donateMID,
            "<b>Предлагаемые суммы:</b>",
            inlineKeyboard = StringForBot.chooseSumIK(),
            parseMode = ParseMode.HTML
        )

    }

    fun donateСustomPrice(bot: TelegramLongPollingBot, chatId: Long) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }

        // Очистка предыдущих сообщений
        clearPreviousMessages(userState, bot, chatId, userState.somethingElseMID)

        // Отправляем сообщение с новым содержанием
        editMessage(
            bot,
            chatId,
            userState.donateMID,
            "<b>Введите сумму ниже:</b>",
            parseMode = ParseMode.HTML
        )

        userState.pollCreationState = CallbackData.PollCreationState.WaitingCustomDonationSum()
    }

    // Функция, которая обрабатывает платежи для Telegram-бота
    fun donateSetSum(bot: TelegramLongPollingBot, chatId: Long, dataSum: String) {
        val userState = CallbackData.userStates.getOrPut(chatId) { CallbackData.UserState() }
        val donationAmount = dataSum.removePrefix("donateSum_").toInt()

        clearPreviousMessages(userState, bot, chatId, userState.donateMID, userState.somethingElseMID)
        userState.donationSum = donationAmount * 100

        val invoice = SendInvoice.builder()
            .chatId(chatId)
            .title("Поддержка проекта")
            .description(DESCRIPTION)
            .payload("payment-$chatId-${System.currentTimeMillis()}") // Уникальный идентификатор
            .providerToken(PROVIDER_TOKEN) // Используем корректный токен авторизации
            .currency(CURRENCY)
            .prices(listOf(LabeledPrice("Поддержка проекта \uD83C\uDF81", userState.donationSum)))
            .photoUrl("https://i.postimg.cc/G2jtqjg7/photo-2025-01-25-18-03-09.jpg")
            .photoSize(400)
            .photoHeight(400)
            .photoWidth(400)
            .providerData(createProviderData(chatId))
            .startParameter("donate_${System.currentTimeMillis()}")
            .needEmail(true)
            .needShippingAddress(false) // Отключаем запрос адреса, если не требуется
            .isFlexible(false) // Фиксированная стоимость
            .build()

        try {
            bot.execute(invoice)
        } catch (e: TelegramApiException) {
            println("Error sending invoice: ${e.message}")
            sendMessage(bot, chatId, "Ошибка при создании платежа. Попробуйте позже.")
        }

        Generation.somethingElseKeyboardGeneration(bot, chatId, userState)
    }

//    private fun createProviderData(chatId: Long): String {
//        val userState = CallbackData.userStates.getOrPut(chatId){ CallbackData.UserState()}
//        val gson = Gson()
//        val receiptData = mapOf(
//            "receipt" to mapOf(
//                "items" to listOf(
//                    mapOf(
//                        "description" to "Описание товара",
//                        "quantity" to "1.00",
//                        "amount" to mapOf(
//                            "value" to (userState.donationSum / 100.0).toString(),
//                            "currency" to CURRENCY
//                        ),
//                        "vat_code" to 1
//                    )
//                )
//            )
//        )
//        return gson.toJson(receiptData)
//    }


    private fun createProviderData(chatId: Long): String {
        val userState = CallbackData.userStates[chatId] ?: return "{}"
        val gson = GsonBuilder().disableHtmlEscaping().create()

        val now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"))
        val documentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        println("documentDate = $documentDate")

        val receipt = mapOf(
//            "customer" to mapOf( // Добавить информацию о покупателе
//                "email" to "example@example.com"  // Обязательное поле
//            ),
            "items" to listOf(
                mapOf(
                    "description" to DESCRIPTION,
                    "quantity" to "1.00",
                    "amount" to mapOf(
                        "value" to (userState.donationSum / 100.0).toString(),
                        "currency" to CURRENCY
                    ),
                    "vat_code" to "1",
                    "payment_object" to "service",
                    "payment_mode" to "full_prepayment"
                )
            ),
            "tax_system_code" to "1",
//            "receipt_industry_details" to listOf( // Добавить детали чека
//                mapOf(
//                    "federal_id" to "001",
//                    "document_date" to documentDate,
//                    "document_number" to "123",
//                    "value" to "1"
//                )
//            )
        )

        return gson.toJson(mapOf("receipt" to receipt))
    }


}
