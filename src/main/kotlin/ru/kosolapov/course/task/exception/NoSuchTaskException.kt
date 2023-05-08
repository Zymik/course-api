package ru.kosolapov.course.task.exception

import ru.kosolapov.course.exception.BadRequestException

class NoSuchTaskException
private constructor(message: String) : BadRequestException(message) {
    constructor(id: Long) : this("No task with id $id")
    constructor(lessonId: Long, number: Int) : this("No task with lessonId $lessonId and number $number")
}