package Database.Quizi

import Database.Quizi.CharForQuiziDb.COLLUMN_NAME_ID
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_CHAT_ID
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_IS_BOT
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_IS_PREMIUM
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_LANGUAGE
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_USER
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_USERNAME
import Database.Quizi.CharForQuiziDb.COUNT_CHAT_ID
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_ARRAY_OF_TEST_ID
import Database.Quizi.CharForQuiziDb.COLUMN_NAME_RATING
import Database.Quizi.CharForQuiziDb.INSERT_TO_TABLE
import Database.Quizi.CharForQuiziDb.READ_ARRAY_OF_RESULTS_ID_BY_CHAT_ID
import Database.Quizi.CharForQuiziDb.READ_CHAT_ID_BY_USERNAME
import Database.Quizi.CharForQuiziDb.READ_ARRAY_OF_TEST_ID_BY_CHAT_ID
import Database.Quizi.CharForQuiziDb.READ_USERNAME_BY_CHAT_ID
import Database.Results.DatabaseResultsHelper
import Methods.CallbackData
import com.google.gson.Gson
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class DatabaseQuiziHelper(
    private val dbName: String
) { // передаём название базы данных

    // чтобы не использовать var connection: Connection? = null и работать с nullable переменной
    private lateinit var connection: Connection // откладываем инициализацию переменной до её первого использования (late init - поздняя  инициализация)


    fun connect() {
        if (connection?.isClosed != false) {
            connection = DriverManager.getConnection("jdbc:sqlite:$dbName")
        }
    }

    fun disconnect() {
        connection?.close()
    }

    fun connectToQuziDb() {
        try {
            val url = "jdbc:sqlite:$dbName" // местоположение файла базы данных SQLite
            connection = DriverManager.getConnection(url) // установления соединения с базой данных по url
            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) connection succeed...")
        } catch (e: SQLException) {
            e.printStackTrace() // трассировка стека ошибки (подробное описание возникновения ошибки)
            println("Error connecting to database: $e")
        }
    }

    fun disconnectFromQuiziDb() {
        try {
            if (!connection.isClosed) { // соединение != null и соединение не закрыто (connection.isClosed != true === !connection.isClosed)
                connection.close() // закрываем активное соединение с базой данных
                println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) diconnection succeed...")
            }
        } catch (e: SQLException) { // если соединение уже было закрыто
            e.printStackTrace()
            println("Error closing database connection: $e")
        }
    }

    fun createQuiziTable() {
        val createTableQuery = CharForQuiziDb.CREATE_TABLE // переменная с sql-запросом для создания таблицы
        try {
            connection.createStatement().execute(createTableQuery) // create statement - создать инструкцию
            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) create table succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error creating table: $e")
        }
    }

    fun insertUser(
        chatId: Long,
        username: String,
        name: String,
        language: String,
        isPremium: Boolean,
        isBot: Boolean,
        rating: Int = 0
    ) {
        connectToQuziDb()
        try {
            // Получение количества пользователь с данным chatId
            val countChatIdQuery = COUNT_CHAT_ID
            val countChatIdStatement = connection.prepareStatement(countChatIdQuery)
            countChatIdStatement.setLong(1, chatId)
            val countChatIdResultSet = countChatIdStatement.executeQuery()
            countChatIdResultSet.next()
            val count = countChatIdResultSet.getInt(1)

            // Если пользоваетль ещё не зарегестрирован в боте
            if (count == 0) {
                val insertUserInfoQuery = INSERT_TO_TABLE
                val insertUserInfoStatement = connection.prepareStatement(insertUserInfoQuery)
                insertUserInfoStatement.apply {
                    setLong(1, chatId)
                    setString(2, username)
                    setString(3, name)
                    setString(4, language)
                    setBoolean(5, isPremium)
                    setBoolean(6, isBot)
                    setString(7, "[]")
                    setString(8, "[]")
                    setInt(9, rating)
                }
                insertUserInfoStatement.executeUpdate()
                println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) insert users succeed...")
            } else {
                println("Данный пользователь \"$username\" уже существует")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error inserting user: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun updateTestId(
        chatId: Long,
        newTestId: String
    ) {
        connectToQuziDb()

        var existingTestIds = mutableListOf<String?>() // Список для хранения testId
        try {
            // Считывание списка существующих testId из базы данных
            existingTestIds = readArrayOfTestId(chatId).toMutableList()

            // Обновляем список testId, если newTestId еще не существует
            val updatedArrayOfTestId = if (newTestId !in existingTestIds) {
                val mutableList = (existingTestIds + newTestId).toMutableList()
                mutableList.toList()
            } else {
                existingTestIds
            }
            println("Обновленный список testId: $updatedArrayOfTestId")
            val arrayOfTestIdJson = Gson().toJson(updatedArrayOfTestId)

            connectToQuziDb()

            // Обновление списка testId
            val updateTestIdListQuery = CharForQuiziDb.UPDATE_TEST_ID_IN_TABLE
            val updateTestIdListStatement = connection.prepareStatement(updateTestIdListQuery)
            updateTestIdListStatement.apply {
                setString(1, arrayOfTestIdJson)
                setLong(2, chatId)
            }
            val updatedRows = updateTestIdListStatement.executeUpdate()
            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) update users succeed, updated $updatedRows rows")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error updating testId: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun updateArrayOfResultsId(
        chatId: Long,
        newResultId: String
    ) {
        connectToQuziDb()

        // Список для хранения resultId
        var existingResultsId = mutableListOf<String?>()

        try {
            // Считывание списка существующих resultId из базы данных
            existingResultsId = readArrayOfResultId(chatId).toMutableList()

            // Обновляем список resultId, если newResultId еще не существует
            val updatedArrayOfResultId = if (newResultId !in existingResultsId) {
                val mutableList = (existingResultsId + newResultId).toMutableList()
                mutableList.toList()
            } else {
                existingResultsId
            }
            println("Обновленный список resultId: $updatedArrayOfResultId")
            val arrayOfResultIdJson = Gson().toJson(updatedArrayOfResultId)

            connectToQuziDb()

            // Обновление списка resultId
            val updateQuery = CharForQuiziDb.UPDATE_RESULTS_ID_IN_TABLE
            val updateStatement = connection.prepareStatement(updateQuery)
            updateStatement.apply {
                setString(1, arrayOfResultIdJson)
                setLong(2, chatId)
            }
            val updatedRows = updateStatement.executeUpdate()
            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) update users succeed, updated $updatedRows rows")

        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error updating resultId: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun updateRating(chatId: Long, rating: Int = 0) {
        connectToQuziDb()

        try {
            val updateRatingQuery = CharForQuiziDb.UPDATE_RATING
            val updateRatingStatement = connection.prepareStatement(updateRatingQuery)
            updateRatingStatement.apply {
                setInt(1, rating)
                setLong(2, chatId)
            }
            val updatedRatingRows = updateRatingStatement.executeUpdate()
            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) update users succeed, updated $updatedRatingRows rows")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error updateRating: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun removeTestIdFromUserArray(chatId: Long, testIdToRemove: String) {
        connectToQuziDb()

        try {
            // Считываем список существующих testId из базы данных
            val currentTestIdList = readArrayOfTestId(chatId)

            // Переподключаемся к базе данных
            connectToQuziDb()

            // Проверяем, существует ли testId в списке
            if (currentTestIdList.contains(testIdToRemove)) {

                // Удаляем выбранный testId из списка
                val updatedTestIdList = currentTestIdList.filter { it != testIdToRemove }
                // Сохраняем обновленный список обратно в базу данных в JSON формате
                val arrayOfTestIdJson = Gson().toJson(updatedTestIdList)

                // Обновление списка testId
                val updateTestIdListQuery = CharForQuiziDb.UPDATE_TEST_ID_IN_TABLE
                val updateTestIdListStatement = connection.prepareStatement(updateTestIdListQuery)
                updateTestIdListStatement.apply {
                    setString(1, arrayOfTestIdJson)
                    setLong(2, chatId)
                }
                val updatedRows = updateTestIdListStatement.executeUpdate()
                println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) removeTestIdFromUserArray succeed, updated $updatedRows rows")

                println("Test ID $testIdToRemove был удален. Обновленный список: $updatedTestIdList")
            } else {
                println("Test ID $testIdToRemove не найден в списке.")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error removeTestIdFromUserArray: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun removeResultIdFromResultArray(chatId: Long, resultIdToRemove: String) {
        connectToQuziDb()

        try {
            // Считываем список существующих resultId из базы данных
            val currentResultIdList = readArrayOfResultId(chatId)

            // Переподключаемся к базе данных
            connectToQuziDb()

            // Проверяем, существует ли resultId в списке
            if (currentResultIdList.contains(resultIdToRemove)) {

                // Удаляем выбранный resultId из списка
                val updatedResultIdList = currentResultIdList.filter { it != resultIdToRemove }
                // Сохраняем обновленный список обратно в базу данных в JSON формате
                val arrayOfResultIdJson = Gson().toJson(updatedResultIdList)

                // Обновление списка resultId
                val query = CharForQuiziDb.UPDATE_RESULTS_ID_IN_TABLE
                val statement = connection.prepareStatement(query)
                statement.apply {
                    setString(1, arrayOfResultIdJson)
                    setLong(2, chatId)
                }
                val updatedRows = statement.executeUpdate()
                println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) removeResultIdFromUserArray succeed, updated $updatedRows rows")

                println("resultId $resultIdToRemove был удален. Обновленный список: $updatedResultIdList")
            } else {
                println("resultId $resultIdToRemove не найден в списке.")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error removeResultIdFromResultArray: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }

    fun deleteResultsByAuthorTestId(userState: CallbackData.UserState, dbResultsHelper: DatabaseResultsHelper) {
        connectToQuziDb()

        try {
            // Получение списка всех chatId и resultId под удаление
            val resultsToRemove = dbResultsHelper.getDeleteInfo(userState.testIdForDeleting)
            if (resultsToRemove.isEmpty()) return

            // Удаляем результаты
            for ((chatId, resultId) in resultsToRemove) {
                removeResultIdFromResultArray(chatId, resultId)

                // Очистка словаря mapResultIdUserResult
                userState.mapResultIdUserResult.remove(resultId)
            }

        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error in deleteResultsByAuthorTestId: $e")
        } finally {
            disconnectFromQuiziDb()
        }
    }


    fun readUsersFromQuiziDb() {
        connectToQuziDb()
        try {
            val selectQuery = CharForQuiziDb.READ_FROM_TABLE // переменная хранящая sql-запрос
            val statement = connection.createStatement() // объект позволяет вам отправлять SQL-запросы и получать результаты
            val resultSet = statement.executeQuery(selectQuery) // результирующий набор

            // последовательно перебираем каждую строку в результирующем наборе resultSet
            while (resultSet.next()) {  // "пока можем перебирать"
                val id = resultSet.getInt(COLLUMN_NAME_ID)
                val chatId = resultSet.getLong(COLUMN_NAME_CHAT_ID)
                val username = resultSet.getString(COLUMN_NAME_USERNAME)
                val name = resultSet.getString(COLUMN_NAME_USER)
                val language = resultSet.getString(COLUMN_NAME_LANGUAGE)
                val isPremium = resultSet.getBoolean(COLUMN_NAME_IS_PREMIUM)
                val isBot = resultSet.getBoolean(COLUMN_NAME_IS_BOT)
                val arrayOfTestIdJson = resultSet.getString(COLUMN_NAME_ARRAY_OF_TEST_ID)
                val arrayOfTestId = if (arrayOfTestIdJson.isNullOrEmpty()) {""} else {Gson().fromJson(arrayOfTestIdJson, Array<String>::class.java).toList()}
                val rating = resultSet.getInt(COLUMN_NAME_RATING)


//                println("ID: $id, \nChatId: $chatId, \nUsername: $username, \nName: $name, \nLanguage: $language, \nIsPremium: $isPremium, \nIsBot: $isBot")
                println("""
                    ------------------------------
                    ID: $id, 
                    ChatId: $chatId, 
                    Username: $username, 
                    Name: $name, 
                    Language: $language, 
                    IsPremium: $isPremium, 
                    IsBot: $isBot,
                    arrayOfTestId: $arrayOfTestId
                    rating: $rating
                """.trimIndent())
            }
            // освобождаём занятые ресурсы
            resultSet.close()
            statement.close()

            println("\uD83D\uDE4B (${CharForQuiziDb.TABLE_NAME}) read from db succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error reading from database: $e")
        } catch (e: Exception) { // любые другие типы исключений, которые могут возникнуть в функции
            e.printStackTrace() // не связаны напрямую с работой над базой данных
            println("Unexpected error: $e")
        }
    }

    fun readChatIdByUsername(username: String? = ""): Long {
        connectToQuziDb()

        var chatId: Long = 0
        try {
            val getChatIdQuery = READ_CHAT_ID_BY_USERNAME
            val getChatIdStatement = connection.prepareStatement(getChatIdQuery)
            getChatIdStatement.setString(1, username)
            val chatIdResultSet = getChatIdStatement.executeQuery()
            if (chatIdResultSet.next()) {
                chatId = chatIdResultSet.getLong(COLUMN_NAME_CHAT_ID)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error read chatId by username: $e")
        } finally {
            disconnectFromQuiziDb()
        }

        return chatId
    }

    fun readUsernameByChatId(chatId: Long): String {
        connectToQuziDb()

        var username: String = ""
        try {
            val getUsernameQuery = READ_USERNAME_BY_CHAT_ID
            val getUsernameStatement = connection.prepareStatement(getUsernameQuery)
            getUsernameStatement.setLong(1, chatId)
            val usernameResultSet = getUsernameStatement.executeQuery()
            if (usernameResultSet.next()) {
                username = usernameResultSet.getString(COLUMN_NAME_USERNAME)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error read chatId by username: $e")
        } finally {
            disconnectFromQuiziDb()
        }

        return username
    }

    fun readArrayOfTestId(chatId: Long = 0): List<String?> {
        connectToQuziDb()

        val existingTestIds = mutableListOf<String?>()
        try {
            // Считываем список существующих testId из базы данных
            val getTestIdsQuery = READ_ARRAY_OF_TEST_ID_BY_CHAT_ID
            val getTestIdsStatement = connection.prepareStatement(getTestIdsQuery)
            getTestIdsStatement.setLong(1, chatId)
            val testIdsResultSet = getTestIdsStatement.executeQuery()
            if (testIdsResultSet.next()) {
                val testIdsJson = testIdsResultSet.getString(COLUMN_NAME_ARRAY_OF_TEST_ID)
                existingTestIds.addAll(Gson().fromJson(testIdsJson, Array<String?>::class.java).toList())
            }

        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error inserting/updating user: $e")
        } finally {
            disconnectFromQuiziDb()
        }

        return existingTestIds
    }

    fun readArrayOfResultId(chatId: Long = 0): List<String?> {
        connectToQuziDb()

        val existingTestIds = mutableListOf<String?>()
        try {
            // Считываем список существующих testId из базы данных
            val readQuery = READ_ARRAY_OF_RESULTS_ID_BY_CHAT_ID
            val readStatement = connection.prepareStatement(readQuery)
            readStatement.setLong(1, chatId)

            val readResultSet = readStatement.executeQuery()
            if (readResultSet.next()) {
                val testIdsJson = readResultSet.getString(CharForQuiziDb.COLUMN_NAME_ARRAY_OF_RESULTS_ID)
                existingTestIds.addAll(Gson().fromJson(testIdsJson, Array<String?>::class.java).toList())
            }

        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error inserting/updating user: $e")
        } finally {
            disconnectFromQuiziDb()
        }

        return existingTestIds
    }
}