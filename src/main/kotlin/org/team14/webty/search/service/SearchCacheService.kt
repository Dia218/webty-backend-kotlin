package org.team14.webty.search.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.team14.webty.review.entity.Review
import org.team14.webty.common.util.CompressionUtils
import org.team14.webty.search.constants.SearchConstants
import java.time.Duration

@Service
class SearchCacheService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(SearchCacheService::class.java)
    private val CACHE_TTL = Duration.ofHours(SearchConstants.NORMAL_CACHE_TTL_HOURS)
    private val POPULAR_CACHE_TTL = Duration.ofHours(SearchConstants.POPULAR_CACHE_TTL_HOURS)

    /**
     * 캐시에서 검색 결과를 가져옵니다.
     * 캐시가 비활성화됨에 따라 항상 null을 반환합니다.
     * @param cacheKey 캐시 키
     * @return null (캐시 사용 안함)
     */
    @Cacheable(value = ["search"], key = "#cacheKey", unless = "#result == null || #result.isEmpty()")
    fun getFromCache(cacheKey: String): List<Review>? {
        log.debug("캐시 기능이 비활성화되었습니다. 항상 null 반환: $cacheKey")
        return null
    }

    /**
     * 검색 결과를 캐시에 저장합니다.
     * 캐시가 비활성화됨에 따라 아무 작업도 수행하지 않습니다.
     * @param cacheKey 캐시 키
     * @param resultList 검색 결과 리스트
     * @param isPopular 인기 검색어 여부
     */
    fun cacheResults(cacheKey: String, resultList: List<Review>, isPopular: Boolean = false) {
        log.debug("캐시 기능이 비활성화되었습니다. 캐싱 작업을 수행하지 않습니다: $cacheKey")
        // 캐싱 비활성화 - 아무 작업도 수행하지 않음
    }

    /**
     * 특정 패턴의 캐시를 무효화합니다.
     * @param pattern 캐시 키 패턴
     */
    @CacheEvict(value = ["search"], allEntries = true)
    fun invalidateCache(pattern: String) {
        runCatching {
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                log.info("캐시에서 ${keys.size}개 항목 삭제: $pattern")
            }
        }.onFailure { e ->
            log.error("캐시를 무효화하는 중 오류 발생: ${e.message}", e)
        }
    }
} 