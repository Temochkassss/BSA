import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.concurrent.thread

class TelegramBotApplication

fun main() {
    /**  Регистрация телеграмм бота  **/
    val bot = Bot()
    TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)
}
