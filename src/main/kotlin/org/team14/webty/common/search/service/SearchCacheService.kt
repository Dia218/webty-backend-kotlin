package org.team14.webty.common.search.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.team14.webty.review.entity.Review
import java.time.Duration

@Service
class SearchCacheService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(SearchCacheService::class.java)
    private val CACHE_TTL = Duration.ofHours(24)

    fun getFromCache(cacheKey: String): List<Review>? {
        return try {
            val cachedValue = redisTemplate.opsForValue().get(cacheKey)
            cachedValue?.let {
                log.debug("캐시에서 가져온 JSON: $it")
                objectMapper.readValue(
                    it,
                    objectMapper.typeFactory.constructCollectionType(List::class.java, Review::class.java)
                )
            }
        } catch (e: Exception) {
            log.error("캐시된 검색 결과를 가져오는 중 오류 발생: ${e.message}", e)
            null
        }
    }

    fun cacheResults(cacheKey: String, resultList: List<Review>) {
        try {
            val jsonString = objectMapper.writeValueAsString(resultList)
            log.debug("캐시에 저장할 JSON: $jsonString")
            redisTemplate.opsForValue().set(cacheKey, jsonString, CACHE_TTL)
            log.info("검색 결과를 캐시에 저장했습니다: $cacheKey")
        } catch (e: Exception) {
            log.error("검색 결과를 캐시하는 중 오류 발생: ${e.message}", e)
        }
    }

    fun invalidateCache(pattern: String) {
        try {
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                log.info("캐시에서 ${keys.size}개 항목 삭제: $pattern")
            }
        } catch (e: Exception) {
            log.error("캐시를 무효화하는 중 오류 발생: ${e.message}", e)
        }
    }
} 