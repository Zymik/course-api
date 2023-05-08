package ru.kosolapov.course.course.repository


import ru.kosolapov.course.course.Course
import ru.kosolapov.course.course.CourseDescription


interface CourseRepository {
    suspend fun createCourse(name: String, description: String): Long
    suspend fun getCourse(id: Long): Course?
    suspend fun getCourseDescription(id: Long): CourseDescription?
    suspend fun isCourseExist(id: Long): Boolean
}
