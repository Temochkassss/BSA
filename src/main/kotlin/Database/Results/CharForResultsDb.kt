package Database.Results

object CharForResultsDb {

    /***                           SQL-запросы и названия таблицы вопросов                  ***/

    const val TABLE_NAME = "resultsTestTable"
    const val CN_AUTHOR_CHAT_ID = "author_chat_id"
    const val CN_USER_CHAT_ID = "user_chat_id"
    const val CN_AUTHOR_USERNAME = "author_username"
    const val CN_USER_USERNAME = "user_username"
    const val CN_TEST_NAME = "test_name"
    const val CN_AUTHOR_TEST_ID = "author_test_id"
    const val CN_RESULT_TEST_ID = "result_test_id"
    const val CN_CHOOSED_INDEX = "choosed_index"
    const val CN_DATA = "data"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "results.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $CN_AUTHOR_CHAT_ID LONG,
            $CN_USER_CHAT_ID LONG,
            $CN_AUTHOR_USERNAME TEXT DEFAULT "SYSTEM",
            $CN_USER_USERNAME TEXT,
            $CN_TEST_NAME TEXT,
            $CN_AUTHOR_TEST_ID TEXT,
            $CN_RESULT_TEST_ID TEXT,
            $CN_CHOOSED_INDEX INTEGER,
            $CN_DATA LONG            
        )
    """

    const val INSERT_TO_TABLE = """
        INSERT INTO $TABLE_NAME (
            $CN_AUTHOR_CHAT_ID,
            $CN_USER_CHAT_ID,
            $CN_AUTHOR_USERNAME,
            $CN_USER_USERNAME,
            $CN_TEST_NAME,
            $CN_AUTHOR_TEST_ID,
            $CN_RESULT_TEST_ID,
            $CN_CHOOSED_INDEX,
            $CN_DATA       
        ) VALUES (?, ?, COALESCE(?, 'SYSTEM'), ?, ?, ?, ?, ?, ?)
    """

    const val COUNT_RESULT_ID = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $CN_RESULT_TEST_ID = ?"
    const val DELETE_RESULTS = "DELETE FROM $TABLE_NAME WHERE $CN_AUTHOR_TEST_ID = ?"

    const val COUNT_ORIGINAL_RESULT_ID_BY_AUTHOR_TEST_ID = """
        SELECT COUNT(DISTINCT $CN_RESULT_TEST_ID) 
        FROM $TABLE_NAME
        WHERE $CN_AUTHOR_TEST_ID = ?
    """

    const val GET_ALL_ATTEMPTS = """
        SELECT DISTINCT $CN_RESULT_TEST_ID, $CN_AUTHOR_TEST_ID, $CN_DATA
        FROM $TABLE_NAME
        WHERE $CN_AUTHOR_TEST_ID = ?
        ORDER BY $CN_DATA ASC
    """

    const val GET_DELETE_INFO = """
        SELECT $CN_USER_CHAT_ID, $CN_RESULT_TEST_ID
        FROM $TABLE_NAME
        WHERE $CN_AUTHOR_TEST_ID = ?
    """

    const val GET_RESULT_INFO = """
        SELECT $CN_AUTHOR_TEST_ID, $CN_DATA 
        FROM $TABLE_NAME 
        WHERE $CN_RESULT_TEST_ID = ? 
        LIMIT 1
    """
    const val GET_CHOOSED_INDEXES = """
        SELECT $CN_CHOOSED_INDEX
        FROM $TABLE_NAME 
        WHERE $CN_RESULT_TEST_ID = ?
    """
    const val GET_PERSON_INFO = """
        SELECT $CN_AUTHOR_USERNAME, $CN_USER_USERNAME 
        FROM $TABLE_NAME 
        WHERE $CN_RESULT_TEST_ID = ? 
        LIMIT 1
    """
}