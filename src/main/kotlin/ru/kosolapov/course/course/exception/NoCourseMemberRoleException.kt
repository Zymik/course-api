package ru.kosolapov.course.course.exception

import ru.kosolapov.course.course.Role
import ru.kosolapov.course.exception.ForbiddenException

class NoCourseMemberRoleException(username: String, courseId: Long, role: Set<Role>) :
    ForbiddenException(
        "Member with name $username of course $courseId don't not have any of roles: ${role.joinToString(", ")}"
    )