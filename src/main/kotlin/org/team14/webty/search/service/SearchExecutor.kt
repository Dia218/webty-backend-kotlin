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
import java.util.concurrent.ConcurrentHashMap

/**
 * 검색 실행을 담당하는 유틸리티 클래스입니다.
 */
class SearchExecutor(
    private val searchCacheService: SearchCacheService,
    private val searchResponseMapper: SearchResponseMapper
) {
    private val log = LoggerFactory.getLogger(SearchExecutor::class.java)
    
    // 검색어 인기도 추적을 위한 맵
    private val searchCountMap = ConcurrentHashMap<String, Int>()
    
    /**
     * 검색을 실행하고 결과를 반환합니다.
     * 캐시 사용을 비활성화하고 항상 데이터베이스에서 최신 결과를 가져옵니다.
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param cacheKey 캐시 키 (사용하지 않음)
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
            // 검색어 인기도 추적
            trackSearchPopularity(keyword)
            
            val pageable: Pageable = PageRequest.of(page, size)
            
            // 캐시를 사용하지 않고 항상 DB에서 최신 검색 결과를 가져옴
            log.info("데이터베이스에서 검색 실행: $keyword (캐시 사용 안함)")
            val searchResults = searchFunction(keyword, pageable)
            
            val resultList = searchResults.content
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            
            // 검색결과를 캐싱하지 않음
            log.debug("캐시 기능이 비활성화되어 검색 결과를 캐싱하지 않습니다: $keyword")
            
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
    
    /**
     * 검색어의 인기도를 추적합니다.
     */
    fun trackSearchPopularity(keyword: String) {
        val normalizedKeyword = keyword.trim().lowercase()
        searchCountMap.compute(normalizedKeyword) { _, count -> (count ?: 0) + 1 }
    }
    
    /**
     * 인기 검색어인지 확인합니다.
     */
    fun isPopularSearch(keyword: String): Boolean {
        val normalizedKeyword = keyword.trim().lowercase()
        return (searchCountMap[normalizedKeyword] ?: 0) >= org.team14.webty.search.constants.SearchConstants.POPULAR_THRESHOLD
    }
} 