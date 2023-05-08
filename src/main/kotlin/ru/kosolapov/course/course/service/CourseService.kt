package ru.kosolapov.course.course.service

import ru.kosolapov.course.course.Course
import ru.kosolapov.course.course.CourseDescription
import ru.kosolapov.course.course.exception.NoSuchCourseException
import ru.kosolapov.course.course.repository.CourseMemberRepository
import ru.kosolapov.course.course.repository.CourseRepository
import ru.kosolapov.course.course.repository.addAdmin
import ru.kosolapov.course.course.repository.addStudent

class CourseService(
    private val courseRepository: CourseRepository,
    private val courseMemberRepository: CourseMemberRepository
) {
    suspend fun createCourse(owner: String, teachers: Set<String>, name: String, description: String): Long {
        val id = courseRepository.createCourse(name, description)
        addTeachers(id, teachers)
        courseMemberRepository.addAdmin(id, owner)
        return id
    }

    suspend fun addTeachers(id: Long, teachers: Set<String>) =
        courseMemberRepository.addTeachers(id, teachers)

    suspend fun addStudent(id: Long, student: String) =
        courseMemberRepository.addStudent(id, student)

    suspend fun getCourse(id: Long): Course =
        courseRepository.getCourse(id) ?: throw NoSuchCourseException(id)

    suspend fun getCourseDescription(id: Long): CourseDescription =
        courseRepository.getCourseDescription(id) ?: throw NoSuchCourseException(id)
}