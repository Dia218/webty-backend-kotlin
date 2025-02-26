package org.team14.webty.search.service

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.team14.webty.search.dto.SearchSuggestionDto
import org.team14.webty.search.constants.SearchConstants
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class AutocompleteService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, Any>,
    private val searchCacheService: SearchCacheService
) {
    private val log = LoggerFactory.getLogger(AutocompleteService::class.java)
    private val suggestionProcessor = SuggestionProcessor(redisTemplate)
    
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
        suggestionProcessor.addSuggestion(keyword, SearchConstants.SEARCH_SUGGESTION_KEY)
    }
    
    /**
     * 웹툰 이름을 자동완성 제안에 추가합니다.
     */
    suspend fun addWebtoonNameToSuggestions(webtoonName: String) = withContext(Dispatchers.IO) {
        suggestionProcessor.addSuggestion(webtoonName, SearchConstants.WEBTOON_SUGGESTION_KEY)
    }
    
    /**
     * 닉네임을 자동완성 제안에 추가합니다.
     */
    suspend fun addNicknameToSuggestions(nickname: String) = withContext(Dispatchers.IO) {
        suggestionProcessor.addSuggestion(nickname, SearchConstants.NICKNAME_SUGGESTION_KEY)
    }
    
    /**
     * 리뷰 내용 및 제목에서 추출한 키워드를 자동완성 제안에 추가합니다.
     */
    suspend fun addReviewContentToSuggestions(reviewContent: String, reviewTitle: String) = withContext(Dispatchers.IO) {
        runCatching {
            // 리뷰 제목은 그대로 추가
            if (reviewTitle.length >= SearchConstants.MIN_PREFIX_LENGTH) {
                suggestionProcessor.addSuggestion(reviewTitle, SearchConstants.REVIEW_CONTENT_SUGGESTION_KEY)
            }
            
            // 리뷰 내용에서 키워드 추출 (공백으로 분리하여 2글자 이상인 단어만 추가)
            reviewContent.split(" ", ".", ",", "!", "?", "\n")
                .filter { it.length >= SearchConstants.MIN_PREFIX_LENGTH }
                .forEach { keyword ->
                    suggestionProcessor.addSuggestion(keyword, SearchConstants.REVIEW_CONTENT_SUGGESTION_KEY)
                }
            
            log.info("리뷰 내용 및 제목을 자동완성 제안에 추가")
        }.onFailure { e ->
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
        suggestionProcessor.getSuggestions(prefix, SearchConstants.SEARCH_SUGGESTION_KEY, "/search?keyword={keyword}&sortBy=$sortBy")
    }
    
    /**
     * 접두사에 해당하는 웹툰 이름 제안을 가져옵니다.
     */
    suspend fun getWebtoonNameSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(prefix, SearchConstants.WEBTOON_SUGGESTION_KEY, "/search?keyword={keyword}&searchType=webtoonName")
    }
    
    /**
     * 접두사에 해당하는 닉네임 제안을 가져옵니다.
     */
    suspend fun getNicknameSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(prefix, SearchConstants.NICKNAME_SUGGESTION_KEY, "/search?keyword={keyword}&searchType=nickname")
    }
    
    /**
     * 접두사에 해당하는 리뷰 내용 및 제목 제안을 가져옵니다.
     */
    suspend fun getReviewContentSuggestions(prefix: String): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(prefix, SearchConstants.REVIEW_CONTENT_SUGGESTION_KEY, "/search?keyword={keyword}&searchType=reviewContent")
    }
} 