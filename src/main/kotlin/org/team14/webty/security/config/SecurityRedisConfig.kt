package org.team14.webty.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.connection.RedisConnectionFactory

/**
 * JWT 인증을 위한 Redis 설정
 */
@Configuration
class SecurityRedisConfig {
    
    /**
     * JWT 인증용 RedisTemplate입니다.
     * - 토큰 저장 및 관리
     * - 리프레시 토큰 캐싱
     * - 로그아웃된 토큰 블랙리스트 관리
     */
    @Primary
    @Bean
    fun authRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.setConnectionFactory(redisConnectionFactory)
        
        val stringSerializer = StringRedisSerializer()
        template.keySerializer = stringSerializer
        template.valueSerializer = stringSerializer
        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = stringSerializer
        
        template.afterPropertiesSet()
        return template
    }
} 