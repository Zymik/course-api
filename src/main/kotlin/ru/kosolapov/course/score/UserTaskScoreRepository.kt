package ru.kosolapov.course.score

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import ru.kosolapov.course.table.TaskTable
import ru.kosolapov.course.table.UserTaskScoreTable
import ru.kosolapov.course.task.exception.InvalidTaskScoreException


interface UserTaskScoreRepository {
    suspend fun setScore(username: String, score: Double, taskId: Long)
    suspend fun getScore(username: String, taskId: Long): Double?
}

class UserTaskScoreRepositoryDatabase: UserTaskScoreRepository {
    private fun filterByPrimaryKey(username: String, taskId: Long) =
        (UserTaskScoreTable.taskId eq taskId) and (UserTaskScoreTable.username eq username)

    override suspend fun setScore(username: String, score: Double, taskId: Long) =
        newSuspendedTransaction(Dispatchers.IO) {
            val maxScore = TaskTable.slice(TaskTable.score)
                .select(TaskTable.id eq taskId)
                .map { it[TaskTable.score] }
                .single()

            if (score < 0 || score > maxScore) {
                throw InvalidTaskScoreException(score, maxScore, taskId)
            }

            val count = UserTaskScoreTable.select(
                (UserTaskScoreTable.taskId eq taskId) and
                        (UserTaskScoreTable.username eq username)
            ).count()

            if (count == 0L) {
                UserTaskScoreTable.insert {
                    it[UserTaskScoreTable.username] = username
                    it[UserTaskScoreTable.taskId] = taskId
                    it[UserTaskScoreTable.score] = score
                }
                return@newSuspendedTransaction
            }

            UserTaskScoreTable.update({ filterByPrimaryKey(username, taskId) }) {
                it[UserTaskScoreTable.score] = score
            }
        }

    override suspend fun getScore(username: String, taskId: Long): Double? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTaskScoreTable.slice(UserTaskScoreTable.score)
                .select { filterByPrimaryKey(username, taskId) }
                .map { it[UserTaskScoreTable.score] }
                .singleOrNull()
        }
}