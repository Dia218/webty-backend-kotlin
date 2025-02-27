package org.team14.webty.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Jackson 설정 클래스
 * LocalDateTime 등의 Java 8 시간 타입을 직렬화/역직렬화하기 위한 설정
 */
@Configuration
class JacksonConfig {

    /**
     * ObjectMapper 빈 설정
     * JavaTimeModule을 등록하여 LocalDateTime 등의 Java 8 시간 타입을 처리
     * KotlinModule을 등록하여 Kotlin 클래스를 처리
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule.Builder().build())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return objectMapper
    }
} 