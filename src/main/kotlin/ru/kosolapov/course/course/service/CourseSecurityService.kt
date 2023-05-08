package ru.kosolapov.course.course.service

import ru.kosolapov.course.course.Role
import ru.kosolapov.course.course.exception.NoCourseMemberRoleException
import ru.kosolapov.course.course.exception.NoSuchCourseException
import ru.kosolapov.course.course.exception.NotCourseMemberException
import ru.kosolapov.course.course.repository.CourseMemberRepository
import ru.kosolapov.course.course.repository.CourseRepository


class CourseSecurityService(
    private val courseMemberRepository: CourseMemberRepository,
    private val courseRepository: CourseRepository
) {

    suspend fun checkMember(username: String, courseId: Long) {
        checkCourse(courseId)
        if (!courseMemberRepository.isMember(courseId, username)) {
            throw NotCourseMemberException(courseId, username)
        }
    }

    /**
     * Check that user have one of the roles in course, else throw exception
     */
    suspend fun checkMember(username: String, courseId: Long, vararg roles: Role) {
        checkCourse(courseId)
        val userRoles = courseMemberRepository.getMember(courseId, username)?.roles
            ?: throw NotCourseMemberException(courseId, username)

        if (!roles.fold(false) { bool, role -> bool || userRoles.contains(role) }) {
            throw NoCourseMemberRoleException(username, courseId, userRoles)
        }
    }

    suspend fun checkCourse(courseId: Long) {
        if (!courseRepository.isCourseExist(courseId)) {
            throw NoSuchCourseException(courseId)
        }
    }
}

suspend fun CourseSecurityService.checkTeacher(username: String, courseId: Long) =
    checkMember(username, courseId, Role.TEACHER)

suspend fun CourseSecurityService.checkAdmin(username: String, courseId: Long) =
    checkMember(username, courseId, Role.ADMIN)

suspend fun CourseSecurityService.checkStudent(username: String, courseId: Long) =
    checkMember(username, courseId, Role.STUDENT)
