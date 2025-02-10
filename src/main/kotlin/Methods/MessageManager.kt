package Methods

object MessageManager {
    /** Ключ - chatId, значение - это список messageId **/
    private val messageIdsToDelete = mutableMapOf<Long, MutableList<Int>>()
    /** Функция для добавления MID в список:
     *
     *  * Если в messageIdsToDelete уже есть запись для данного chatId, она возвращает соответствующий список messageId.
     *
     *  * Если записи для данного chatId еще нет, она создает новый пустой MutableList<Long> с помощью лямбда-выражения { mutableListOf() }, связывает его с chatId и возвращает этот новый список.
     *
     *   Добавляем messageId в список, возвращенный getOrPut**/
    fun addMessageToDelete(chatId: Long, messageId: Int) {
        messageIdsToDelete.getOrPut(chatId) { mutableListOf() }.add(messageId)
    }
    /** Функция для возврата списка под удаления: **/
    fun getMessagesToDelete(chatId: Long): List<Int> {
        return messageIdsToDelete[chatId] ?: emptyList()
    }
    /** Очистка словаря по ключу: **/
    fun clearMessagesToDelete(chatId: Long) {
        messageIdsToDelete.remove(chatId)
    }
}