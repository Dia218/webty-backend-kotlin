package org.team14.webty.search.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.dto.SearchSuggestionDto
import org.team14.webty.search.service.SearchService
import org.team14.webty.search.service.AutocompleteService
import org.team14.webty.search.service.SearchRequestProcessor
import org.team14.webty.search.service.SearchCacheService

/**
 * 검색 관련 API를 제공하는 컨트롤러입니다.
 * 검색, 자동완성, 인기 검색어 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/search")
class SearchController(
    // 핵심 검색 서비스를 주입받아 사용합니다.
    private val searchService: SearchService,
    // 자동완성 서비스를 주입받아 사용합니다.
    // 내부적으로 SuggestionProcessor를 사용하여 실제 자동완성 처리를 수행합니다.
    private val autocompleteService: AutocompleteService,
    // 검색 요청 처리 서비스를 주입받아 사용합니다.
    private val searchRequestProcessor: SearchRequestProcessor,
    // 검색 캐시 서비스를 주입받아 사용합니다.
    private val searchCacheService: SearchCacheService
) {
    private val log = LoggerFactory.getLogger(SearchController::class.java)
    
    /**
     * 검색을 수행합니다.
     * 
     * @param keyword 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param searchType 검색 타입 (webtoonName, nickname, reviewContent, null)
     * @param sortBy 정렬 방식 (recommend, recent)
     * @param filter 필터 (all, webtoon, user, review)
     * @return 검색 결과
     */
    @GetMapping
    suspend fun search(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) searchType: String?,
        @RequestParam(defaultValue = "recommend") sortBy: String,
        @RequestParam(defaultValue = "all") filter: String
    ): ResponseEntity<SearchResponseDto> {
        log.info("검색 요청: keyword={}, searchType={}, sortBy={}, filter={}, page={}, size={}", 
                keyword, searchType, sortBy, filter, page, size)
    
        return runCatching {
            // 검색 요청 처리 서비스를 통해 검색 수행
            val result = searchRequestProcessor.processSearchRequest(
                keyword, page, size, searchType, sortBy, filter
            )
            
            log.info("검색 결과: keyword={}, resultCount={}", keyword, result.results.size)
            ResponseEntity.ok(result)
        }.onFailure { e ->
            log.error("검색 중 오류 발생: keyword={}, error={}", keyword, e.message, e)
            throw e
        }.getOrThrow()
    }
    
    /**
     * 검색 캐시를 초기화합니다.
     */
    @PostMapping("/clear-cache")
    suspend fun clearCache(): ResponseEntity<Map<String, String>> {
        log.info("검색 캐시 초기화 요청")
        
        return runCatching {
            // 모든 검색 관련 캐시 제거
            searchCacheService.invalidateCache("search:*")
            
            log.info("검색 캐시 초기화 완료")
            ResponseEntity.ok(mapOf("message" to "검색 캐시가 성공적으로 초기화되었습니다."))
        }.onFailure { e ->
            log.error("검색 캐시 초기화 중 오류 발생: error={}", e.message, e)
            throw e
        }.getOrThrow()
    }
    
    /**
     * 추천수 기준으로 정렬된 검색을 수행합니다.
     */
    @GetMapping("/recommendations")
    suspend fun searchOrderByRecommendations(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) searchType: String?,
        @RequestParam(defaultValue = "all") filter: String
    ): ResponseEntity<SearchResponseDto> {
        log.info("추천수 기준 검색 요청: keyword={}, searchType={}, filter={}, page={}, size={}", 
                keyword, searchType, filter, page, size)
        
        // 기존의 search 메서드를 호출하고 정렬 방식을 "recommend"로 지정
        return search(keyword, page, size, searchType, "recommend", filter)
    }
    
    /**
     * 자동완성 제안을 가져옵니다.
     * AutocompleteService를 통해 SuggestionProcessor의 기능을 활용합니다.
     * 
     * @param prefix 검색어 접두사
     * @param suggestionType 제안 타입 (webtoonName, nickname, reviewContent, null)
     * @param sortBy 정렬 방식 (recommend, recent)
     * @param minMatchScore 최소 유사도 점수 (0.0 ~ 1.0)
     * @return 자동완성 제안 결과
     */
    @GetMapping("/suggestions")
    suspend fun getSearchSuggestions(
        @RequestParam prefix: String,
        @RequestParam(required = false) suggestionType: String?,
        @RequestParam(defaultValue = "recommend") sortBy: String,
        @RequestParam(defaultValue = "0.5") minMatchScore: Double
    ): ResponseEntity<SearchSuggestionDto> {
        log.info("자동완성 제안 요청: prefix={}, suggestionType={}, sortBy={}, minMatchScore={}", 
                prefix, suggestionType, sortBy, minMatchScore)
        
        return runCatching {
            // 자동완성 요청 처리를 서비스에 위임
            val result = autocompleteService.getSuggestions(prefix, suggestionType, sortBy, minMatchScore)
            
            log.info("자동완성 제안 결과: prefix={}, suggestionCount={}", prefix, result.suggestions.size)
            ResponseEntity.ok(result)
        }.onFailure { e ->
            log.error("자동완성 제안 중 오류 발생: prefix={}, error={}", prefix, e.message, e)
            throw e
        }.getOrThrow()
    }
    
    /**
     * 인기 검색어 목록을 가져옵니다.
     * AutocompleteService를 통해 SuggestionProcessor의 인기 검색어 관리 기능을 활용합니다.
     * 
     * @return 인기 검색어 목록
     */
    @GetMapping("/popular")
    suspend fun getPopularSearchTerms(): ResponseEntity<SearchSuggestionDto> {
        log.info("인기 검색어 목록 요청")
        
        return runCatching {
            // 인기 검색어 목록 조회를 서비스에 위임
            val result = autocompleteService.getPopularSearchTerms()
            
            log.info("인기 검색어 목록 결과: count={}", result.suggestions.size)
            ResponseEntity.ok(result)
        }.onFailure { e ->
            log.error("인기 검색어 목록 조회 중 오류 발생: error={}", e.message, e)
            throw e
        }.getOrThrow()
    }
} 