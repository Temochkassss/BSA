package Database.Results

import Database.Quizi.DatabaseQuiziHelper
import Database.Results.CharForResultsDb.CN_AUTHOR_TEST_ID
import Database.Results.CharForResultsDb.CN_AUTHOR_USERNAME
import Database.Results.CharForResultsDb.CN_CHOOSED_INDEX
import Database.Results.CharForResultsDb.CN_DATA
import Database.Results.CharForResultsDb.CN_RESULT_TEST_ID
import Database.Results.CharForResultsDb.CN_USER_CHAT_ID
import Database.Results.CharForResultsDb.CN_USER_USERNAME
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseResultsHelper(
    private val dbName: String
) {
    private lateinit var connection: Connection

    fun connectToResultsDb(){
        try {
            val url = "jdbc:sqlite:$dbName"
            connection = DriverManager.getConnection(url)
            println("\uD83D\uDCCA (${CharForResultsDb.TABLE_NAME}) connection succeed ...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error connecting to database: $e")
        }
    }

    fun disconnectFromResultsDb() {
        try {
            if (!connection.isClosed) {
                connection.close()
                println("\uD83D\uDCCA (${CharForResultsDb.TABLE_NAME}) diconnection succeed...")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error closing database connection: $e")
        }
    }

    fun createResultsDb() {
        val createTableQuery = CharForResultsDb.CREATE_TABLE

        try {
            connection.createStatement().execute(createTableQuery) // execute - выполнять
            println("\uD83D\uDCCA (${CharForResultsDb.TABLE_NAME}) create table succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error creating table: $e")
        }
    }

    fun insertResults(
        authorChatId: Long,
        userChatId: Long,
        authorUsername: String? = "",
        userUsername: String? = "",
        testName: String? = "Без названия",
        authorTestId: String,
        resultsTestId: String,
        choosedIndex: Int,
        timestamp: Long,
        dbQuiziHelper: DatabaseQuiziHelper
    ) {
        connectToResultsDb()

        try {
            val query = CharForResultsDb.INSERT_TO_TABLE
            val statement = connection.prepareStatement(query)
            statement.apply {
                setLong(1, authorChatId)
                setLong(2, userChatId)
                setString(3, authorUsername)
                setString(4, userUsername)
                setString(5, testName)
                setString(6, authorTestId)
                setString(7, resultsTestId)
                setInt(8, choosedIndex)
                setLong(9, timestamp)
            }
            statement.executeUpdate()
            println("\uD83D\uDCCA (${CharForResultsDb.TABLE_NAME}) insert results succeed...")

            // Обновление списка resultId
            dbQuiziHelper.updateArrayOfResultsId(userChatId, resultsTestId)

        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error insertResults: $e")
        } finally {
            disconnectFromResultsDb()
        }
    }

    // Считаем resultId для проверки на уникальность
    fun countResultId(resultId: String): Int {
        connectToResultsDb()

        var count: Int = 0
        try {
            val query = CharForResultsDb.COUNT_RESULT_ID
            val statement = connection.prepareStatement(query).apply {
                setString(1, resultId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                count = resultSet.getInt(1)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error countResultId: $e")
        } finally {
            disconnectFromResultsDb()
        }

        return count
    }

    // Считаем resultId для получения попыток
    fun countOriginalResultId(authorTestId: String): Int {
        connectToResultsDb()

        var count: Int = 0
        try {
            val query = CharForResultsDb.COUNT_ORIGINAL_RESULT_ID_BY_AUTHOR_TEST_ID
            val statement = connection.prepareStatement(query).apply {
                setString(1, authorTestId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                count = resultSet.getInt(1)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error countOriginalResultId: $e")
        } finally {
            disconnectFromResultsDb()
        }

        return count
    }

    // Класс для хранения информации о попытке
    data class Attempt(
        val resultId: String,
        val authorTestId: String,
        val timestamp: Long
    )

    // Функция для получения всех попыток для конкретного теста
    fun getAllAttemptsForTest(authorTestId: String): List<Attempt> {
        connectToResultsDb() // Подключаемся к базе данных
        val attempts = mutableListOf<Attempt>() // Список для хранения попыток
        try {
            val query = CharForResultsDb.GET_ALL_ATTEMPTS

            val statement = connection.prepareStatement(query).apply {
                setString(1, authorTestId) // Подставляем authorTestId в запрос
            }
            val resultSet = statement.executeQuery() // Выполняем запрос

            // Обрабатываем результат
            while (resultSet.next()) {
                val resultId = resultSet.getString(CN_RESULT_TEST_ID)
                val timestamp = resultSet.getLong(CN_DATA)
                attempts.add(Attempt(resultId, authorTestId, timestamp)) // Добавляем попытку в список
            }
        } catch (e: SQLException) {
            e.printStackTrace() // Обрабатываем ошибки
        } finally {
            disconnectFromResultsDb() // Отключаемся от базы данных
        }
        return attempts // Возвращаем список попыток
    }

    // Получаем основную информацию о результате по его resultId (для кнопки)
    fun getResultInfo(resultId: String): Pair<String, Long>? {
        connectToResultsDb()
        var result: Pair<String, Long>? = null
        try {
            val query = CharForResultsDb.GET_RESULT_INFO
            val statement = connection.prepareStatement(query).apply {
                setString(1, resultId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                result = Pair(
                    resultSet.getString(CN_AUTHOR_TEST_ID),
                    resultSet.getLong(CN_DATA)  // Читаем как LONG
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            disconnectFromResultsDb()
        }
        return result
    }

    // Получаем все ответы пользователя для конкретного результата (для сообщения)
    fun getChoosedIndexes(resultId: String): List<Int> {
        connectToResultsDb()
        val choosedIndexes = mutableListOf<Int>()
        try {
            val query = CharForResultsDb.GET_CHOOSED_INDEXES
            val statement = connection.prepareStatement(query).apply {
                setString(1, resultId)
            }
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                choosedIndexes.add(resultSet.getInt(CN_CHOOSED_INDEX))
            }
        } finally {
            disconnectFromResultsDb()
        }
        return choosedIndexes
    }

    // Получаем информацию об авторе и пользователе
    fun getPersonInfo(resultId: String): Pair<String, String>? {
        connectToResultsDb()
        var persons: Pair<String, String>? = null
        try {
            val query = CharForResultsDb.GET_PERSON_INFO
            val statement = connection.prepareStatement(query).apply {
                setString(1, resultId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                persons = Pair(
                    resultSet.getString(CN_AUTHOR_USERNAME),
                    resultSet.getString(CN_USER_USERNAME)
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            disconnectFromResultsDb()
        }
        return persons
    }

    // Удаление данный о результатах по authorTestId
    fun deleteResults(authorTestId: String) {
        connectToResultsDb()
        try {
            val deleteQuery = CharForResultsDb.DELETE_RESULTS
            val deleteStatement = connection.prepareStatement(deleteQuery)
            deleteStatement.setString(1, authorTestId)
            deleteStatement.executeUpdate()

            println("Results by authorTestId = $authorTestId was delete succesed")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error deleting results: $e")
        } finally {
            disconnectFromResultsDb()
        }
    }

    fun getDeleteInfo(authorTestId: String): MutableList<Pair<Long, String>> {
        connectToResultsDb()
        val resultsToRemove = mutableListOf<Pair<Long, String>>()
        try {
            val query = CharForResultsDb.GET_DELETE_INFO
            val statement = connection.prepareStatement(query)
            statement.setString(1, authorTestId)

            val resultSet = statement.executeQuery()

            // Сохраняем результаты в список
            while (resultSet.next()) {
                val userChatId = resultSet.getLong(CN_USER_CHAT_ID)
                val resultTestId = resultSet.getString(CN_RESULT_TEST_ID)
                resultsToRemove.add(Pair(userChatId, resultTestId))
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error deleting results: $e")
        } finally {
            disconnectFromResultsDb()
        }

        return resultsToRemove
    }

}