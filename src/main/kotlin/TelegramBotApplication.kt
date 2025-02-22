//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import okhttp3.mockwebserver.Dispatcher
//import okhttp3.mockwebserver.RecordedRequest
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//import org.telegram.telegrambots.meta.TelegramBotsApi
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
//
//@Serializable
//data class YooKassaNotification(
//    val event: String,
//    val `object`: PaymentObject // Используем обратные кавычки, так как `object` — зарезервированное слово в Kotlin
//) {
//    @Serializable
//    data class PaymentObject(
//        val id: String,
//        val status: String,
//        val amount: Amount,
//        val description: String
//    ) {
//        @Serializable
//        data class Amount(
//            val value: String,
//            val currency: String
//        )
//    }
//}
//
//fun main() {
//    // Запуск вебхук-сервера в отдельной корутине
//    GlobalScope.launch {
//        val server = MockWebServer()
//        server.start(8080)
//
//        server.dispatcher = object : Dispatcher() {
//            override fun dispatch(request: RecordedRequest): MockResponse {
//                return when (request.path) {
//                    "/yookassa/webhook" -> {
//                        val body = request.body.readUtf8()
//                        val notification = Json.decodeFromString<YooKassaNotification>(body)
//                        when (notification.event) {
//                            "payment.waiting_for_capture" -> {
//                                println("Payment ${notification.`object`.id} is waiting for capture")
//                            }
//                            "payment.succeeded" -> {
//                                println("Payment ${notification.`object`.id} succeeded")
//                            }
//                            "payment.canceled" -> {
//                                println("Payment ${notification.`object`.id} canceled")
//                            }
//                        }
//                        MockResponse().setResponseCode(200)
//                    }
//                    else -> MockResponse().setResponseCode(404)
//                }
//            }
//        }
//
//        println("Webhook server started at http://localhost:8080")
//    }
//
//    // Запуск Telegram-бота
//    val bot = Bot()
//    val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
//    telegramBotsApi.registerBot(bot)
//    println("Telegram bot started!")
//
//}



import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class TelegramBotApplication

fun main() {
    /**  Регистрация телеграмм бота  **/
    val bot = Bot()
    TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)
}
