package ru.kosolapov.course.task.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.kosolapov.course.table.LessonTable
import ru.kosolapov.course.table.TaskTable
import ru.kosolapov.course.task.Task
import ru.kosolapov.course.task.exception.NoSuchTaskException
import java.sql.Connection

class TaskRepositoryDatabase : TaskRepository {

    companion object {
        private fun ResultRow.getTask(): Task =
            Task(
                this[TaskTable.id].value,
                this[TaskTable.lessonId],
                this[TaskTable.number],
                this[TaskTable.name],
                this[TaskTable.description],
                this[TaskTable.score]
            )

    }

    override suspend fun getTask(id: Long): Task? =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.select { TaskTable.id eq id }
                .map { it.getTask() }
                .singleOrNull()
        }

    override suspend fun existTask(id: Long): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.select { TaskTable.id eq id }
                .count() > 0
        }

    override suspend fun getScore(id: Long): Double? =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.slice(TaskTable.score)
                .select { TaskTable.id eq id }
                .map { it[TaskTable.score] }
                .singleOrNull()
        }

    override suspend fun addTask(lessonId: Long, number: Int, name: String, description: String, score: Double): Long =
        newSuspendedTransaction(
            Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        ) {
            val maxNumber = getMaxNumber(lessonId) ?: -1
            val n = number.coerceAtMost(maxNumber + 1)

            val ordered = getWithGreaterOrEqualNumber(lessonId, n, order = SortOrder.DESC)

            for (id in ordered) {
                TaskTable.update({
                    TaskTable.id eq id
                }) {
                    with(SqlExpressionBuilder) {
                        it.update(TaskTable.number, TaskTable.number + 1)
                    }
                }
            }

            val id = insert(lessonId, n, name, description, score)

            id
        }

    private fun getWithGreaterOrEqualNumber(lessonId: Long, n: Int, order: SortOrder) =
        TaskTable.slice(TaskTable.id).select {
            (TaskTable.lessonId eq lessonId) and (TaskTable.number greaterEq n)
        }.orderBy(TaskTable.number, order = order).map { it[TaskTable.id].value }

    override suspend fun addLastTask(lessonId: Long, name: String, description: String, score: Double): Long =
        newSuspendedTransaction(
            Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        ) {
            val number = (getMaxNumber(lessonId) ?: -1) + 1

            val id = insert(lessonId, number, name, description, score)

            id
        }

    override suspend fun getTasksByLessonId(lessonId: Long): List<Task> =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.select(TaskTable.lessonId eq lessonId)
                .map { it.getTask() }
        }

    override suspend fun getTaskIdsByLessonId(lessonId: Long): List<Long> =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.slice(TaskTable.id)
                .select { TaskTable.lessonId eq lessonId }
                .map { it[TaskTable.id].value }
        }

    override suspend fun getLessonId(taskId: Long): Long? =
        newSuspendedTransaction(Dispatchers.IO) {
            TaskTable.slice(TaskTable.lessonId)
                .select { TaskTable.id eq taskId }
                .map { it[TaskTable.lessonId] }
                .singleOrNull()
        }

    override suspend fun removeTask(taskId: Long) = newSuspendedTransaction(
        Dispatchers.IO,
        transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
    ) {
        val (n, courseId) = TaskTable.slice(TaskTable.number, TaskTable.lessonId)
            .select(TaskTable.id eq taskId)
            .map { Pair(it[TaskTable.number], it[TaskTable.lessonId]) }
            .singleOrNull() ?: throw NoSuchTaskException(taskId)

        TaskTable.deleteWhere { id eq taskId }

        val ordered = getWithGreaterOrEqualNumber(courseId, n, SortOrder.ASC)

        for (id in ordered) {
            TaskTable.update({
                TaskTable.id eq id
            }) {
                with(SqlExpressionBuilder) {
                    it.update(number, number - 1)
                }
            }
        }

        Unit
    }

    private fun getMaxNumber(lessonId: Long): Int? {
        val max = TaskTable.number.max()
        return TaskTable.slice(max)
            .select { TaskTable.lessonId eq lessonId }
            .singleOrNull()
            ?.get(max)
    }

    private fun insert(
        lessonId: Long,
        number: Int,
        name: String,
        description: String,
        score: Double
    ) = TaskTable.insertAndGetId {
        it[TaskTable.lessonId] = lessonId
        it[TaskTable.number] = number
        it[TaskTable.name] = name
        it[TaskTable.description] = description
        it[TaskTable.score] = score
    }.value


}
