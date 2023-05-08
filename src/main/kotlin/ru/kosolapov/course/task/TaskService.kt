package ru.kosolapov.course.task

import ru.kosolapov.course.lesson.NoSuchLessonException
import ru.kosolapov.course.lesson.repository.LessonRepository
import ru.kosolapov.course.task.exception.NoSuchTaskException
import ru.kosolapov.course.task.repository.TaskRepository

class TaskService(
    private val taskRepository: TaskRepository,
    private val lessonRepository: LessonRepository
) {
    suspend fun getTasks(courseId: Long, lessonNumber: Int): List<Task> {
        val lessonId = lessonRepository.getLessonId(courseId, lessonNumber)
            ?: throw NoSuchLessonException(courseId, lessonNumber)
        return getTasks(lessonId)
    }

    suspend fun getTasks(lessonId: Long): List<Task> {
        return taskRepository.getTasksByLessonId(lessonId)
    }

    suspend fun checkTaskExist(taskId: Long) {
        taskRepository.getTask(taskId) ?: throw NoSuchTaskException(taskId)
    }

    suspend fun getCourseId(taskId: Long): Long {
        val lessonId = taskRepository.getLessonId(taskId) ?: throw NoSuchTaskException(taskId)
        return lessonRepository.getCourseId(lessonId) ?: throw NoSuchLessonException(lessonId)
    }

    suspend fun addLastTask(lessonId: Long, createTaskRequest: CreateTaskRequest) =
        taskRepository.addLastTask(
            lessonId,
            createTaskRequest.name,
            createTaskRequest.description,
            createTaskRequest.score
        )

    suspend fun addTask(lessonId: Long, number: Int, createTaskRequest: CreateTaskRequest) =
        taskRepository.addTask(
            lessonId,
            number,
            createTaskRequest.name,
            createTaskRequest.description,
            createTaskRequest.score
        )

    suspend fun removeTask(taskId: Long) = taskRepository.removeTask(taskId)
}
