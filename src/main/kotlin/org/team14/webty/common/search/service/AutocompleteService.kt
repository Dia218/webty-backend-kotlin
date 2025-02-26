package org.team14.webty.common.search.service

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team14.webty.common.search.dto.SearchSuggestionDto
import org.team14.webty.common.search.dto.SuggestionItemDto
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class AutocompleteService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, Any>,
    private val searchCacheService: SearchCacheService
) {
    private val log = LoggerFactory.getLogger(AutocompleteService::class.java)
    
    // 자동완성 제안 관련 상수
    companion object {
        const val MAX_SUGGESTIONS = 10
        const val MIN_PREFIX_LENGTH = 2
        const val POPULAR_THRESHOLD = 3
        const val SEARCH_SUGGESTION_KEY = "search:suggestions"
        const val WEBTOON_NAME_SUGGESTION_KEY = "search:suggestions:webtoonName"
        const val NICKNAME_SUGGESTION_KEY = "search:suggestions:nickname"
        const val REVIEW_CONTENT_SUGGESTION_KEY = "search:suggestions:reviewContent"
        const val POPULAR_SUGGESTION_THRESHOLD = 5
    }
    
    // 제안 인기도를 추적하기 위한 맵
    private val suggestionScoreMap = ConcurrentHashMap<String, Int>()
    
    /**
     * 검색어를 자동완성 제안에 추가합니다.
     */
    suspend fun addSearchKeywordToSuggestions(keyword: String) = withContext(Dispatchers.IO) {
        try {
            if (keyword.length >= MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(SEARCH_SUGGESTION_KEY, keyword)
                updateSuggestionScore(keyword)
                log.info("검색어를 자동완성 제안에 추가: $keyword")
            }
        } catch (e: Exception) {
            log.error("검색어를 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 웹툰 이름을 자동완성 제안에 추가합니다.
     */
    suspend fun addWebtoonNameToSuggestions(webtoonName: String) = withContext(Dispatchers.IO) {
        try {
            if (webtoonName.length >= MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(WEBTOON_NAME_SUGGESTION_KEY, webtoonName)
                redisTemplate.opsForSet().add(SEARCH_SUGGESTION_KEY, webtoonName)
                updateSuggestionScore(webtoonName)
                log.info("웹툰 이름을 자동완성 제안에 추가: $webtoonName")
            }
        } catch (e: Exception) {
            log.error("웹툰 이름을 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 닉네임을 자동완성 제안에 추가합니다.
     */
    suspend fun addNicknameToSuggestions(nickname: String) = withContext(Dispatchers.IO) {
        try {
            if (nickname.length >= MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(NICKNAME_SUGGESTION_KEY, nickname)
                redisTemplate.opsForSet().add(SEARCH_SUGGESTION_KEY, nickname)
                updateSuggestionScore(nickname)
                log.info("닉네임을 자동완성 제안에 추가: $nickname")
            }
        } catch (e: Exception) {
            log.error("닉네임을 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 리뷰 내용 및 제목에서 추출한 키워드를 자동완성 제안에 추가합니다.
     */
    suspend fun addReviewContentToSuggestions(reviewContent: String, reviewTitle: String) = withContext(Dispatchers.IO) {
        try {
            // 리뷰 제목은 그대로 추가
            if (reviewTitle.length >= MIN_PREFIX_LENGTH) {
                redisTemplate.opsForSet().add(REVIEW_CONTENT_SUGGESTION_KEY, reviewTitle)
                redisTemplate.opsForSet().add(SEARCH_SUGGESTION_KEY, reviewTitle)
                updateSuggestionScore(reviewTitle)
            }
            
            // 리뷰 내용에서 키워드 추출 (공백으로 분리하여 2글자 이상인 단어만 추가)
            reviewContent.split(" ", ".", ",", "!", "?", "\n")
                .filter { it.length >= MIN_PREFIX_LENGTH }
                .forEach { keyword ->
                    redisTemplate.opsForSet().add(REVIEW_CONTENT_SUGGESTION_KEY, keyword)
                    redisTemplate.opsForSet().add(SEARCH_SUGGESTION_KEY, keyword)
                    updateSuggestionScore(keyword)
                }
            
            log.info("리뷰 내용 및 제목을 자동완성 제안에 추가")
        } catch (e: Exception) {
            log.error("리뷰 내용 및 제목을 자동완성 제안에 추가하는 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 제안이 추가되었을 때 점수를 업데이트합니다.
     */
    private fun updateSuggestionScore(suggestion: String) {
        suggestionScoreMap.compute(suggestion) { _, score -> (score ?: 0) + 1 }
        
        if (isPopularSuggestion(suggestion)) {
            log.info("인기 제안 감지: {}, 점수: {}", suggestion, suggestionScoreMap[suggestion])
        }
    }
    
    /**
     * 제안이 인기 있는지 확인합니다.
     */
    private fun isPopularSuggestion(keyword: String): Boolean {
        return (suggestionScoreMap[keyword] ?: 0) >= POPULAR_THRESHOLD
    }
    
    /**
     * 접두사에 해당하는 검색 제안을 가져옵니다.
     */
    suspend fun getSearchSuggestions(prefix: String, sortBy: String = "recommend"): SearchSuggestionDto = withContext(Dispatchers.IO) {
        try {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(SEARCH_SUGGESTION_KEY) ?: emptySet()
            
            log.info("검색 제안 조회 - 접두사: {}, 전체 제안 수: {}", prefix, allSuggestions.size)
            
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
            
            log.info("검색 제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 정렬 방식에 따라 제안 정렬
            val sortedSuggestions = when (sortBy) {
                "recommend" -> {
                    // 인기도 기준 정렬
                    matchingSuggestions.sortedByDescending { suggestionScoreMap.getOrDefault(it.toString(), 0) }
                }
                "recent" -> {
                    // 최신순 정렬 (현재는 인기도 기준과 동일하게 처리)
                    matchingSuggestions.sortedByDescending { suggestionScoreMap.getOrDefault(it.toString(), 0) }
                }
                else -> matchingSuggestions.toList()
            }
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(MAX_SUGGESTIONS.toInt())
                .map { it.toString().replace("\"", "") }
            
            log.info("최종 검색 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = "/search?keyword=$prefix&sortBy=$sortBy"
            )
        } catch (e: Exception) {
            log.error("검색 제안 조회 중 오류 발생: {}", e.message, e)
            SearchSuggestionDto(suggestions = emptyList())
        }
    }
    
    /**
     * 접두사에 해당하는 웹툰 이름 제안을 가져옵니다.
     */
    suspend fun getWebtoonNameSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        try {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(WEBTOON_NAME_SUGGESTION_KEY) ?: emptySet()
            
            log.info("웹툰 이름 제안 조회 - 접두사: {}, 전체 제안 수: {}", prefix, allSuggestions.size)
            
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
            
            log.info("웹툰 이름 제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 정렬 방식에 따라 제안 정렬
            val sortedSuggestions = matchingSuggestions.sortedByDescending { 
                suggestionScoreMap.getOrDefault(it.toString(), 0) 
            }
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(MAX_SUGGESTIONS.toInt())
                .map { it.toString().replace("\"", "") }
            
            log.info("최종 웹툰 이름 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = "/search?keyword=$prefix&searchType=webtoonName"
            )
        } catch (e: Exception) {
            log.error("웹툰 이름 제안 조회 중 오류 발생: {}", e.message, e)
            SearchSuggestionDto(suggestions = emptyList())
        }
    }
    
    /**
     * 접두사에 해당하는 닉네임 제안을 가져옵니다.
     */
    suspend fun getNicknameSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        try {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(NICKNAME_SUGGESTION_KEY) ?: emptySet()
            
            log.info("닉네임 제안 조회 - 접두사: {}, 전체 제안 수: {}", prefix, allSuggestions.size)
            
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
            
            log.info("닉네임 제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 정렬 방식에 따라 제안 정렬
            val sortedSuggestions = matchingSuggestions.sortedByDescending { 
                suggestionScoreMap.getOrDefault(it.toString(), 0) 
            }
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(MAX_SUGGESTIONS.toInt())
                .map { it.toString().replace("\"", "") }
            
            log.info("최종 닉네임 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = "/search?keyword=$prefix&searchType=nickname"
            )
        } catch (e: Exception) {
            log.error("닉네임 제안 조회 중 오류 발생: {}", e.message, e)
            SearchSuggestionDto(suggestions = emptyList())
        }
    }
    
    /**
     * 접두사에 해당하는 리뷰 내용 및 제목 제안을 가져옵니다.
     */
    suspend fun getReviewContentSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        try {
            val operations = redisTemplate.opsForSet()
            val allSuggestions = operations.members(REVIEW_CONTENT_SUGGESTION_KEY) ?: emptySet()
            
            log.info("리뷰 내용 제안 조회 - 접두사: {}, 전체 제안 수: {}", prefix, allSuggestions.size)
            
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
            
            log.info("리뷰 내용 제안 필터링 결과 - 접두사: {}, 일치하는 제안 수: {}", prefix, matchingSuggestions.size)
            
            // 정렬 방식에 따라 제안 정렬
            val sortedSuggestions = matchingSuggestions.sortedByDescending { 
                suggestionScoreMap.getOrDefault(it.toString(), 0) 
            }
            
            // 최대 개수만큼 제한
            val limitedSuggestions = sortedSuggestions.take(MAX_SUGGESTIONS.toInt())
                .map { it.toString().replace("\"", "") }
            
            log.info("최종 리뷰 내용 제안 결과 - 접두사: {}, 제안 수: {}", prefix, limitedSuggestions.size)
            
            SearchSuggestionDto(
                suggestions = limitedSuggestions,
                searchUrl = "/search?keyword=$prefix&searchType=reviewContent"
            )
        } catch (e: Exception) {
            log.error("리뷰 내용 제안 조회 중 오류 발생: {}", e.message, e)
            SearchSuggestionDto(suggestions = emptyList())
        }
    }
} 