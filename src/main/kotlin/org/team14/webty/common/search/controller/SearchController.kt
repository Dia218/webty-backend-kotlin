package org.team14.webty.common.search.controller

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
    
    // GET 방식의 "/search" 요청을 처리하는 메서드입니다.
    @GetMapping
    fun search(
        // 검색어를 파라미터로 받습니다.
        @RequestParam keyword: String,
        // 페이지 번호를 파라미터로 받습니다. 기본값은 0입니다.
        @RequestParam(defaultValue = "0") page: Int,
        // 한 페이지에 표시할 결과 수를 파라미터로 받습니다. 기본값은 10입니다.
        @RequestParam(defaultValue = "10") size: Int,
        // 검색 타입을 파라미터로 받습니다. 필수가 아닙니다.
        @RequestParam(required = false) searchType: String?,
        // 정렬 방식을 파라미터로 받습니다. 기본값은 "recommend"(추천순)입니다.
        @RequestParam(defaultValue = "recommend") sortBy: String
    ): ResponseEntity<SearchResponseDto> {
    
        // 검색 타입과 정렬 방식에 따라 적절한 서비스 메서드를 호출하고 결과를 반환합니다.
        return ResponseEntity.ok(
            when {
                // 웹툰 이름으로 검색하고 추천순으로 정렬
                searchType == "webtoonName" && sortBy == "recommend" -> 
                    searchService.searchByWebtoonNameOrderByRecommendCount(keyword, page, size)
                // 웹툰 이름으로 검색하고 최신순으로 정렬
                searchType == "webtoonName" && sortBy == "recent" -> 
                    searchService.searchByWebtoonName(keyword, page, size)
                // 닉네임으로 검색하고 추천순으로 정렬
                searchType == "nickname" && sortBy == "recommend" -> 
                    searchService.searchByNicknameOrderByRecommendCount(keyword, page, size)
                // 닉네임으로 검색하고 최신순으로 정렬
                searchType == "nickname" && sortBy == "recent" -> 
                    searchService.searchByNickname(keyword, page, size)
                // 일반 검색이고 추천순으로 정렬
                sortBy == "recommend" -> 
                    searchService.searchOrderByRecommendCount(keyword, page, size)
                // 그 외의 경우 (일반 검색이고 최신순으로 정렬)
                else -> 
                    searchService.search(keyword, page, size)
            }
        )
    }
    
    // GET 방식의 "/search/recommendations" 요청을 처리하는 메서드입니다.
    @GetMapping("/recommendations")
    fun searchOrderByRecommendations(
        // 검색어를 파라미터로 받습니다.
        @RequestParam keyword: String,
        // 페이지 번호를 파라미터로 받습니다. 기본값은 0입니다.
        @RequestParam(defaultValue = "0") page: Int,
        // 한 페이지에 표시할 결과 수를 파라미터로 받습니다. 기본값은 10입니다.
        @RequestParam(defaultValue = "10") size: Int,
        // 검색 타입을 파라미터로 받습니다. 필수가 아닙니다.
        @RequestParam(required = false) searchType: String?
    ): ResponseEntity<SearchResponseDto> {
        // 기존의 search 메서드를 호출하고 정렬 방식을 "recommend"로 지정합니다.
        return search(keyword, page, size, searchType, "recommend")
    }
    
    // GET 방식의 "/search/suggestions" 요청을 처리하는 메서드입니다.
    @GetMapping("/suggestions")
    fun getSearchSuggestions(
        @RequestParam prefix: String,
        @RequestParam(required = false) suggestionType: String?,
        @RequestParam(defaultValue = "recommend") sortBy: String
    ): ResponseEntity<SearchSuggestionDto> {
        // suggestionType에 따라 적절한 서비스 메서드를 호출
        val result = when (suggestionType?.uppercase()) {
            "WEBTOONNAME", "WEBTOON", "WEBTOON_NAME" -> autocompleteService.getWebtoonNameSuggestions(prefix)
            "NICKNAME", "NICK", "NICK_NAME" -> autocompleteService.getNicknameSuggestions(prefix)
            else -> autocompleteService.getSearchSuggestions(prefix, sortBy)
        }
        
        return ResponseEntity.ok(result)
    }
} 