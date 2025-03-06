package org.team14.webty.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.team14.webty.voting.redis.RedisSubscriber

/**
 * Redis 관련 설정을 담당하는 설정 클래스입니다.
 * 검색 기능과 유사도 투표 기능을 위한 설정을 포함합니다.
 */
@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int
) {
    /**
     * Redis 연결을 위한 ConnectionFactory를 생성합니다.
     */
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(host, port)


    //-------------------
    // 투표 관련 설정
    //-------------------

    /**
     * 투표용 RedisTemplate입니다.
     */
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> = RedisTemplate<String, Any>().apply {
        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()
        setConnectionFactory(redisConnectionFactory())
    }

    //-------------------
    // 검색 관련 설정
    //-------------------

    /**
     * 검색용 RedisTemplate입니다.
     * 검색 결과 캐싱과 자동완성 기능에 사용됩니다.
     */
    @Bean(name = ["searchRedisTemplate"])
    fun searchRedisTemplate(objectMapper: ObjectMapper): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory()

        val stringSerializer = StringRedisSerializer()
        template.keySerializer = stringSerializer

        // 값은 JSON으로 직렬화하여 저장
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)
        template.valueSerializer = jsonSerializer

        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = jsonSerializer

        template.afterPropertiesSet()
        return template
    }

    /**
     * 투표 결과를 구독하기 위한 메시지 리스너 설정입니다.
     */
    @Bean
    fun messageListenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber) // RedisSubscriber를 Listener로 등록
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(listenerAdapter, PatternTopic("vote-results")) // 구독 채널 지정
        }
    }
}