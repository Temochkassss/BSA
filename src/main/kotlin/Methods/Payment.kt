package Methods
import Methods.TextSender.sendMessage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.bots.TelegramLongPollingBot

@OptIn(InternalAPI::class)
object Payment {
    const val YOOKASSA_SHOP_ID = "1019095"
    const val YOOKASSA_SECRET_KEY = "live_CjlL6CRpKzjajEHVOV5-fVKiZh3Csp0ohM0luJMIfq8"
    const val PROVIDER_TOKEN = "390540012:LIVE:64209"
    const val CURRENCY = "RUB"
    private const val DESCRIPTION = "Пожертвование на развитие этой малышки \uD83E\uDD18"

    fun handleDonationCommand(bot: TelegramLongPollingBot, chatId: Long) {
        runBlocking {
            val paymentUrl = createYooKassaPayment(100.0, "RUB", "Пожертвование", "https://t.me/and_she_will_ask_bot")
            sendMessage(bot, chatId, "Оплатите по ссылке: $paymentUrl")
        }
    }

    @Serializable
    data class YooKassaPaymentRequest(
        val amount: Amount,
        val capture: Boolean = true,
        val confirmation: Confirmation,
        val description: String
    ) {
        @Serializable
        data class Amount(
            val value: String,
            val currency: String
        )

        @Serializable
        data class Confirmation(
            val type: String = "redirect",
            val return_url: String
        )

        @Serializable
        enum class ConfirmationType {
            REDIRECT,
            EXTERNAL,
            EMBEDDED
        }
    }

    suspend fun createYooKassaPayment(amount: Double, currency: String, description: String, returnUrl: String): String {
        val client = HttpClient()

        val paymentRequest = YooKassaPaymentRequest(
            amount = YooKassaPaymentRequest.Amount(
                value = amount.toString(),
                currency = currency
            ),
            confirmation = YooKassaPaymentRequest.Confirmation(
                type = "redirect",
                return_url = returnUrl
            ),
            description = description
        )


        val json = Json { ignoreUnknownKeys = true } // Настройте Json по необходимости
        val jsonBody = json.encodeToString(paymentRequest)

        val response: HttpResponse = client.post("https://api.yookassa.ru/v3/payments?test=true") {
            headers {
                append(HttpHeaders.Authorization, "Basic ${"$YOOKASSA_SHOP_ID:$YOOKASSA_SECRET_KEY".encodeBase64()}")
                append(HttpHeaders.ContentType, "application/json")
                header("Idempotence-Key", System.currentTimeMillis().toString())
            }
            body = jsonBody
        }

        return response.bodyAsText()
    }

    fun String.encodeBase64(): String {
        return Base64.getEncoder().encodeToString(this.toByteArray())
    }
}
