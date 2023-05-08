package ru.kosolapov.course.course.exception

import ru.kosolapov.course.exception.ForbiddenException

class NotCourseMemberException(courseId: Long, username: String) :
    ForbiddenException("User with name $username is not member of course with id $courseId")