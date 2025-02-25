package org.team14.webty.common.config

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
import org.team14.webty.common.redis.RedisSubscriber

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
    // 검색 관련 설정
    //-------------------
    
    /**
     * 검색용 RedisTemplate입니다.
     * 검색 결과 캐싱과 자동완성 기능에 사용됩니다.
     */
    @Bean(name = ["searchRedisTemplate"])
    fun searchRedisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.setConnectionFactory(redisConnectionFactory())
        
        val stringSerializer = StringRedisSerializer()
        template.keySerializer = stringSerializer
        template.valueSerializer = stringSerializer
        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = stringSerializer
        
        template.afterPropertiesSet()
        return template
    }

    //-------------------
    // 투표 관련 설정
    //-------------------

    /**
     * 투표용 RedisTemplate입니다.
     */
    @Bean
    fun voteRedisTemplate(): RedisTemplate<String, Any> = RedisTemplate<String, Any>().apply {
        setConnectionFactory(redisConnectionFactory())
        
        keySerializer = StringRedisSerializer()
        valueSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        hashKeySerializer = StringRedisSerializer()
        hashValueSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        
        afterPropertiesSet()
    }

    /**
     * 투표 결과를 구독하기 위한 메시지 리스너 설정입니다.
     */
    @Bean
    fun messageListenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber)
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(listenerAdapter, PatternTopic("vote-results"))
        }
    }
}