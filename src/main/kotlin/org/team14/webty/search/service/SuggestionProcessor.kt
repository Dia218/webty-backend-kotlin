package org.team14.webty.search.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.team14.webty.search.constants.SearchConstants
import org.team14.webty.search.dto.SearchSuggestionDto
import java.util.concurrent.ConcurrentHashMap

/**
 * 검색 제안 처리를 위한 유틸리티 클래스입니다.
 */
class SuggestionProcessor(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(SuggestionProcessor::class.java)
    
    // 제안 인기도를 추적하기 위한 맵
    private val suggestionScoreMap = ConcurrentHashMap<String, Int>()
    
    /**
     * 키워드를 지정된 키의 자동완성 제안에 추가합니다.
     */
    fun addSuggestion(keyword: String, suggestionKey: String, addToGeneral: Boolean = true) {
        try {
            if (keyword.length >= SearchConstants.MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(suggestionKey, keyword)
                
                // 일반 검색 제안에도 추가
                if (addToGeneral) {
                    redisTemplate.opsForSet().add(SearchConstants.SEARCH_SUGGESTION_KEY, keyword)
                }
                
                updateSuggestionScore(keyword)
                log.info("키워드를 자동완성 제안에 추가: $keyword (키: $suggestionKey)")
            }
        } catch (e: Exception) {
            log.error("키워드를 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 제안이 추가되었을 때 점수를 업데이트합니다.
     */
    fun updateSuggestionScore(suggestion: String) {
        suggestionScoreMap.compute(suggestion) { _, score -> (score ?: 0) + 1 }
        
        if (isPopularSuggestion(suggestion)) {
            log.info("인기 제안 감지: {}, 점수: {}", suggestion, suggestionScoreMap[suggestion])
        }
    }
    
    /**
     * 제안이 인기 있는지 확인합니다.
     */
    fun isPopularSuggestion(keyword: String): Boolean {
        return (suggestionScoreMap[keyword] ?: 0) >= SearchConstants.POPULAR_SUGGESTION_THRESHOLD
    }
    
    /**
     * 접두사에 해당하는 제안을 가져옵니다.
     */
    fun getSuggestions(prefix: String, suggestionKey: String, searchUrlTemplate: String): SearchSuggestionDto {
        try {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(suggestionKey) ?: emptySet()
            
            log.info("제안 조회 - 접두사: {}, 전체 제안 수: {}, 키: {}", prefix, allSuggestions.size, suggestionKey)
            
            // 접두사로 시작하는 제안 필터링
            val matchingSuggestions = if (prefix.isEmpty()) {
                // 접두사가 비어있으면 인기 제안만 반환
                allSuggestions.filter { isPopularSuggestion(it.toString()) }
            } else {
                // 접두사로 시작하는 제안 필터링
                allSuggestions.filter { 
                    val suggestionStr = it.toString().replace("\"", "")
                    suggestionStr.lowercase().startsWith(prefix.lowercase()) 
                }
            }
            
            log.info("제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 인기도 기준 정렬
            val sortedSuggestions = matchingSuggestions.sortedByDescending { 
                suggestionScoreMap.getOrDefault(it.toString(), 0) 
            }
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(SearchConstants.MAX_SUGGESTIONS)
                .map { it.toString().replace("\"", "") }
            
            log.info("최종 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            return SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = searchUrlTemplate.replace("{keyword}", prefix)
            )
        } catch (e: Exception) {
            log.error("제안 조회 중 오류 발생: {}", e.message, e)
            return SearchSuggestionDto(suggestions = emptyList())
        }
    }
} 