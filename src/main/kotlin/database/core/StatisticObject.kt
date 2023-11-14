package database.core

import java.sql.Timestamp

data class StatisticObject(
    private val id: Int,
    private val name: String,
    private val datetime: Timestamp,
    private val count: Int
) {
    override fun toString(): String {
        return "{ id: $id name: $name datetime: $datetime count: $count } "
    }
}