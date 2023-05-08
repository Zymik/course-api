package ru.kosolapov.course.task.repository

import ru.kosolapov.course.task.Task

interface TaskRepository {
    suspend fun getTask(id: Long): Task?
    suspend fun existTask(id: Long): Boolean
    suspend fun getScore(id: Long): Double?
    suspend fun addTask(lessonId: Long, number: Int, name: String, description: String, score: Double): Long
    suspend fun addLastTask(lessonId: Long, name: String, description: String, score: Double): Long
    suspend fun getTasksByLessonId(lessonId: Long): List<Task>
    suspend fun getTaskIdsByLessonId(lessonId: Long): List<Long>
    suspend fun getLessonId(taskId: Long): Long?
    suspend fun removeTask(taskId: Long)
}
