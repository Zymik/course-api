package ru.kosolapov.course.lesson.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.kosolapov.course.lesson.LessonDescription
import ru.kosolapov.course.lesson.NoSuchLessonException
import ru.kosolapov.course.table.LessonTable
import ru.kosolapov.course.table.TaskTable
import java.sql.Connection


class LessonRepositoryDatabase : LessonRepository {
    override suspend fun getLessonDescription(lessonId: Long): LessonDescription? =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable.select { LessonTable.id eq lessonId }
                .map {
                    getLessonDescription(it)
                }.singleOrNull()
        }

    override suspend fun getLessonDescription(courseId: Long, number: Int): LessonDescription? =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable.select { (LessonTable.courseId eq courseId) and (LessonTable.number eq number) }
                .map {
                    getLessonDescription(it)
                }.singleOrNull()
        }

    override suspend fun getLessonId(courseId: Long, number: Int): Long? =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable.slice(LessonTable.id)
                .select { (LessonTable.courseId eq courseId) and (LessonTable.number eq number) }
                .map { it[LessonTable.id].value }
                .singleOrNull()
        }

    override suspend fun getCourseId(lessonId: Long): Long? =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable.slice(LessonTable.courseId)
                .select { LessonTable.id eq lessonId }
                .map { it[LessonTable.courseId] }
                .singleOrNull()
        }

    override suspend fun existLesson(lessonId: Long): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable
                .select { LessonTable.id eq lessonId }
                .count() > 0
        }

    override suspend fun getLessonDescriptions(courseId: Long): List<LessonDescription> =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable.select { LessonTable.courseId eq courseId }
                .map { getLessonDescription(it) }
        }

    override suspend fun getLessonIds(courseId: Long): List<Long> =
        newSuspendedTransaction(Dispatchers.IO) {
            LessonTable
                .slice(LessonTable.id)
                .select { LessonTable.courseId eq courseId }
                .map { it[LessonTable.id].value }
        }

    override suspend fun addLesson(courseId: Long, number: Int, name: String, description: String): Long =
        newSuspendedTransaction(
            Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        ) {
            val maxNumber = getMaxNumber(courseId) ?: -1
            val n = number.coerceAtMost(maxNumber + 1)

            val ordered = getWithGreaterOrEqualNumber(courseId, n, SortOrder.DESC)

            for (id in ordered) {
                LessonTable.update({
                    LessonTable.id eq id
                }) {
                    with(SqlExpressionBuilder) {
                        it.update(LessonTable.number, LessonTable.number + 1)
                    }
                }
            }


            val id = insert(courseId, number, name, description)

            id
        }

    override suspend fun removeLesson(lessonId: Long) =
        newSuspendedTransaction(
            Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        ) {
            val (n, courseId) = LessonTable.slice(LessonTable.number, LessonTable.courseId)
                .select(LessonTable.id eq lessonId)
                .map { Pair(it[LessonTable.number], it[LessonTable.courseId]) }
                .singleOrNull() ?: throw NoSuchLessonException(lessonId)

            LessonTable.deleteWhere { id eq lessonId }

            val ordered = getWithGreaterOrEqualNumber(courseId, n, SortOrder.ASC)

            for (id in ordered) {
                LessonTable.update({
                    LessonTable.id eq id
                }) {
                    with(SqlExpressionBuilder) {
                        it.update(number, number - 1)
                    }
                }
            }
        }

    private fun getWithGreaterOrEqualNumber(courseId: Long, n: Int, order: SortOrder) =
        LessonTable.slice(LessonTable.id).select {
            (LessonTable.courseId eq courseId) and (LessonTable.number greaterEq n)
        }.orderBy(LessonTable.number, order = order).map { it[LessonTable.id].value }

    override suspend fun addLastLesson(courseId: Long, name: String, description: String): Long =
        newSuspendedTransaction(
            Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        ) {
            val number = (getMaxNumber(courseId) ?: -1) + 1

            val id = insert(courseId, number, name, description)

            id
        }

    private fun getLessonDescription(it: ResultRow) = LessonDescription(
        it[LessonTable.id].value,
        it[LessonTable.courseId],
        it[LessonTable.number],
        it[LessonTable.name],
        it[LessonTable.description]
    )

    private fun getMaxNumber(courseId: Long): Int? {
        val max = LessonTable.number.max()
        return LessonTable.slice(max)
            .select { LessonTable.courseId eq courseId }
            .singleOrNull()
            ?.get(max)
    }

    private fun insert(
        courseId: Long,
        number: Int,
        name: String,
        description: String
    ) = LessonTable.insertAndGetId {
        it[LessonTable.courseId] = courseId
        it[LessonTable.number] = number
        it[LessonTable.name] = name
        it[LessonTable.description] = description
    }.value
}
