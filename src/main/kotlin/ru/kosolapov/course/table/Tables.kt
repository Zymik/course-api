package ru.kosolapov.course.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import ru.kosolapov.course.course.Role


const val NAME_LENGTH = 128



object UserTable : Table() {
    val name: Column<String> = varchar("name", NAME_LENGTH).uniqueIndex()
    val salt: Column<ByteArray> = binary("salt")
    val password: Column<ByteArray> = binary("password")

    override val primaryKey = PrimaryKey(name)
}

object CourseTable : LongIdTable() {
    val name: Column<String> = varchar("name", NAME_LENGTH)
    val description: Column<String> = text("description")

}

object CourseMemberTable : Table() {
    val username: Column<String> = varchar("username", NAME_LENGTH).references(UserTable.name)
    val courseId: Column<Long> = long("courseId").references(CourseTable.id)
    val role: Column<Role> = enumeration("role", Role::class)

    init {
        index(true, username, courseId, role)
        index(false, username, courseId, role)
        index(false, username)
        index(false, courseId)
    }
}

object LessonTable : LongIdTable() {
    val courseId: Column<Long> = long("courseId").references(CourseTable.id)
    val number: Column<Int> = integer("number")
    val name: Column<String> = varchar("name", NAME_LENGTH)
    val description: Column<String> = text("description")

    init {
        index(true, courseId, number)
    }
}

object TaskTable : LongIdTable() {
    val lessonId: Column<Long> = long("lessonId")
        .references(LessonTable.id, onDelete = ReferenceOption.CASCADE)

    val number: Column<Int> = integer("number")
    val name: Column<String> = varchar("name", NAME_LENGTH)
    val description: Column<String> = text("description")
    val score: Column<Double> = double("score")

    init {
        index(true, lessonId, number)
    }

}

object UserTaskScoreTable : Table() {
    val taskId: Column<Long> = long("taskId").references(TaskTable.id, onDelete = ReferenceOption.CASCADE)
    val username: Column<String> = varchar("username", NAME_LENGTH)
        .references(UserTable.name, onDelete = ReferenceOption.CASCADE)

    val score: Column<Double> = double("score").check {
        it greaterEq 0.0
    }

    init {
        index(true, taskId, username)
    }
}

object Tables {
    fun init() {
        transaction {
            SchemaUtils.create(
                UserTable,
                CourseTable,
                CourseMemberTable,
                LessonTable,
                TaskTable,
                UserTaskScoreTable
            )
        }
    }
}