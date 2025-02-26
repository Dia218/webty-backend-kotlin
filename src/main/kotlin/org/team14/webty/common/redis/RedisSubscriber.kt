package org.team14.webty.common.redis

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.team14.webty.common.dto.PageDto
import org.team14.webty.voting.dto.SimilarResponse

@Component
class RedisSubscriber(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
) : MessageListener {
    private val logger = KotlinLogging.logger {}

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val messageBody = String(message.body)
        logger.info { "Redis에서 메세지 수신: $messageBody" }

        runCatching {
            val pageDto = objectMapper.readValue(messageBody, object : TypeReference<PageDto<SimilarResponse>>() {})

            // WebSocket을 통해 프론트로 전송
            val destination = "/topic/vote-results/${pageDto.content.firstOrNull()?.targetWebtoonId ?: "default"}"
            simpMessagingTemplate.convertAndSend(destination, pageDto)
            destination
        }.onSuccess { destination ->
            logger.info { "Websocket으로 메세지 전송 완료 destination: $destination" }
        }.onFailure { e ->
            logger.info { "전송 중 오류 발생 ${e.message}" }
        }
    }
}
