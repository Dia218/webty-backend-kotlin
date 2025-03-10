package org.team14.webty.search.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.team14.webty.search.constants.SearchConstants
import org.team14.webty.search.dto.SearchSuggestionDto
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * 검색 제안 처리를 위한 유틸리티 클래스입니다.
 * AutocompleteService에서 사용되는 핵심 기능을 제공합니다.
 * 검색어 추가, 인기도 관리, 제안 조회 등의 기능을 담당합니다.
 */
class SuggestionProcessor(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(SuggestionProcessor::class.java)
    
    /**
     * 제안 인기도를 추적하기 위한 맵입니다.
     * 이 맵은 시스템 전체에서 검색어의 인기도를 관리하는 유일한 데이터 구조입니다.
     * 키는 검색어, 값은 해당 검색어가 사용된 횟수입니다.
     */
    private val suggestionScoreMap = ConcurrentHashMap<String, Int>()
    
    /**
     * 키워드를 지정된 키의 자동완성 제안에 추가합니다.
     * 
     * @param keyword 추가할 키워드
     * @param suggestionKey Redis에 저장될 키
     * @param addToGeneral 일반 검색 제안에도 추가할지 여부
     */
    fun addSuggestion(keyword: String, suggestionKey: String, addToGeneral: Boolean = true) {
        runCatching {
            if (keyword.length >= SearchConstants.MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(suggestionKey, keyword)
                
                // 일반 검색 제안에도 추가
                if (addToGeneral) {
                    redisTemplate.opsForSet().add(SearchConstants.SEARCH_SUGGESTION_KEY, keyword)
                }
                
                updateSuggestionScore(keyword)
                log.info("키워드를 자동완성 제안에 추가: $keyword (키: $suggestionKey)")
            }
        }.onFailure { e ->
            log.error("키워드를 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 제안이 추가되었을 때 점수를 업데이트합니다.
     * 이 메서드는 검색어의 인기도를 추적하는 데 사용됩니다.
     * 
     * @param suggestion 점수를 업데이트할 제안(검색어)
     */
    fun updateSuggestionScore(suggestion: String) {
        suggestionScoreMap.compute(suggestion) { _, score -> (score ?: 0) + 1 }
        
        if (isPopularSuggestion(suggestion)) {
            log.info("인기 제안 감지: {}, 점수: {}", suggestion, suggestionScoreMap[suggestion])
        }
    }
    
    /**
     * 제안이 인기 있는지 확인합니다.
     * SearchConstants.POPULAR_SUGGESTION_THRESHOLD 이상의 점수를 가진 검색어는 인기 있는 것으로 간주됩니다.
     * 
     * @param keyword 확인할 키워드
     * @return 인기 있는지 여부
     */
    fun isPopularSuggestion(keyword: String): Boolean {
        return (suggestionScoreMap[keyword] ?: 0) >= SearchConstants.POPULAR_SUGGESTION_THRESHOLD
    }
    
    /**
     * 접두사에 해당하는 제안을 가져옵니다.
     * 
     * @param prefix 검색어 접두사
     * @param suggestionKey 제안 키
     * @param searchUrlTemplate 검색 URL 템플릿
     * @param minMatchScore 최소 유사도 점수 (0.0 ~ 1.0)
     * @return 검색 제안 DTO
     */
    fun getSuggestions(
        prefix: String, 
        suggestionKey: String, 
        searchUrlTemplate: String,
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto {
        return runCatching {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(suggestionKey) ?: emptySet()
            
            log.info("제안 조회 - 접두사: {}, 전체 제안 수: {}, 키: {}", prefix, allSuggestions.size, suggestionKey)
            
            // 접두사가 비어있으면 인기 제안만 반환
            if (prefix.isEmpty()) {
                val popularSuggestions = allSuggestions.filter { isPopularSuggestion(it.toString()) }
                
                // 인기도 기준 정렬
                val sortedSuggestions = popularSuggestions.sortedByDescending { 
                    suggestionScoreMap.getOrDefault(it.toString(), 0) 
                }
                
                // 최대 개수만큼 제한
                val limitedSuggestions = sortedSuggestions.take(SearchConstants.MAX_SUGGESTIONS)
                    .map { it.toString().replace("\"", "") }
                
                log.info("인기 제안 결과 - 제안 수: {}", limitedSuggestions.size)
                
                return SearchSuggestionDto(
                    suggestions = limitedSuggestions,
                    searchUrl = searchUrlTemplate.replace("{keyword}", prefix)
                )
            }
            
            // 접두사로 시작하는 제안 필터링 (초성 검색 포함)
            val matchingSuggestions = allSuggestions.mapNotNull { suggestion ->
                val suggestionStr = suggestion.toString().replace("\"", "")
                val similarity = calculateSimilarity(prefix, suggestionStr)
                
                if (similarity >= minMatchScore) {
                    Pair(suggestionStr, similarity)
                } else {
                    null
                }
            }
            
            log.info("제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 유사도 및 인기도 기준 정렬 (유사도가 같으면 인기도 기준)
            val sortedSuggestions = matchingSuggestions.sortedWith(
                compareByDescending<Pair<String, Double>> { it.second }
                .thenByDescending { suggestionScoreMap.getOrDefault(it.first, 0) }
            )
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(SearchConstants.MAX_SUGGESTIONS)
                .map { it.first }
            
            log.info("최종 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = searchUrlTemplate.replace("{keyword}", prefix)
            )
        }.onFailure { e ->
            log.error("제안 조회 중 오류 발생: {}", e.message, e)
        }.getOrDefault(SearchSuggestionDto(suggestions = emptyList()))
    }
    
    /**
     * 두 문자열 간의 유사도를 계산합니다.
     * 한국어 초성 검색을 지원합니다.
     * 
     * @param query 검색어
     * @param target 대상 문자열
     * @return 유사도 (0.0 ~ 1.0)
     */
    private fun calculateSimilarity(query: String, target: String): Double {
        // 대소문자 구분 없이 비교
        val normalizedQuery = query.lowercase()
        val normalizedTarget = target.lowercase()
        
        // 1. 정확한 접두사 매칭 (가장 높은 점수)
        if (normalizedTarget.startsWith(normalizedQuery)) {
            return 1.0
        }
        
        // 2. 초성 검색 지원
        val queryConsonants = extractConsonants(normalizedQuery)
        if (queryConsonants.isNotEmpty()) {
            val targetConsonants = extractConsonants(normalizedTarget)
            
            // 초성이 일치하는 경우 (높은 점수)
            if (targetConsonants.startsWith(queryConsonants)) {
                return 0.9
            }
            
            // 초성이 포함되는 경우 (중간 점수)
            if (targetConsonants.contains(queryConsonants)) {
                return 0.8
            }
        }
        
        // 3. 부분 문자열 매칭 (낮은 점수)
        if (normalizedTarget.contains(normalizedQuery)) {
            return 0.7
        }
        
        // 4. 레벤슈타인 거리 기반 유사도 (가장 낮은 점수)
        val distance = levenshteinDistance(normalizedQuery, normalizedTarget)
        val maxLength = max(normalizedQuery.length, normalizedTarget.length)
        val similarity = 1.0 - (distance.toDouble() / maxLength)
        
        return if (similarity >= 0.5) similarity * 0.6 else 0.0
    }
    
    /**
     * 한글 문자열에서 초성을 추출합니다.
     * 
     * @param text 한글 문자열
     * @return 초성 문자열
     */
    private fun extractConsonants(text: String): String {
        val consonants = StringBuilder()
        
        for (char in text) {
            val code = char.code
            
            // 한글 범위 내에 있는지 확인
            if (code in SearchConstants.HANGUL_BASE..SearchConstants.HANGUL_END) {
                // 초성 추출
                val consonantIndex = (code - SearchConstants.HANGUL_BASE) / (SearchConstants.VOWELS_COUNT * SearchConstants.FINAL_COUNT)
                consonants.append(SearchConstants.KOREAN_CONSONANTS[consonantIndex])
            } else if (char.toString() in SearchConstants.KOREAN_CONSONANTS) {
                // 이미 초성인 경우
                consonants.append(char)
            } else {
                // 한글이 아닌 경우 그대로 추가
                consonants.append(char)
            }
        }
        
        return consonants.toString()
    }
    
    /**
     * 두 문자열 간의 레벤슈타인 거리를 계산합니다.
     * 
     * @param s1 첫 번째 문자열
     * @param s2 두 번째 문자열
     * @return 레벤슈타인 거리
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        // 빈 문자열 처리
        if (m == 0) return n
        if (n == 0) return m
        
        // DP 배열 초기화
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        // 첫 행과 첫 열 초기화
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        // DP 계산
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[m][n]
    }
} 