package org.team14.webty.search.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.mapper.SearchResponseMapper
import org.team14.webty.review.entity.Review

/**
 * 검색 실행을 담당하는 유틸리티 클래스입니다.
 * 항상 데이터베이스에서 최신 데이터를 조회합니다.
 */
class SearchExecutor(
    private val searchResponseMapper: SearchResponseMapper
) {
    private val log = LoggerFactory.getLogger(SearchExecutor::class.java)
    
    /**
     * 검색을 실행하고 결과를 반환합니다.
     * 항상 데이터베이스에서 최신 결과를 가져옵니다.
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param cacheKey 캐시 키 (로깅 목적으로만 사용)
     * @param searchFunction 실제 검색을 수행하는 함수
     * @return 검색 결과
     */
    suspend fun executeSearch(
        keyword: String,
        page: Int,
        size: Int,
        cacheKey: String,
        searchFunction: suspend (String, Pageable) -> Page<Review>
    ): SearchResponseDto = withContext(Dispatchers.IO) {
        runCatching {
            val pageable: Pageable = PageRequest.of(page, size)
            
            // DB에서 최신 검색 결과를 가져옴
            log.debug("데이터베이스에서 검색 실행: 키워드=$keyword, 페이지=$page, 사이즈=$size")
            val searchResults = searchFunction(keyword, pageable)
            
            val resultList = searchResults.content
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            
            // SearchResponseMapper를 통해 결과를 매핑 (ViewCountCacheService에서 최신 조회수 반영)
            searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
        }.onFailure { e ->
            log.error("검색 실행 중 오류 발생: ${e.message}", e)
        }.getOrDefault(
            SearchResponseDto(
                keyword = keyword,
                results = emptyList()
            )
        )
    }
} 