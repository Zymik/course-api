package ru.kosolapov.course.configuration

import io.ktor.server.application.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletionStage

fun Application.configureRedisPool(): BoundedAsyncPool<StatefulRedisConnection<String, String>> {
    val client = RedisClient.create()
    log.info("Starting connecting to Redis")
    val pool: CompletionStage<BoundedAsyncPool<StatefulRedisConnection<String, String>>> =
        AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
            {
                client.connectAsync(
                    StringCodec.UTF8,
                    RedisURI.Builder
                        .redis("localhost", environment.config.property("redis.port").getString().toInt())
                        .withPassword(environment.config.property("redis.password").getString().toCharArray())
                        .build()
                )
            }, BoundedPoolConfig.create()
        )!!
    val result = runBlocking { pool.await() }
    result.checkConnection()
    log.info("Connected to Redis")
    return result
}

fun BoundedAsyncPool<StatefulRedisConnection<String, String>>.checkConnection() {
    val connection = runBlocking{ acquire().await()}
    release(connection)
}