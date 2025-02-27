package org.team14.webty.search.service

import org.springframework.stereotype.Service
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.enums.SearchType
import org.team14.webty.search.enums.SortType
import org.slf4j.LoggerFactory

/**
 * 검색 요청을 처리하는 서비스 클래스입니다.
 * 컨트롤러에서 분리된 비즈니스 로직을 담당합니다.
 */
@Service
class SearchRequestProcessor(
    private val searchService: SearchService
) {
    private val log = LoggerFactory.getLogger(SearchRequestProcessor::class.java)
    
    /**
     * 검색 요청을 처리합니다.
     * 
     * @param keyword 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param searchType 검색 타입 문자열
     * @param sortBy 정렬 방식 문자열
     * @param filter 필터 문자열
     * @return 검색 결과
     */
    suspend fun processSearchRequest(
        keyword: String,
        page: Int,
        size: Int,
        searchType: String?,
        sortBy: String,
        filter: String
    ): SearchResponseDto {
        log.debug("검색 요청 처리 시작: keyword={}, searchType={}, sortBy={}, filter={}", 
                keyword, searchType, sortBy, filter)
        
        // 검색 타입 결정
        val searchTypeEnum = determineSearchType(searchType, filter)
        
        // 정렬 방식 결정
        val sortTypeEnum = determineSortType(sortBy)
        
        // 검색 서비스 호출
        return searchService.search(keyword, page, size, searchTypeEnum, sortTypeEnum)
    }
    
    /**
     * 검색 타입을 결정합니다.
     * 
     * @param searchType 검색 타입 문자열
     * @param filter 필터 문자열
     * @return 검색 타입 열거형
     */
    private fun determineSearchType(searchType: String?, filter: String): SearchType {
        return when {
            searchType == "webtoonName" || filter == "webtoon" -> SearchType.WEBTOON_NAME
            searchType == "nickname" || filter == "user" -> SearchType.NICKNAME
            searchType == "reviewContent" || filter == "review" -> SearchType.REVIEW_CONTENT
            else -> SearchType.ALL
        }
    }
    
    /**
     * 정렬 방식을 결정합니다.
     * 
     * @param sortBy 정렬 방식 문자열
     * @return 정렬 방식 열거형
     */
    private fun determineSortType(sortBy: String): SortType {
        return when (sortBy) {
            "recommend" -> SortType.RECOMMEND
            "viewCount" -> SortType.VIEW_COUNT
            else -> SortType.LATEST
        }
    }
} 