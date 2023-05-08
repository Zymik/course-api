package ru.kosolapov.course.task.exception

import ru.kosolapov.course.exception.BadRequestException

class InvalidTaskScoreException(score: Double, maxScore: Double, taskId: Long) :
    BadRequestException("Invalid score $score for task with id $taskId, max score is $maxScore")