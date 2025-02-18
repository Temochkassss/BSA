package Database.Quizi

object CharForQuiziDb { // назначаем константы - названия, столбцы, шаблоны

    /***                           SQL-запросы и названия таблицы                  ***/

    const val TABLE_NAME = "quiziTestTable"

    const val COLLUMN_NAME_ID = "id"
    const val COLUMN_NAME_CHAT_ID = "chatId"
    const val COLUMN_NAME_USERNAME = "username"
    const val COLUMN_NAME_USER = "name"
    const val COLUMN_NAME_LANGUAGE = "language"
    const val COLUMN_NAME_IS_PREMIUM = "isPremium"
    const val COLUMN_NAME_IS_BOT = "isBot"
    const val COLUMN_NAME_ARRAY_OF_TEST_ID = "arrayOfTestId"
    const val COLUMN_NAME_ARRAY_OF_RESULTS_ID = "arrayOfResultsId"
    const val COLUMN_NAME_RATING = "rating"

    const val DATABASE_VERSION = 7
    const val DATABASE_NAME = "C:\\Users\\priia\\Downloads\\SpringDemoBot0Java\\BSA\\data\\quizi.db"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLLUMN_NAME_ID INTEGER PRIMARY KEY," +
            "$COLUMN_NAME_CHAT_ID LONG," +
            "$COLUMN_NAME_USERNAME TEXT," +
            "$COLUMN_NAME_USER TEXT," +
            "$COLUMN_NAME_LANGUAGE TEXT," +
            "$COLUMN_NAME_IS_PREMIUM BOOLEAN," +
            "$COLUMN_NAME_IS_BOT BOOLEAN," +
            "$COLUMN_NAME_ARRAY_OF_TEST_ID TEXT," +
            "$COLUMN_NAME_ARRAY_OF_RESULTS_ID TEXT," +
            "$COLUMN_NAME_RATING INTEGER" +
            ")"

    // запрос использует параметризованные значения ? для полей name и age, чтобы избежать SQL-инъекций (вредоносная вставка в запрос)
    const val INSERT_TO_TABLE = "INSERT INTO $TABLE_NAME " +
            "($COLUMN_NAME_CHAT_ID," +
            " $COLUMN_NAME_USERNAME," +
            " $COLUMN_NAME_USER," +
            " $COLUMN_NAME_LANGUAGE," +
            " $COLUMN_NAME_IS_PREMIUM," +
            " $COLUMN_NAME_IS_BOT," +
            " $COLUMN_NAME_ARRAY_OF_TEST_ID," +
            " $COLUMN_NAME_ARRAY_OF_RESULTS_ID," +
            " $COLUMN_NAME_RATING)" +
            " VALUES (?, ?, ?, ?, ?, ?, COALESCE(?, '[]'), COALESCE(?, '[]'), ?)"


    const val READ_FROM_TABLE = "SELECT * FROM $TABLE_NAME" //  для выборки всех (*) столбцов из таблицы

    // Посчитай, сколько записей есть в таблице $TABLE_NAME, где значение в столбце $COLUMN_NAME_CHAT_ID равно тому значению, которое мы передадим вместо ?
    const val COUNT_CHAT_ID = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_NAME_CHAT_ID = ?"

    const val READ_ARRAY_OF_TEST_ID_BY_CHAT_ID = "SELECT $COLUMN_NAME_ARRAY_OF_TEST_ID FROM $TABLE_NAME WHERE $COLUMN_NAME_CHAT_ID = ?"
    const val READ_ARRAY_OF_RESULTS_ID_BY_CHAT_ID = "SELECT $COLUMN_NAME_ARRAY_OF_RESULTS_ID FROM $TABLE_NAME WHERE $COLUMN_NAME_CHAT_ID = ?"
    const val READ_CHAT_ID_BY_USERNAME = "SELECT $COLUMN_NAME_CHAT_ID FROM $TABLE_NAME WHERE $COLUMN_NAME_USERNAME = ?"
    const val READ_USERNAME_BY_CHAT_ID = "SELECT $COLUMN_NAME_USERNAME FROM $TABLE_NAME WHERE $COLUMN_NAME_CHAT_ID = ?"

    const val UPDATE_TEST_ID_IN_TABLE = "UPDATE $TABLE_NAME SET $COLUMN_NAME_ARRAY_OF_TEST_ID = ? WHERE $COLUMN_NAME_CHAT_ID = ?"
    const val UPDATE_RESULTS_ID_IN_TABLE = "UPDATE $TABLE_NAME SET $COLUMN_NAME_ARRAY_OF_RESULTS_ID = ? WHERE $COLUMN_NAME_CHAT_ID = ?"
    const val UPDATE_RATING = "UPDATE $TABLE_NAME SET $COLUMN_NAME_RATING = ? WHERE $COLUMN_NAME_CHAT_ID = ?"

}