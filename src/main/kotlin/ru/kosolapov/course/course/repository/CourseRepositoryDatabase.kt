import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.kosolapov.course.course.Course
import ru.kosolapov.course.course.CourseDescription
import ru.kosolapov.course.course.repository.CourseRepository
import ru.kosolapov.course.lesson.repository.LessonRepository
import ru.kosolapov.course.table.CourseTable

class CourseRepositoryDatabase(
    private val lessonRepository: LessonRepository
) : CourseRepository {

    override suspend fun createCourse(name: String, description: String): Long =
        newSuspendedTransaction(Dispatchers.IO) {
            val id = CourseTable.insertAndGetId {
                it[CourseTable.name] = name
                it[CourseTable.description] = description
            }.value
            id
        }


    override suspend fun getCourseDescription(id: Long): CourseDescription? =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseTable.select {
                CourseTable.id eq id
            }.map {
                CourseDescription(it[CourseTable.id].value, it[CourseTable.name], it[CourseTable.description])
            }.singleOrNull()
        }

    override suspend fun isCourseExist(id: Long): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            CourseTable.select {
                CourseTable.id eq id
            }.count() > 0
        }

    override suspend fun getCourse(id: Long): Course? {
        val description = getCourseDescription(id) ?: return null
        val lessons = lessonRepository.getLessonDescriptions(id)
        return Course(description, lessons)
    }

}