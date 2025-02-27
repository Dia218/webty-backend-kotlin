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
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param cacheKey 캐시 키
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
            
            // 캐시에서 결과 확인
            val cachedResults = searchCacheService.getFromCache(cacheKey)
            
            if (cachedResults != null) {
                log.info("캐시에서 검색 결과 반환: $keyword (캐시 키: $cacheKey)")
                val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
                return@runCatching searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
            }
            
            // 캐시에 없으면 DB에서 검색
            log.info("데이터베이스에서 검색 실행: $keyword (캐시 키: $cacheKey)")
            val searchResults = searchFunction(keyword, pageable)
            
            // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
            val resultList = searchResults.content
            val isPopular = isPopularSearch(keyword)
            searchCacheService.cacheResults(cacheKey, resultList, isPopular)
            
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
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