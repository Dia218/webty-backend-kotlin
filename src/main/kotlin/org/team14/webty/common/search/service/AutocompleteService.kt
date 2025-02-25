package org.team14.webty.common.search.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.team14.webty.common.search.dto.SearchSuggestionDto

@Service
class AutocompleteService(
    @Qualifier("searchRedisTemplate") private val redisTemplate: RedisTemplate<String, String>,
    private val searchCacheService: SearchCacheService
) {
    private val log = LoggerFactory.getLogger(AutocompleteService::class.java)
    
    companion object {
        private const val WEBTOON_SUGGESTIONS_KEY = "search:webtoon:suggestions"
        private const val NICKNAME_SUGGESTIONS_KEY = "search:nickname:suggestions"
        private const val MAX_SUGGESTIONS = 10
    }
    
    /**
     * 일반 검색어를 자동완성 제안 목록에 추가합니다.
     */
    fun addSearchKeywordToSuggestions(keyword: String) {
        addKeywordToSuggestions(keyword, WEBTOON_SUGGESTIONS_KEY)
    }

    /**
     * 웹툰 이름을 자동완성 제안 목록에 추가합니다.
     */
    fun addWebtoonNameToSuggestions(keyword: String) {
        addKeywordToSuggestions(keyword, WEBTOON_SUGGESTIONS_KEY)
    }

    /**
     * 닉네임을 자동완성 제안 목록에 추가합니다.
     */
    fun addNicknameToSuggestions(keyword: String) {
        addKeywordToSuggestions(keyword, NICKNAME_SUGGESTIONS_KEY)
    }

    /**
     * 키워드를 Redis Set에 추가합니다.
     */
    private fun addKeywordToSuggestions(keyword: String, key: String) {
        try {
            val normalizedKeyword = keyword.trim().lowercase()
            redisTemplate.opsForSet().add(key, normalizedKeyword)
            
            val members = redisTemplate.opsForSet().members(key)
            log.info("현재 저장된 키워드들: key={}, members={}", key, members)
        } catch (e: Exception) {
            log.error("자동완성 키워드 추가 실패: keyword={}, key={}", keyword, key, e)
        }
    }

    /**
     * 검색어 접두사에 기반한 자동완성 제안을 가져옵니다.
     */
    fun getSearchSuggestions(prefix: String, sortBy: String): SearchSuggestionDto {
        return getSuggestionsByPrefix(prefix, WEBTOON_SUGGESTIONS_KEY)
    }

    /**
     * 웹툰 이름 접두사에 기반한 자동완성 제안을 가져옵니다.
     */
    fun getWebtoonNameSuggestions(prefix: String): SearchSuggestionDto {
        return getSuggestionsByPrefix(prefix, WEBTOON_SUGGESTIONS_KEY)
    }

    /**
     * 닉네임 접두사에 기반한 자동완성 제안을 가져옵니다.
     */
    fun getNicknameSuggestions(prefix: String): SearchSuggestionDto {
        return getSuggestionsByPrefix(prefix, NICKNAME_SUGGESTIONS_KEY)
    }

    /**
     * 지정된 키와 접두사에 기반한 자동완성 제안을 가져옵니다.
     */
    private fun getSuggestionsByPrefix(prefix: String, key: String): SearchSuggestionDto {
        try {
            val normalizedPrefix = prefix.trim().lowercase()
            
            val allMembers = redisTemplate.opsForSet().members(key)
            log.info("검색 전 전체 데이터: key={}, allMembers={}", key, allMembers)
            
            val suggestions = allMembers
                ?.filter { it.startsWith(normalizedPrefix) }
                ?.take(MAX_SUGGESTIONS)
                ?: emptyList()
            
            log.info("자동완성 검색 결과: prefix={}, key={}, count={}, suggestions={}", 
                    normalizedPrefix, key, suggestions.size, suggestions)
            
            return SearchSuggestionDto(
                suggestions = suggestions,
                searchUrl = "/search"
            )
        } catch (e: Exception) {
            log.error("자동완성 검색 실패: prefix={}, key={}", prefix, key, e)
            return SearchSuggestionDto(emptyList(), "/search")
        }
    }
} 