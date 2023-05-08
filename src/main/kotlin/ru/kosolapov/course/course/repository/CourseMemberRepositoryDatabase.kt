import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.kosolapov.course.course.CourseMember
import ru.kosolapov.course.course.Role
import ru.kosolapov.course.course.repository.CourseMemberRepository
import ru.kosolapov.course.table.CourseMemberTable
import ru.kosolapov.course.user.User

class CourseMemberRepositoryDatabase : CourseMemberRepository {
    override suspend fun getMembers(courseId: Long): List<CourseMember> =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseMemberTable
                .slice(CourseMemberTable.username, CourseMemberTable.role)
                .select { CourseMemberTable.courseId eq courseId }
                .map { Pair(it[CourseMemberTable.username], it[CourseMemberTable.role]) }
                .groupBy({ it.first }, { it.second })
                .map { (name, members) ->
                    CourseMember(User(name), members.toSet())
                }
        }

    override suspend fun isMember(courseId: Long, username: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseMemberTable
                .select(byUsernameAndCourseId(username, courseId))
                .count() > 0
        }

    private fun byUsernameAndCourseId(
        username: String,
        courseId: Long
    ): SqlExpressionBuilder.() -> Op<Boolean> =
        { (CourseMemberTable.username eq username) and (CourseMemberTable.courseId eq courseId) }


    override suspend fun getMember(courseId: Long, username: String): CourseMember? =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseMemberTable
                .select(byUsernameAndCourseId(username, courseId))
                .groupBy({ it[CourseMemberTable.username] }, { it[CourseMemberTable.role] })
                .map { CourseMember(User(it.key), it.value.toSet()) }
                .singleOrNull()
        }


    override suspend fun addMember(courseId: Long, username: String, status: Role) =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseMemberTable.insertIgnore {
                it[CourseMemberTable.courseId] = courseId
                it[role] = status
                it[CourseMemberTable.username] = username
            }
            return@newSuspendedTransaction
        }

    override suspend fun addTeachers(courseId: Long, teachers: Set<String>) {
        newSuspendedTransaction(Dispatchers.IO) {
            CourseMemberTable.batchInsert(teachers, ignore = true) {
                this[CourseMemberTable.courseId] = courseId
                this[CourseMemberTable.username] = it
                this[CourseMemberTable.role] = Role.TEACHER
            }
        }
    }

}