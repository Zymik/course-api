package ru.kosolapov.course.task.exception

import ru.kosolapov.course.exception.BadRequestException

class NoScoreException(username: String, taskId: Long) :
    BadRequestException("No score for user with name $username and task with id $taskId")