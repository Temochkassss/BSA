package Database.Question

import com.google.gson.Gson
import java.sql.*


class DatabaseQuestionHelper(
    private val dbName: String
)
{
    private lateinit var connection: Connection

    fun connectToQuestionDb() {
        try {
            val url = "jdbc:sqlite:$dbName"
            connection = DriverManager.getConnection(url)
            println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) connection succeed ...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error connecting to database: $e")
        }
    }

    fun disconnectFromQuestionDb() {
        try {
            if (!connection.isClosed) {
                connection.close()
                println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) diconnection succeed...")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error closing database connection: $e")
        }
    }

    fun createQuestionTable() {
        val createTableQuery = CharForQuestionDb.CREATE_TABLE
        try {
            connection.createStatement().execute(createTableQuery) // execute - выполнять
            println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) create table succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error creating table: $e")
        }
    }

    fun insertQuestionToQuestionDb(
        testID: String,
        question: String,
        arrayOfAnswers: List<String?>,
        indexOfRightAnswer: Int,
        completionPercent: Double = 0.0,
        imageUrl: String = "",
        testName: String = "",
        numUpdate: Int = 0,
    ) {
        connectToQuestionDb()
        try {
            val insertQuery = CharForQuestionDb.INSERT_TO_TABLE
            val insertStatement = connection.prepareStatement(insertQuery) // подготовленное выражение на основе SQL-запроса
            val answersJson = Gson().toJson(arrayOfAnswers)
            insertStatement.apply {
                setString(1, imageUrl)
                setString(2, testName)
                setString(3, testID)
                setString(4, question)
                setString(5, answersJson) // преобразованный массив в JSON-строку
                setInt(6, indexOfRightAnswer)
                setDouble(7, completionPercent)
                setInt(8, numUpdate)
            }
            insertStatement.executeUpdate()
            println("\uD83E\uDDE0 ${CharForQuestionDb.TABLE_NAME} insert question succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error inserting question: $e")
        } finally {
            connection.close()
        }
    }

    data class Question( // класс данных представяющий стркутуру вопроса
        val id: Int,
        val testID: String,
        val questionText: String,
        val listOfAnswers: List<String>,
        val indexOfRightAnswer: Int,
        val completionPercent: Double,
        val testName: String? = "",
        val imageUrl: String? = "",
        val numUpdate: Int = 0,
        val fileId: String = "",
        val targetUsername: String = "",
        val targetNumAttempts: Int = 1000,
    )

    private fun extractQuestionFromResultSet(resultSet: ResultSet): Question {
        val id = resultSet.getInt(CharForQuestionDb.COLUMN_NAME_ID)
        val testID = resultSet.getString(CharForQuestionDb.COLUMN_NAME_TEST_ID)
        val question = resultSet.getString(CharForQuestionDb.COLUMN_NAME_QUESTION)
        val answersJson = resultSet.getString(CharForQuestionDb.COLUMN_NAME_ARRAY_OF_ANSWERS)
        val arrayOfAnswers = Gson().fromJson(answersJson, Array<String>::class.java).toList()
        val indexOfRightAnswer = resultSet.getInt(CharForQuestionDb.COLUMN_NAME_INDEX_OF_CORRECT_ANSWER)
        val completionPercent = resultSet.getDouble(CharForQuestionDb.COLUMN_NAME_COMPLETION_PERCENT)
        val testName = resultSet.getString(CharForQuestionDb.COLUMN_NAME_TEST_NAME)
        val numUpdate = resultSet.getInt(CharForQuestionDb.COLUMN_NAME_NUMBER_OF_UPDATE)
        val imageUrl = resultSet.getString(CharForQuestionDb.COLUMN_NAME_IMAGE_URL)
        return Question(id, testID, question, arrayOfAnswers, indexOfRightAnswer, completionPercent, testName, imageUrl, numUpdate) // объект с полученными значениями
    }
   fun readQuestionFromQuestionDb(testId: String? = null) : List<Question> {
        val questions = mutableListOf<Question>() // пустой изменяемый список
        connectToQuestionDb()

        try {
            val selectQuery = buildString {
                append("SELECT * FROM ${CharForQuestionDb.TABLE_NAME}") // sql-запрос для чтения данных из таблицы вопросов
                if (testId != null) {
                    append(" WHERE ${CharForQuestionDb.COLUMN_NAME_TEST_ID} = ?")
                }
            }

            val statement = if (testId != null) { // statement - заявление/объект для выполнения запроса
                connection.prepareStatement(selectQuery).apply {
                    setString(1, testId)
                }
            } else {
                connection.createStatement()
            }

            val resultSet = when (statement) { // выполнение sql-запроса с помощью executeQuery()
                is PreparedStatement -> statement.executeQuery()
                is Statement -> statement.executeQuery(selectQuery)
                else -> throw IllegalStateException("Unexpected statement type: ${statement::class.java}")
            }

            // перебор строк результата запроса
            while (resultSet.next()) { // пока есть следующая строка (resultSet.next() возвращает true), выполняется тело цикла
                questions.add(extractQuestionFromResultSet(resultSet))
            }

            resultSet.close()
            statement.close()
            println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) read from db succeed...")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error reading from database: $e")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Unexpected error: $e")
        } finally {
            connection.close()
        }

        return questions
    }

    fun updateTestName(testName: String? = "", testId: String? = "") {
        connectToQuestionDb()
        val updateQuery = CharForQuestionDb.INSERT_TEST_NAME
        val updateStatement = connection.prepareStatement(updateQuery)
        updateStatement.apply {// Подставляем переменные в запрос
            setString(1, testName)
            setString(2, testId)
        }
        val updatedRows = updateStatement.executeUpdate()
        println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) update testName succeed, updated $updatedRows rows")
        disconnectFromQuestionDb()
    }

    fun updateCompletionPercent(newPercent: Double, testId: String) {
        connectToQuestionDb()
        try {
            val updateComPercentQuery= CharForQuestionDb.UPDATE_COMPLETION_PERCENT
            val updateComPercentStatement = connection.prepareStatement(updateComPercentQuery)
            updateComPercentStatement.apply {
                setDouble(1, newPercent)
                setString(2, testId)
            }
            val updatedRows = updateComPercentStatement.executeUpdate()
            println("🧠 (${CharForQuestionDb.TABLE_NAME}) update completionPercent succeed, updated $updatedRows rows")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error updateCompletionPercent: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun deleteTest(testId: String) {
        connectToQuestionDb()

        try {
            val deleteTestIdQuery = CharForQuestionDb.DELETE_TEST_ID
            val deleteTestIdStatement = connection.prepareStatement(deleteTestIdQuery)
            deleteTestIdStatement.setString(1, testId)
            deleteTestIdStatement.executeUpdate()
            println("TestId was delete succesed")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error deleting test: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }


    fun readTestName(testId: String): String? {
        connectToQuestionDb()

        var testName = ""
        try {
            val query = CharForQuestionDb.READ_TEST_NAME
            val statement = connection.prepareStatement(query)
            statement.setString(1, testId) // Подставляем переданный testId в запрос
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                testName = resultSet.getString(CharForQuestionDb.COLUMN_NAME_TEST_NAME)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error readTestName: $e")
        } finally {
            disconnectFromQuestionDb()
        }

        return testName
    }

    fun readImageUrl(testId: String): String? {
        connectToQuestionDb()

        var imageUrl = ""
        try {
            val query = CharForQuestionDb.READ_IMAGE_URL
            val statement = connection.prepareStatement(query)
            statement.setString(1, testId)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                imageUrl = resultSet.getString(CharForQuestionDb.COLUMN_NAME_IMAGE_URL)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error readImageUrl: $e")
        } finally {
            disconnectFromQuestionDb()
        }

        return imageUrl
    }

    fun readComPercent(testId: String): Double {
        connectToQuestionDb()
        var comPercent: Double = 0.0
        try {
            val query = CharForQuestionDb.READ_COMPLETION_PERCENT
            val statement = connection.prepareStatement(query)
            statement.setString(1, testId) // Подставляем переданный testId в запрос
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                comPercent = resultSet.getDouble(CharForQuestionDb.COLUMN_NAME_COMPLETION_PERCENT)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error rearCompPerccent: $e")
        } finally {
            disconnectFromQuestionDb()
        }
        return comPercent
    }

    fun countOriginalSystemTestIds(): Int {
        var count = 0

        try {
            connectToQuestionDb()
            val query = CharForQuestionDb.COUNT_ORIGINAL_SYSTEM_TEST_ID
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)

            if (resultSet.next()) {
                count = resultSet.getInt(1)
            }

            statement?.close()
            resultSet?.close()

        } catch (e: SQLException) {
            e.printStackTrace()
            throw e // Бросаем исключение, чтобы оно могло быть обработано выше по цепочке
        } finally {
            disconnectFromQuestionDb()
        }

        println("🧠 Количество оригинальных системных тестов: $count")
        return count
    }

    fun countOriginalPublicTestIds(): Int {
        var count = 0

        try {
            connectToQuestionDb()
            val query = CharForQuestionDb.COUNT_ORIGINAL_PUBLIC_TEST_ID
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)

            if (resultSet.next()) {
                count = resultSet.getInt(1)
            }

            statement?.close()
            resultSet?.close()

        } catch (e: SQLException) {
            e.printStackTrace()
            throw e // Бросаем исключение, чтобы оно могло быть обработано выше по цепочке
        } finally {
            disconnectFromQuestionDb()
        }

        println("🧠 Количество оригинальных публичных тестов: $count")
        return count
    }

    fun updateImageUrlFromSystemTest(targetTestId: String, systemTestId: String) {
        connectToQuestionDb()
        try {
            val updateQuery = CharForQuestionDb.UPDATE_IMAGE_URL

            val statement = connection.prepareStatement(updateQuery)
            statement.apply {
                setString(1, systemTestId) // Подставляем systemTestId в подзапрос
                setString(2, targetTestId) // Подставляем targetTestId в основной запрос
            }

            val updatedRows = statement.executeUpdate()
            println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) update image_url succeed, updated $updatedRows rows")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error updating image_url: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun saveFileId(testId: String, fileId: String) {
        connectToQuestionDb()
        try {
            val saveQuery = CharForQuestionDb.SAVE_PHOTO
            val statement = connection.prepareStatement(saveQuery).apply {
                setString(1, fileId)
                setString(2, testId)
            }
            val rowsAffected = statement.executeUpdate()
            if (rowsAffected > 0) {
                println("Фото успешно сохранено в базу данных.")
            } else {
                println("Не удалось сохранить фото в базу данных.")
            }        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error savePhotoToDatabase: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun saveTagretUsername(testId: String, targetUsername: String) {
        connectToQuestionDb()
        try {
            val saveQuery = CharForQuestionDb.SAVE_TARGET_USERNAME
            val statement = connection.prepareStatement(saveQuery).apply {
                setString(1, targetUsername)
                setString(2, testId)
            }
            val rowsAffected = statement.executeUpdate()
            if (rowsAffected > 0) {
                println("targetUsername сохранено в базу данных.")
            } else {
                println("Не удалось сохранить targetUsername в базу данных.")
            }        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error saveTagretUsername: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun saveTagretComPercent(testId: String, targetComPercent: Int) {
        connectToQuestionDb()
        try {
            val saveQuery = CharForQuestionDb.SAVE_TARGET_COM_PERCENT
            val statement = connection.prepareStatement(saveQuery).apply {
                setInt(1, targetComPercent)
                setString(2, testId)
            }
            val rowsAffected = statement.executeUpdate()
            if (rowsAffected > 0) {
                println("targetComPercent сохранено в базу данных.")
            } else {
                println("Не удалось сохранить targetComPercent в базу данных.")
            }        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error saveTagretComPercent: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun saveTagretNumAttempts(testId: String, targetNumAttempts: Int) {
        connectToQuestionDb()
        try {
            val saveQuery = CharForQuestionDb.SAVE_TARGET_NUM_ATTEMPTS
            val statement = connection.prepareStatement(saveQuery).apply {
                setInt(1, targetNumAttempts)
                setString(2, testId)
            }
            val rowsAffected = statement.executeUpdate()
            if (rowsAffected > 0) {
                println("targetNumAttempts сохранено в базу данных.")
            } else {
                println("Не удалось сохранить targetNumAttempts в базу данных.")
            }        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error saveTagretNumAttempts: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }

    fun getPhotoFileId(testId: String): String? {
        connectToQuestionDb()
        var fileId: String? = null
        try {
            val query = CharForQuestionDb.GET_FILE_ID
            val statement = connection.prepareStatement(query).apply {
                setString(1, testId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                fileId = resultSet.getString(CharForQuestionDb.COLUMN_NAME_FILE_ID)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error getPhotoFileId: $e")
        } finally {
            disconnectFromQuestionDb()
        }
        return fileId
    }

    fun getTargetComPercent(testId: String): Int? {
        connectToQuestionDb()
        var threshold: Int? = null
        try {
            val query = CharForQuestionDb.GET_TAGRET_COM_PERCENT
            val statement = connection.prepareStatement(query).apply {
                setString(1, testId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                threshold = resultSet.getInt(CharForQuestionDb.COLUMN_NAME_TAGRET_COM_PERCENT)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error getTargetComPercent: $e")
        } finally {
            disconnectFromQuestionDb()
        }
        return threshold
    }

    fun getTargetUsername(testId: String): String? {
        connectToQuestionDb()
        var username: String? = null
        try {
            val query = CharForQuestionDb.GET_TARGET_USERNAME
            val statement = connection.prepareStatement(query).apply {
                setString(1, testId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                username = resultSet.getString(CharForQuestionDb.COLUMN_NAME_TAGRET_USERNAME)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error getTargetUsername: $e")
        } finally {
            disconnectFromQuestionDb()
        }
        return username
    }

    fun getAttemptsCount(testId: String): Int {
        connectToQuestionDb()
        var attempts: Int? = null
        try {
            val query = CharForQuestionDb.GET_ATTEMPTS_COUNT
            val statement = connection.prepareStatement(query).apply {
                setString(1, testId)
            }
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                attempts = resultSet.getInt(CharForQuestionDb.COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error getAttemptsCount: $e")
        } finally {
            disconnectFromQuestionDb()
        }
        if (attempts == null || attempts == 0) return Int.MAX_VALUE
        return attempts
    }

    fun saveTestPepperSettings(
        testId: String,
        tagretComPercent: Int,
        targetUsername: String,
        targetNumAttempts: Int,
        photoFileId: String
    ) {
        connectToQuestionDb()
        try {
            val saveQuery = CharForQuestionDb.SAVE_PEPPER_TEST_INFO
            val updateStatement = connection.prepareStatement(saveQuery).apply {
                setString(1, photoFileId)
                setString(2, targetUsername)
                setInt(3, tagretComPercent)
                setInt(4, targetNumAttempts)
                setString(5, testId)
            }
            val updatedRows = updateStatement.executeUpdate()
            println("\uD83E\uDDE0 (${CharForQuestionDb.TABLE_NAME}) update TestPepperSettings succeed, updated $updatedRows rows")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Error saveTestPepperSettings: $e")
        } finally {
            disconnectFromQuestionDb()
        }
    }


}