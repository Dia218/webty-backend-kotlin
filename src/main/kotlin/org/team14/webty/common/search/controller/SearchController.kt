package org.team14.webty.common.search.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.team14.webty.common.search.dto.SearchResponseDto
import org.team14.webty.common.search.dto.SearchSuggestionDto
import org.team14.webty.common.search.service.SearchService
import org.team14.webty.common.search.service.AutocompleteService

// REST API를 제공하는 컨트롤러임을 나타냅니다.
@RestController
// "/search" 경로로 들어오는 모든 요청을 이 컨트롤러에서 처리합니다.
@RequestMapping("/search")
class SearchController(
    // 핵심 검색 서비스를 주입받아 사용합니다.
    private val searchService: SearchService,
    // 자동완성 서비스를 주입받아 사용합니다.
    private val autocompleteService: AutocompleteService
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
    
        try {
            // 검색 타입과 정렬 방식, 필터에 따라 적절한 서비스 메서드를 호출
            val result = when {
                // 웹툰 이름으로 검색
                searchType == "webtoonName" && sortBy == "recommend" -> 
                    searchService.searchByWebtoonNameOrderByRecommendCount(keyword, page, size)
                searchType == "webtoonName" && sortBy == "recent" -> 
                    searchService.searchByWebtoonName(keyword, page, size)
                
                // 닉네임으로 검색
                searchType == "nickname" && sortBy == "recommend" -> 
                    searchService.searchByNicknameOrderByRecommendCount(keyword, page, size)
                searchType == "nickname" && sortBy == "recent" -> 
                    searchService.searchByNickname(keyword, page, size)
                    
                // 리뷰 내용 및 제목으로 검색
                searchType == "reviewContent" && sortBy == "recommend" -> 
                    searchService.searchByReviewContentOrderByRecommendCount(keyword, page, size)
                searchType == "reviewContent" && sortBy == "recent" -> 
                    searchService.searchByReviewContent(keyword, page, size)
                
                // 필터 적용 (webtoon 필터)
                filter == "webtoon" && sortBy == "recommend" ->
                    searchService.searchByWebtoonNameOrderByRecommendCount(keyword, page, size)
                filter == "webtoon" && sortBy == "recent" ->
                    searchService.searchByWebtoonName(keyword, page, size)
                    
                // 필터 적용 (user 필터)
                filter == "user" && sortBy == "recommend" ->
                    searchService.searchByNicknameOrderByRecommendCount(keyword, page, size)
                filter == "user" && sortBy == "recent" ->
                    searchService.searchByNickname(keyword, page, size)
                    
                // 필터 적용 (review 필터)
                filter == "review" && sortBy == "recommend" ->
                    searchService.searchByReviewContentOrderByRecommendCount(keyword, page, size)
                filter == "review" && sortBy == "recent" ->
                    searchService.searchByReviewContent(keyword, page, size)
                    
                // 일반 검색 (필터 없음)
                sortBy == "recommend" -> 
                    searchService.searchOrderByRecommendCount(keyword, page, size)
                else -> 
                    searchService.search(keyword, page, size)
            }
            
            log.info("검색 결과: keyword={}, resultCount={}", keyword, result.results.size)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            log.error("검색 중 오류 발생: keyword={}, error={}", keyword, e.message, e)
            throw e
        }
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
        
        try {
            // suggestionType에 따라 적절한 서비스 메서드를 호출
            val result = when (suggestionType?.uppercase()) {
                "WEBTOONNAME", "WEBTOON", "WEBTOON_NAME" -> {
                    log.debug("웹툰 이름 자동완성 제안 요청")
                    autocompleteService.getWebtoonNameSuggestions(prefix)
                }
                "NICKNAME", "NICK", "NICK_NAME" -> {
                    log.debug("닉네임 자동완성 제안 요청")
                    autocompleteService.getNicknameSuggestions(prefix)
                }
                "REVIEWCONTENT", "REVIEW", "REVIEW_CONTENT" -> {
                    log.debug("리뷰 내용 자동완성 제안 요청")
                    autocompleteService.getReviewContentSuggestions(prefix)
                }
                else -> {
                    log.debug("일반 자동완성 제안 요청")
                    autocompleteService.getSearchSuggestions(prefix, sortBy)
                }
            }
            
            log.info("자동완성 제안 결과: prefix={}, suggestionCount={}", prefix, result.suggestions.size)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            log.error("자동완성 제안 중 오류 발생: prefix={}, error={}", prefix, e.message, e)
            throw e
        }
    }
    
    /**
     * 인기 검색어 목록을 가져옵니다.
     */
    @GetMapping("/popular")
    suspend fun getPopularSearchTerms(): ResponseEntity<SearchSuggestionDto> {
        log.info("인기 검색어 목록 요청")
        
        try {
            // 빈 접두사로 검색하면 인기 검색어가 반환됨
            val result = autocompleteService.getSearchSuggestions("", "recommend")
            
            log.info("인기 검색어 목록 결과: count={}", result.suggestions.size)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            log.error("인기 검색어 목록 조회 중 오류 발생: error={}", e.message, e)
            throw e
        }
    }
} 