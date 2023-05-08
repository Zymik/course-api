package ru.kosolapov.course.lesson

import ru.kosolapov.course.exception.BadRequestException

class NoSuchLessonException
private constructor(message: String) : BadRequestException(message) {
    constructor(id: Long) : this("No lesson with id $id")
    constructor(courseId: Long, number: Int) : this("No lesson with courseId $courseId and number $number")
}