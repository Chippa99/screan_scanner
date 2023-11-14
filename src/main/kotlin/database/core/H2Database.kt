package database.core

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement


class H2Database(dbName: String, user: String, password: String) : AutoCloseable {
    private val conn: Connection
    private val stmt: Statement

    init {
        conn = DriverManager.getConnection("jdbc:h2:~/$dbName", user, password)
        stmt = conn.createStatement()
    }

    init {
        stmt.execute(
            """
            CREATE TABLE IF NOT EXISTS statistic (
                id INT AUTO_INCREMENT PRIMARY KEY,
                creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
                object_name VARCHAR(255) UNIQUE,
                count INT DEFAULT 0
            );
            CREATE INDEX IF NOT EXISTS idx_creation_datetime ON statistic (creation_datetime);
            """.trimIndent()
        )
    }

    override fun close() {
        conn.close()
        stmt.close()
    }

    fun getObjectsPeerToday(): List<StatisticObject> {
        try {
            val res = stmt.executeQuery("""
                SELECT * FROM statistic WHERE CAST(creation_datetime AS DATE) >= DATEADD('DAY', -1, CURRENT_DATE);
            """.trimIndent()
            )
            val resultList: MutableList<StatisticObject> = mutableListOf()
            while (res.next()) {
                resultList.add(
                    StatisticObject(
                        res.getInt("id"),
                        res.getString("object_name"),
                        res.getTimestamp("creation_datetime"),
                        res.getInt("count")
                    )
                )
            }
            return resultList
        } catch (e: Exception) {
            println("____ERROR GETTING: $e")
            return emptyList()
        }
    }

    fun deleteAllDataForLastWeek() {
        try {
            stmt.execute("""
                    DELETE FROM statistic WHERE creation_datetime <= DATEADD('WEEK', -1, CURRENT_TIMESTAMP);
            """.trimIndent())
            println("Old objects has been deleted")
        } catch (e: Exception) {
            println("____ERROR DELETING: $e")
        }
    }

    fun addObject(name: String) {
        val trimmedName = name.trim().replace("'", "")
        try {
            stmt.execute("""
                INSERT INTO statistic (object_name) VALUES ('$trimmedName')
            """.trimIndent())
        } catch (uni: JdbcSQLIntegrityConstraintViolationException) {
            try {
                stmt.execute("""
                    UPDATE statistic SET count = count + 1 WHERE object_name = '$trimmedName'
                """.trimIndent())
            } catch (err: Exception) {
                println("____ERROR UPDATING: $err")
            }
        } catch (e: Exception) {
            println("____ERROR INSERTING: $e")
        }
    }
}