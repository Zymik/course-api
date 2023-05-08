package ru.kosolapov.course.course


import kotlinx.serialization.Serializable
import ru.kosolapov.course.lesson.LessonDescription
import ru.kosolapov.course.user.User


@Serializable
data class Course(
    val id: Long,
    val name: String,
    val description: String,
    val lessons: List<LessonDescription>
) {
    constructor(courseDescription: CourseDescription, lessons: List<LessonDescription>) :
            this(
                courseDescription.id,
                courseDescription.name,
                courseDescription.description,
                lessons
            )
}

@Serializable
data class CourseMember(
    val user: User,
    val roles: Set<Role>
)

@Serializable
enum class Role {
    STUDENT, TEACHER, ADMIN
}

@Serializable
data class CourseDescription(
    val id: Long,
    val name: String,
    val description: String
)