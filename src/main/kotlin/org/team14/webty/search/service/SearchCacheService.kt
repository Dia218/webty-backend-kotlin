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
     * @param cacheKey 캐시 키
     * @return 검색 결과 리스트 또는 null (캐시 미스 시)
     */
    @Cacheable(value = ["search"], key = "#cacheKey", unless = "#result == null || #result.isEmpty()")
    fun getFromCache(cacheKey: String): List<Review>? {
        return try {
            val valueOps = redisTemplate.opsForValue()
            val cachedValue = valueOps.get(cacheKey)
            
            if (cachedValue == null) {
                log.debug("캐시에서 결과를 찾을 수 없음: $cacheKey")
                return null
            }
            
            // 문자열인 경우 (이전 방식과의 호환성)
            if (cachedValue is String) {
                val jsonString = if (CompressionUtils.isCompressed(cachedValue)) 
                    CompressionUtils.decompress(cachedValue) 
                else 
                    cachedValue
                
                log.debug("캐시에서 가져온 문자열 JSON: ${jsonString.take(100)}...")
                
                // TypeReference를 사용하여 명시적으로 타입 지정
                val typeRef = object : TypeReference<List<Review>>() {}
                val reviews = objectMapper.readValue(jsonString, typeRef)
                
                return reviews
            } 
            // 리스트인 경우 (직접 객체 저장)
            else if (cachedValue is List<*>) {
                log.debug("캐시에서 가져온 객체 리스트: ${cachedValue.size}개 항목")
                
                // 캐시된 객체를 Review 객체로 변환
                val reviews = cachedValue.map { item ->
                    if (item is Review) {
                        item
                    } else if (item is Map<*, *>) {
                        // Map을 JSON으로 변환 후 Review 객체로 다시 변환
                        val json = objectMapper.writeValueAsString(item)
                        objectMapper.readValue(json, Review::class.java)
                    } else {
                        throw ClassCastException("캐시된 객체를 Review로 변환할 수 없습니다: ${item?.javaClass?.name}")
                    }
                }
                
                return reviews
            }
            
            log.warn("캐시에서 예상치 못한 타입의 데이터 발견: ${cachedValue.javaClass.name}")
            null
        } catch (e: Exception) {
            log.error("캐시된 검색 결과를 가져오는 중 오류 발생: ${e.message}", e)
            null
        }
    }

    /**
     * 검색 결과를 캐시에 저장합니다.
     * @param cacheKey 캐시 키
     * @param resultList 검색 결과 리스트
     * @param isPopular 인기 검색어 여부
     */
    fun cacheResults(cacheKey: String, resultList: List<Review>, isPopular: Boolean = false) {
        try {
            // 인기 검색어는 더 오래 캐싱
            val ttl = if (isPopular) POPULAR_CACHE_TTL else CACHE_TTL
            
            // JSON 문자열로 변환하여 저장 (직렬화 문제 방지)
            val jsonString = objectMapper.writeValueAsString(resultList)
            
            // 크기가 임계값을 넘으면 압축
            val valueToStore = if (jsonString.length > SearchConstants.COMPRESSION_THRESHOLD) {
                CompressionUtils.compress(jsonString)
            } else {
                jsonString
            }
            
            redisTemplate.opsForValue().set(cacheKey, valueToStore, ttl)
            
            log.info("검색 결과를 캐시에 저장했습니다: $cacheKey (${resultList.size}개 항목)")
        } catch (e: Exception) {
            log.error("검색 결과를 캐시하는 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 특정 패턴의 캐시를 무효화합니다.
     * @param pattern 캐시 키 패턴
     */
    @CacheEvict(value = ["search"], allEntries = true)
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