package ru.kosolapov.course.course.exception

import ru.kosolapov.course.exception.BadRequestException

class NoSuchCourseException(courseId: Long): BadRequestException("No course with id $courseId")