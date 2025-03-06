package org.team14.webty.voting.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisPublisher(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = KotlinLogging.logger {}

    fun publish(channel: String, message: Any) {
        val jsonMessage = objectMapper.writeValueAsString(message)
        redisTemplate.convertAndSend(channel, jsonMessage)
        logger.info { "Redis에서 메세지 발행- channel: $channel, message: $jsonMessage" }
    }
}