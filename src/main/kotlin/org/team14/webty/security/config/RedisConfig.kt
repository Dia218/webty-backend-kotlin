package org.team14.webty.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.team14.webty.common.redis.RedisSubscriber

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(host, port)

    @Primary
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> = RedisTemplate<String, Any>().apply {
        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()
        setConnectionFactory(redisConnectionFactory())
    }

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
