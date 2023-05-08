package ru.kosolapov.course.course.repository

import ru.kosolapov.course.course.CourseMember
import ru.kosolapov.course.course.Role

interface CourseMemberRepository {
    suspend fun getMembers(courseId: Long): List<CourseMember>
    suspend fun isMember(courseId: Long, username: String): Boolean
    suspend fun getMember(courseId: Long, username: String): CourseMember?
    suspend fun addMember(courseId: Long, username: String, status: Role)
    suspend fun addTeachers(courseId: Long, teachers: Set<String>)
}

suspend fun CourseMemberRepository.addTeacher(courseId: Long, username: String) =
    addMember(courseId, username, Role.TEACHER)

suspend fun CourseMemberRepository.addStudent(courseId: Long, username: String) =
    addMember(courseId, username, Role.STUDENT)

suspend fun CourseMemberRepository.addAdmin(courseId: Long, username: String) =
    addMember(courseId, username, Role.ADMIN)
