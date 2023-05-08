package ru.kosolapov.course.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisCacheService(
    val redisConnectionService: RedisConnectionService,
    val expireSeconds: Long
) {
    val log: Logger = LoggerFactory.getLogger(RedisConnectionService::class.java)

    suspend inline fun <reified T> getValue(key: String): T? =
        redisConnectionService.withDecode<T> {
            get(key)
        }

    suspend inline fun <reified T> setValue(key: String, value: T) {
        val encoded = Json.encodeToString(value)
        val result = redisConnectionService.with {
            setex(key, expireSeconds, encoded)
        }

        if (result != "OK") {
            log.error("Error while set value to key $key in redis, result: $result")
        }
    }

    suspend inline fun exist(vararg key: String): Long? =
        redisConnectionService.with {
            exists(*key)
        }

}