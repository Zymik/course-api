package ru.kosolapov.course.score

import ru.kosolapov.course.lesson.NoSuchLessonException
import ru.kosolapov.course.lesson.repository.LessonRepository
import ru.kosolapov.course.task.exception.NoSuchTaskException
import ru.kosolapov.course.task.repository.TaskRepository

class ScoreService(
    private val taskRepository: TaskRepository,
    private val lessonRepository: LessonRepository,
    private val userTaskScoreRepository: UserTaskScoreRepository
) {
    suspend fun getScoreByTaskId(username: String, taskId: Long): Double {
        if (taskRepository.existTask(taskId)) {
            throw NoSuchTaskException(taskId)
        }
        return userTaskScoreRepository.getScore(username, taskId) ?: 0.0
    }

    suspend fun getScoreByTaskId(taskId: Long): Double =
        taskRepository.getScore(taskId) ?: throw NoSuchTaskException(taskId)

    suspend fun setTaskScore(username: String, taskId: Long, score: Double) {
        userTaskScoreRepository.setScore(username, score, taskId)
    }

    suspend fun getScoreByLessonId(lessonId: Long): Double {
        if (!lessonRepository.existLesson(lessonId)) {
            throw NoSuchLessonException(lessonId)
        }
        return taskRepository.getTaskIdsByLessonId(lessonId).sumOf { getScoreByTaskId(it) }
    }

    suspend fun getScoreByLessonId(username: String, lessonId: Long): Double {
        if (!lessonRepository.existLesson(lessonId)) {
            throw NoSuchLessonException(lessonId)
        }
        return taskRepository.getTaskIdsByLessonId(lessonId).sumOf { getScoreByTaskId(username, it) }
    }

    suspend fun getScoreByCourseId(courseId: Long): Double {
        return lessonRepository.getLessonIds(courseId).sumOf { getScoreByLessonId(it) }
    }

    suspend fun getScoreByCourseId(username: String, courseId: Long): Double {
        return lessonRepository.getLessonIds(courseId).sumOf { getScoreByLessonId(username, courseId) }
    }

    suspend fun setScore(username: String, score: Double, taskId: Long) =
        userTaskScoreRepository.setScore(username, score, taskId)
}