package Database.Question

object CharForQuestionDb {

    /***                           SQL-запросы и названия таблицы вопросов                  ***/

    const val TABLE_NAME = "questionTestTable"
    const val COLUMN_NAME_ID = "id"
    const val COLUMN_NAME_IMAGE_URL = "image_url"
    const val COLUMN_NAME_TEST_NAME = "test_name"
    const val COLUMN_NAME_TEST_ID = "test_id"
    const val COLUMN_NAME_QUESTION = "question"
    const val COLUMN_NAME_ARRAY_OF_ANSWERS = "array_of_answers"
    const val COLUMN_NAME_INDEX_OF_CORRECT_ANSWER = "index_of_correct_answers"
    const val COLUMN_NAME_COMPLETION_PERCENT = "completion_percent"
    const val COLUMN_NAME_NUMBER_OF_UPDATE = "number_of_update"
    const val COLUMN_NAME_FILE_ID = "fileId"
    const val COLUMN_NAME_TAGRET_USERNAME = "target_username"
    const val COLUMN_NAME_TAGRET_COM_PERCENT = "target_com_percent"
    const val COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS = "target_number_of_attempts"

    const val DATABASE_VERSION = 4
    const val DATABASE_NAME = "question.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_NAME_ID INTEGER PRIMARY KEY,
            $COLUMN_NAME_IMAGE_URL TEXT DEFAULT "",
            $COLUMN_NAME_TEST_NAME TEXT DELAULT "",
            $COLUMN_NAME_TEST_ID TEXT,
            $COLUMN_NAME_QUESTION TEXT,
            $COLUMN_NAME_ARRAY_OF_ANSWERS TEXT,
            $COLUMN_NAME_INDEX_OF_CORRECT_ANSWER INTEGER,
            $COLUMN_NAME_COMPLETION_PERCENT DOUBLE,
            $COLUMN_NAME_NUMBER_OF_UPDATE INTEGER,
            $COLUMN_NAME_FILE_ID TEXT,
            $COLUMN_NAME_TAGRET_USERNAME TEXT,
            $COLUMN_NAME_TAGRET_COM_PERCENT INTEGER,
            $COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS INTEGER
        )
    """

    const val INSERT_TO_TABLE = """
        INSERT INTO $TABLE_NAME (
            $COLUMN_NAME_IMAGE_URL,
            $COLUMN_NAME_TEST_NAME,
            $COLUMN_NAME_TEST_ID,
            $COLUMN_NAME_QUESTION,
            $COLUMN_NAME_ARRAY_OF_ANSWERS,
            $COLUMN_NAME_INDEX_OF_CORRECT_ANSWER,
            $COLUMN_NAME_COMPLETION_PERCENT,
            $COLUMN_NAME_NUMBER_OF_UPDATE,
            $COLUMN_NAME_FILE_ID,
            $COLUMN_NAME_TAGRET_USERNAME,
            $COLUMN_NAME_TAGRET_COM_PERCENT,
            $COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    const val INSERT_TEST_NAME = """
        UPDATE $TABLE_NAME 
        SET $COLUMN_NAME_TEST_NAME = ? 
        WHERE $COLUMN_NAME_TEST_ID = ?
    """

    const val COUNT_TEST_ID = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_NAME_TEST_ID = ?"
    const val DELETE_TEST_ID = "DELETE FROM $TABLE_NAME WHERE $COLUMN_NAME_TEST_ID = ?"


    const val READ_TEST_NAME = "SELECT $COLUMN_NAME_TEST_NAME FROM $TABLE_NAME WHERE $COLUMN_NAME_TEST_ID = ?"
    const val READ_IMAGE_URL = "SELECT $COLUMN_NAME_IMAGE_URL FROM $TABLE_NAME WHERE $COLUMN_NAME_TEST_ID = ?"

    const val COUNT_ORIGINAL_SYSTEM_TEST_ID = """
        SELECT COUNT(DISTINCT $COLUMN_NAME_TEST_ID)
        FROM $TABLE_NAME
        WHERE LENGTH($COLUMN_NAME_TEST_ID) <= 3
        AND CAST($COLUMN_NAME_TEST_ID AS SIGNED) BETWEEN 1 AND 100
    """

    const val COUNT_ORIGINAL_PUBLIC_TEST_ID = """
        SELECT COUNT(DISTINCT $COLUMN_NAME_TEST_ID)
        FROM $TABLE_NAME
        WHERE LENGTH($COLUMN_NAME_TEST_ID) <= 3
        AND CAST($COLUMN_NAME_TEST_ID AS SIGNED) BETWEEN 101 AND 200
    """

    const val UPDATE_COMPLETION_PERCENT = """
        UPDATE $TABLE_NAME
        SET $COLUMN_NAME_COMPLETION_PERCENT = ($COLUMN_NAME_COMPLETION_PERCENT * $COLUMN_NAME_NUMBER_OF_UPDATE + ?) / ($COLUMN_NAME_NUMBER_OF_UPDATE + 1),
        $COLUMN_NAME_NUMBER_OF_UPDATE = $COLUMN_NAME_NUMBER_OF_UPDATE + 1
        WHERE $COLUMN_NAME_TEST_ID = ?
    """

    const val UPDATE_IMAGE_URL= """
            UPDATE $TABLE_NAME
            SET $COLUMN_NAME_IMAGE_URL = (
                SELECT $COLUMN_NAME_IMAGE_URL
                FROM $TABLE_NAME
                WHERE $COLUMN_NAME_TEST_ID = ? 
                AND CAST(${COLUMN_NAME_TEST_ID} AS SIGNED) BETWEEN 1 AND 100
                LIMIT 1
            )
            WHERE $COLUMN_NAME_TEST_ID = ?
        """

    const val READ_COMPLETION_PERCENT = """
        SELECT $COLUMN_NAME_COMPLETION_PERCENT FROM $TABLE_NAME WHERE $COLUMN_NAME_TEST_ID = ?
    """

    const val SAVE_PHOTO = """
        UPDATE $TABLE_NAME
        SET $COLUMN_NAME_FILE_ID = ?
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val SAVE_TARGET_USERNAME = """
        UPDATE $TABLE_NAME  
        SET $COLUMN_NAME_TAGRET_USERNAME = ?
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val SAVE_TARGET_COM_PERCENT = """
        UPDATE $TABLE_NAME  
        SET $COLUMN_NAME_TAGRET_COM_PERCENT = ?
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val SAVE_TARGET_NUM_ATTEMPTS = """
        UPDATE $TABLE_NAME  
        SET $COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS = ?
        WHERE $COLUMN_NAME_TEST_ID = ?
    """

    const val SAVE_PEPPER_TEST_INFO = """
        UPDATE $TABLE_NAME  
        SET $COLUMN_NAME_FILE_ID = ?,
        $COLUMN_NAME_TAGRET_USERNAME = ?,
        $COLUMN_NAME_TAGRET_COM_PERCENT = ?,
        $COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS = ?
        WHERE $COLUMN_NAME_TEST_ID = ?
    """

    const val GET_FILE_ID = """
        SELECT $COLUMN_NAME_FILE_ID
        FROM $TABLE_NAME
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val GET_TAGRET_COM_PERCENT = """
        SELECT $COLUMN_NAME_TAGRET_COM_PERCENT
        FROM $TABLE_NAME
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val GET_TARGET_USERNAME = """
        SELECT $COLUMN_NAME_TAGRET_USERNAME
        FROM $TABLE_NAME
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
    const val GET_ATTEMPTS_COUNT = """
        SELECT $COLUMN_NAME_TAGRET_NUMBER_OF_ATTEMPTS
        FROM $TABLE_NAME
        WHERE $COLUMN_NAME_TEST_ID = ?
    """
}