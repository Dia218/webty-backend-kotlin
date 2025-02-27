package org.team14.webty.search.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.dto.SearchSuggestionDto
import org.team14.webty.search.service.SearchService
import org.team14.webty.search.service.AutocompleteService
import org.team14.webty.search.service.SearchRequestProcessor

// REST API를 제공하는 컨트롤러임을 나타냅니다.
@RestController
// "/search" 경로로 들어오는 모든 요청을 이 컨트롤러에서 처리합니다.
@RequestMapping("/search")
class SearchController(
    // 핵심 검색 서비스를 주입받아 사용합니다.
    private val searchService: SearchService,
    // 자동완성 서비스를 주입받아 사용합니다.
    private val autocompleteService: AutocompleteService,
    // 검색 요청 처리 서비스를 주입받아 사용합니다.
    private val searchRequestProcessor: SearchRequestProcessor
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
     */
    @GetMapping("/suggestions")
    suspend fun getSearchSuggestions(
        @RequestParam prefix: String,
        @RequestParam(required = false) suggestionType: String?,
        @RequestParam(defaultValue = "recommend") sortBy: String
    ): ResponseEntity<SearchSuggestionDto> {
        log.info("자동완성 제안 요청: prefix={}, suggestionType={}, sortBy={}", prefix, suggestionType, sortBy)
        
        return runCatching {
            // 자동완성 요청 처리를 서비스에 위임
            val result = autocompleteService.getSuggestions(prefix, suggestionType, sortBy)
            
            log.info("자동완성 제안 결과: prefix={}, suggestionCount={}", prefix, result.suggestions.size)
            ResponseEntity.ok(result)
        }.onFailure { e ->
            log.error("자동완성 제안 중 오류 발생: prefix={}, error={}", prefix, e.message, e)
            throw e
        }.getOrThrow()
    }
    
    /**
     * 인기 검색어 목록을 가져옵니다.
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