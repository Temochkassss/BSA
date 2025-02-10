import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json

object YooKassaWebhookServer {
    fun start() {
        // Запуск Ktor сервера на порту 8080
        embeddedServer(Netty, port = 8080) {
            // Подключение плагина для работы с JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            // Настройка маршрутов
            routing {
                // Обработка POST-запросов на /yookassa-webhook
                post("/yookassa-webhook") {
                    try {
                        // Получение JSON-тела запроса
                        val notification = call.receive<Map<String, Any>>()
                        println("Received notification: $notification")

                        // Обработка уведомления
                        handleYooKassaNotification(notification)

                        // Отправка успешного ответа
                        call.respond(HttpStatusCode.OK, "Notification received")
                    } catch (e: Exception) {
                        println("Error processing notification: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, "Invalid notification")
                    }
                }
            }
        }.start(wait = true)
    }

    // Функция для обработки уведомлений от ЮKassa
    private fun handleYooKassaNotification(notification: Map<String, Any>) {
        val paymentId = notification["id"] as String
        val status = notification["status"] as String
        val chatId = (notification["metadata"] as Map<*, *>)["chatId"] as String

        when (status) {
            "succeeded" -> {
                println("Payment $paymentId succeeded for chatId $chatId")
                // Здесь можно добавить логику для отправки сообщения в Telegram
            }
            "canceled" -> {
                println("Payment $paymentId canceled for chatId $chatId")
            }
            else -> {
                println("Payment $paymentId has status: $status")
            }
        }

        // Вызываем обработчик в боте
    }
}