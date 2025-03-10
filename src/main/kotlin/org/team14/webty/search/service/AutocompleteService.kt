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
import java.util.concurrent.TimeUnit

/**
 * 자동완성 기능을 제공하는 서비스 클래스입니다.
 * SuggestionProcessor를 활용하여 실제 자동완성 처리를 수행합니다.
 */
@Service
class AutocompleteService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, Any>,
    private val searchCacheService: SearchCacheService
) {
    private val log = LoggerFactory.getLogger(AutocompleteService::class.java)
    private val suggestionProcessor = SuggestionProcessor(redisTemplate)
    
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
     * 접두사에 해당하는 검색 제안을 가져옵니다.
     */
    suspend fun getSearchSuggestions(
        prefix: String, 
        sortBy: String = "recommend",
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(
            prefix, 
            SearchConstants.SEARCH_SUGGESTION_KEY, 
            "/search?keyword={keyword}&sortBy=$sortBy",
            minMatchScore
        )
    }
    
    /**
     * 접두사에 해당하는 웹툰 이름 제안을 가져옵니다.
     */
    suspend fun getWebtoonNameSuggestions(
        prefix: String,
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(
            prefix, 
            SearchConstants.WEBTOON_SUGGESTION_KEY, 
            "/search?keyword={keyword}&searchType=webtoonName",
            minMatchScore
        )
    }
    
    /**
     * 접두사에 해당하는 닉네임 제안을 가져옵니다.
     */
    suspend fun getNicknameSuggestions(
        prefix: String,
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(
            prefix, 
            SearchConstants.NICKNAME_SUGGESTION_KEY, 
            "/search?keyword={keyword}&searchType=nickname",
            minMatchScore
        )
    }
    
    /**
     * 접두사에 해당하는 리뷰 내용 및 제목 제안을 가져옵니다.
     */
    suspend fun getReviewContentSuggestions(
        prefix: String,
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto = withContext(Dispatchers.IO) {
        suggestionProcessor.getSuggestions(
            prefix, 
            SearchConstants.REVIEW_CONTENT_SUGGESTION_KEY, 
            "/search?keyword={keyword}&searchType=reviewContent",
            minMatchScore
        )
    }
    
    /**
     * 자동완성 제안을 가져옵니다.
     * 
     * @param prefix 검색어 접두사
     * @param suggestionType 자동완성 타입
     * @param sortBy 정렬 방식
     * @param minMatchScore 최소 유사도 점수
     * @return 자동완성 제안 DTO
     */
    suspend fun getSuggestions(
        prefix: String,
        suggestionType: String?,
        sortBy: String,
        minMatchScore: Double = SearchConstants.DEFAULT_MIN_MATCH_SCORE
    ): SearchSuggestionDto {
        log.debug("자동완성 제안 처리 시작: prefix={}, suggestionType={}, sortBy={}, minMatchScore={}", 
                prefix, suggestionType, sortBy, minMatchScore)
        
        // suggestionType에 따라 적절한 서비스 메서드를 호출
        return when (suggestionType?.uppercase()) {
            "WEBTOONNAME", "WEBTOON", "WEBTOON_NAME" -> {
                log.debug("웹툰 이름 자동완성 제안 요청")
                getWebtoonNameSuggestions(prefix, minMatchScore)
            }
            "NICKNAME", "NICK", "NICK_NAME" -> {
                log.debug("닉네임 자동완성 제안 요청")
                getNicknameSuggestions(prefix, minMatchScore)
            }
            "REVIEWCONTENT", "REVIEW", "REVIEW_CONTENT" -> {
                log.debug("리뷰 내용 자동완성 제안 요청")
                getReviewContentSuggestions(prefix, minMatchScore)
            }
            else -> {
                log.debug("일반 자동완성 제안 요청")
                getSearchSuggestions(prefix, sortBy, minMatchScore)
            }
        }
    }
    
    /**
     * 인기 검색어 목록을 가져옵니다.
     * 
     * @return 인기 검색어 목록 DTO
     */
    suspend fun getPopularSearchTerms(): SearchSuggestionDto {
        log.debug("인기 검색어 목록 조회 시작")
        
        // 빈 접두사로 검색하면 인기 검색어가 반환됨
        return getSearchSuggestions("", "recommend")
    }
} 