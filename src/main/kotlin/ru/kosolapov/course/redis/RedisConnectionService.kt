package ru.kosolapov.course.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.support.BoundedAsyncPool
import kotlinx.coroutines.future.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisConnectionService(val connectionPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>) {
    val log: Logger = LoggerFactory.getLogger(RedisConnectionService::class.java)

    suspend inline fun <reified T> withDecode(
        crossinline operation: suspend RedisCoroutinesCommands<String, String>.() -> String?
    ): T? {
        val connection = connectionPool.acquire().await()
        try {
            val commands = connection.coroutines()
            val result = commands.operation() ?: return null

            return try {
                Json.decodeFromString<T?>(result)
            } catch (e: IllegalArgumentException) {
                log.error("Exception while deserializing result from redis: $e")
                null
            }

        } finally {
            release(connection)
        }
    }

    suspend inline fun <reified T> with(
        crossinline operation: suspend RedisCoroutinesCommands<String, String>.() -> T
    ): T {
        val connection = connectionPool.acquire().await()
        try {
            val commands = connection.coroutines()
            return commands.operation()
        } finally {
            release(connection)
        }
    }



    suspend fun release(connection: StatefulRedisConnection<String, String>) {
        try {
            connectionPool.release(connection).await()
        } catch (e: Exception) {
            log.error("Exception while releasing connection: $e")
        }
    }
}