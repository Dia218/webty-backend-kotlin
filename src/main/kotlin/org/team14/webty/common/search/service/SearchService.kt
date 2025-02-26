package org.team14.webty.common.search.service

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.search.dto.SearchResponseDto
import org.team14.webty.common.search.repository.SearchRepository
import org.team14.webty.common.search.mapper.SearchResponseMapper
import java.util.concurrent.ConcurrentHashMap

@Service
class SearchService(
    private val searchRepository: SearchRepository,
    private val searchCacheService: SearchCacheService,
    private val autocompleteService: AutocompleteService,
    private val searchResponseMapper: SearchResponseMapper
) {
    private val log = LoggerFactory.getLogger(SearchService::class.java)
    
    // 인기 검색어 추적을 위한 맵
    private val searchCountMap = ConcurrentHashMap<String, Int>()
    private val POPULAR_THRESHOLD = 5 // 이 횟수 이상 검색되면 인기 검색어로 간주
    
    companion object {
        const val SEARCH_CACHE_KEY_PREFIX = "search:"
        const val SEARCH_SUGGESTION_KEY = "search:suggestions"
        const val WEBTOON_SUGGESTION_KEY = "search:webtoon:suggestions"
        const val NICKNAME_SUGGESTION_KEY = "search:nickname:suggestions"
        const val REVIEW_CONTENT_SUGGESTION_KEY = "search:review:suggestions"
        const val SEARCH_CACHE_TTL_MINUTES = 10L
        const val MAX_SUGGESTIONS = 10L
    }
    
    /**
     * 일반 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun search(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        // 검색어 인기도 추적
        trackSearchPopularity(keyword)
        
        val pageable: Pageable = PageRequest.of(page, size)
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}${keyword}:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가 (비동기)
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults.isPresent) {
            log.info("캐시에서 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
            return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 검색 실행: $keyword")
        val searchResults = searchRepository.searchByKeyword(keyword, pageable)
        
        // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
        val resultList = searchResults.content
        val isPopular = isPopularSearch(keyword)
        searchCacheService.cacheResults(cacheKey, resultList, isPopular)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    /**
     * 추천수 기준으로 정렬된 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        // 검색어 인기도 추적
        trackSearchPopularity(keyword)
        
        val pageable: Pageable = PageRequest.of(page, size)
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}${keyword}:recommend:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가 (비동기)
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults.isPresent) {
            log.info("캐시에서 추천수 정렬 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
            return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 추천수 정렬 검색 실행: $keyword")
        val searchResults = searchRepository.searchByKeywordOrderByRecommendCount(keyword, pageable)
        
        // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
        val resultList = searchResults.content
        val isPopular = isPopularSearch(keyword)
        searchCacheService.cacheResults(cacheKey, resultList, isPopular)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    /**
     * 웹툰 이름으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByWebtoonName(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        try {
            // 검색어 인기도 추적
            trackSearchPopularity(keyword)
            
            val pageable: Pageable = PageRequest.of(page, size)
            val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:${page}:${size}"
            
            // 검색어를 웹툰 이름 자동완성 목록에 추가 (비동기)
            autocompleteService.addWebtoonNameToSuggestions(keyword)
            
            // 캐시에서 결과 확인
            val cachedResults = searchCacheService.getFromCache(cacheKey)
            
            if (cachedResults.isPresent) {
                log.info("캐시에서 웹툰이름 검색 결과 반환: $keyword")
                try {
                    val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
                    return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
                } catch (e: Exception) {
                    log.error("캐시된 웹툰이름 검색 결과 처리 중 오류 발생: ${e.message}", e)
                    // 캐시 오류 시 DB에서 다시 검색
                }
            }
            
            // 캐시에 없으면 DB에서 검색
            log.info("데이터베이스에서 웹툰이름 검색 실행: $keyword")
            val searchResults = searchRepository.searchByWebtoonName(keyword, pageable)
            
            // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
            val resultList = searchResults.content
            val isPopular = isPopularSearch(keyword)
            try {
                searchCacheService.cacheResults(cacheKey, resultList, isPopular)
            } catch (e: Exception) {
                log.error("웹툰이름 검색 결과 캐싱 중 오류 발생: ${e.message}", e)
            }
            
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
        } catch (e: Exception) {
            log.error("웹툰 이름 검색 중 오류 발생: ${e.message}", e)
            // 오류 발생 시 빈 결과 반환
            SearchResponseDto(
                keyword = keyword,
                results = emptyList()
            )
        }
    }
    
    /**
     * 닉네임으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByNickname(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        try {
            // 검색어 인기도 추적
            trackSearchPopularity(keyword)
            
            val pageable: Pageable = PageRequest.of(page, size)
            val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:${page}:${size}"
            
            // 검색어를 닉네임 자동완성 목록에 추가 (비동기)
            autocompleteService.addNicknameToSuggestions(keyword)
            
            // 캐시에서 결과 확인
            val cachedResults = searchCacheService.getFromCache(cacheKey)
            
            if (cachedResults.isPresent) {
                log.info("캐시에서 닉네임 검색 결과 반환: $keyword")
                try {
                    val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
                    return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
                } catch (e: Exception) {
                    log.error("캐시된 닉네임 검색 결과 처리 중 오류 발생: ${e.message}", e)
                    // 캐시 오류 시 DB에서 다시 검색
                }
            }
            
            // 캐시에 없으면 DB에서 검색
            log.info("데이터베이스에서 닉네임 검색 실행: $keyword")
            val searchResults = searchRepository.searchByNickname(keyword, pageable)
            
            // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
            val resultList = searchResults.content
            val isPopular = isPopularSearch(keyword)
            try {
                searchCacheService.cacheResults(cacheKey, resultList, isPopular)
            } catch (e: Exception) {
                log.error("닉네임 검색 결과 캐싱 중 오류 발생: ${e.message}", e)
            }
            
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
        } catch (e: Exception) {
            log.error("닉네임 검색 중 오류 발생: ${e.message}", e)
            // 오류 발생 시 빈 결과 반환
            SearchResponseDto(
                keyword = keyword,
                results = emptyList()
            )
        }
    }
    
    /**
     * 웹툰 이름으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByWebtoonNameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        try {
            // 검색어 인기도 추적
            trackSearchPopularity(keyword)
            
            val pageable: Pageable = PageRequest.of(page, size)
            val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:recommend:${page}:${size}"
            
            // 검색어를 웹툰 자동완성 목록에 추가 (비동기)
            autocompleteService.addWebtoonNameToSuggestions(keyword)
            
            // 캐시에서 결과 확인
            val cachedResults = searchCacheService.getFromCache(cacheKey)
            
            if (cachedResults.isPresent) {
                log.info("캐시에서 웹툰이름 추천수 정렬 검색 결과 반환: $keyword")
                try {
                    val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
                    return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
                } catch (e: Exception) {
                    log.error("캐시된 웹툰이름 검색 결과 처리 중 오류 발생: ${e.message}", e)
                    // 캐시 오류 시 DB에서 다시 검색
                }
            }
            
            // 캐시에 없으면 DB에서 검색
            log.info("데이터베이스에서 웹툰이름 추천수 정렬 검색 실행: $keyword")
            val searchResults = searchRepository.searchByWebtoonNameOrderByRecommendCount(keyword, pageable)
            
            // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
            val resultList = searchResults.content
            val isPopular = isPopularSearch(keyword)
            try {
                searchCacheService.cacheResults(cacheKey, resultList, isPopular)
            } catch (e: Exception) {
                log.error("웹툰이름 검색 결과 캐싱 중 오류 발생: ${e.message}", e)
            }
            
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
        } catch (e: Exception) {
            log.error("웹툰이름 추천수 정렬 검색 중 오류 발생: ${e.message}", e)
            // 오류 발생 시 빈 결과 반환
            SearchResponseDto(
                keyword = keyword,
                results = emptyList()
            )
        }
    }
    
    /**
     * 닉네임으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByNicknameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        try {
            // 검색어 인기도 추적
            trackSearchPopularity(keyword)
            
            val pageable: Pageable = PageRequest.of(page, size)
            val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:recommend:${page}:${size}"
            
            // 검색어를 닉네임 자동완성 목록에 추가 (비동기)
            autocompleteService.addNicknameToSuggestions(keyword)
            
            // 캐시에서 결과 확인
            val cachedResults = searchCacheService.getFromCache(cacheKey)
            
            if (cachedResults.isPresent) {
                log.info("캐시에서 닉네임 추천수 정렬 검색 결과 반환: $keyword")
                try {
                    val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
                    return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
                } catch (e: Exception) {
                    log.error("캐시된 닉네임 검색 결과 처리 중 오류 발생: ${e.message}", e)
                    // 캐시 오류 시 DB에서 다시 검색
                }
            }
            
            // 캐시에 없으면 DB에서 검색
            log.info("데이터베이스에서 닉네임 추천수 정렬 검색 실행: $keyword")
            val searchResults = searchRepository.searchByNicknameOrderByRecommendCount(keyword, pageable)
            
            // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
            val resultList = searchResults.content
            val isPopular = isPopularSearch(keyword)
            try {
                searchCacheService.cacheResults(cacheKey, resultList, isPopular)
            } catch (e: Exception) {
                log.error("닉네임 검색 결과 캐싱 중 오류 발생: ${e.message}", e)
            }
            
            val reviewIds = resultList.mapNotNull { review -> review.reviewId }
            searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
        } catch (e: Exception) {
            log.error("닉네임 추천수 정렬 검색 중 오류 발생: ${e.message}", e)
            // 오류 발생 시 빈 결과 반환
            SearchResponseDto(
                keyword = keyword,
                results = emptyList()
            )
        }
    }
    
    /**
     * 리뷰 내용 및 제목으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByReviewContent(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        // 검색어 인기도 추적
        trackSearchPopularity(keyword)
        
        val pageable: Pageable = PageRequest.of(page, size)
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}review:${keyword}:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가 (비동기)
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults.isPresent) {
            log.info("캐시에서 리뷰 내용 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
            return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 리뷰 내용 검색 실행: $keyword")
        val searchResults = searchRepository.searchByReviewContent(keyword, pageable)
        
        // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
        val resultList = searchResults.content
        val isPopular = isPopularSearch(keyword)
        searchCacheService.cacheResults(cacheKey, resultList, isPopular)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    /**
     * 리뷰 내용 및 제목으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByReviewContentOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto = coroutineScope {
        // 검색어 인기도 추적
        trackSearchPopularity(keyword)
        
        val pageable: Pageable = PageRequest.of(page, size)
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}review:${keyword}:recommend:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가 (비동기)
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults.isPresent) {
            log.info("캐시에서 리뷰 내용 추천수 정렬 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.get().mapNotNull { review -> review.reviewId }
            return@coroutineScope searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults.get(), reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 리뷰 내용 추천수 정렬 검색 실행: $keyword")
        val searchResults = searchRepository.searchByReviewContentOrderByRecommendCount(keyword, pageable)
        
        // 검색 결과를 캐시에 저장 (인기 검색어 여부에 따라 TTL 다르게 적용)
        val resultList = searchResults.content
        val isPopular = isPopularSearch(keyword)
        searchCacheService.cacheResults(cacheKey, resultList, isPopular)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    /**
     * 검색어의 인기도를 추적합니다.
     */
    private fun trackSearchPopularity(keyword: String) {
        val normalizedKeyword = keyword.trim().lowercase()
        searchCountMap.compute(normalizedKeyword) { _, count -> (count ?: 0) + 1 }
    }
    
    /**
     * 인기 검색어인지 확인합니다.
     */
    private fun isPopularSearch(keyword: String): Boolean {
        val normalizedKeyword = keyword.trim().lowercase()
        return (searchCountMap[normalizedKeyword] ?: 0) >= POPULAR_THRESHOLD
}
}