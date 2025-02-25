package org.team14.webty.common.search.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.search.dto.SearchResponseDto
import org.team14.webty.common.search.repository.SearchRepository
import org.team14.webty.common.search.mapper.SearchResponseMapper

@Service
class SearchService(
    private val searchRepository: SearchRepository,
    private val searchCacheService: SearchCacheService,
    private val autocompleteService: AutocompleteService,
    private val searchResponseMapper: SearchResponseMapper
) {
    private val log = LoggerFactory.getLogger(SearchService::class.java)
    
    companion object {
        const val SEARCH_CACHE_KEY_PREFIX = "search:"
        const val SEARCH_SUGGESTION_KEY = "search:suggestions"
        const val WEBTOON_SUGGESTION_KEY = "search:webtoon:suggestions"
        const val NICKNAME_SUGGESTION_KEY = "search:nickname:suggestions"
        const val SEARCH_CACHE_TTL_MINUTES = 10L
        const val MAX_SUGGESTIONS = 10L
    }
    
    @Transactional(readOnly = true)
    fun search(keyword: String, page: Int, size: Int): SearchResponseDto {
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키 생성
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}${keyword}:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults != null) {
            log.info("캐시에서 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 검색 실행: $keyword")
        val searchResults = searchRepository.searchByKeyword(keyword, pageable)
        
        // 검색 결과를 캐시에 저장
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    @Transactional(readOnly = true)
    fun searchOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키 생성 - 추천순 정렬은 다른 캐시 키 사용
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}${keyword}:recommend:${page}:${size}"
        
        // 검색어를 자동완성 목록에 추가
        autocompleteService.addSearchKeywordToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults != null) {
            log.info("캐시에서 추천수 정렬 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 추천수 정렬 검색 실행: $keyword")
        val searchResults = searchRepository.searchByKeywordOrderByRecommendCount(keyword, pageable)
        
        // 검색 결과를 캐시에 저장
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    @Transactional(readOnly = true)
    fun searchByWebtoonName(keyword: String, page: Int, size: Int): SearchResponseDto {
        // 페이지 객체를 생성합니다.
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키를 생성합니다.
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:${page}:${size}"
        
        // 검색어를 웹툰 이름 자동완성 목록에 추가합니다.
        autocompleteService.addWebtoonNameToSuggestions(keyword)
        
        // Redis 캐시에서 결과를 찾아봅니다.
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        // 캐시에 결과가 있으면 캐시에서 반환합니다.
        if (cachedResults != null) {
            log.info("캐시에서 웹툰이름 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 결과가 없으면 데이터베이스에서 검색합니다.
        log.info("데이터베이스에서 웹툰이름 검색 실행: $keyword")
        val searchResults = searchRepository.searchByWebtoonName(keyword, pageable)
        
        // 검색 결과를 캐시에 저장합니다.
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        // 검색 결과를 응답 객체로 변환하여 반환합니다.
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    @Transactional(readOnly = true)
    fun searchByNickname(keyword: String, page: Int, size: Int): SearchResponseDto {
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키 생성
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:${page}:${size}"
        
        // 검색어를 닉네임 자동완성 목록에 추가
        autocompleteService.addNicknameToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults != null) {
            log.info("캐시에서 닉네임 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 닉네임 검색 실행: $keyword")
        val searchResults = searchRepository.searchByNickname(keyword, pageable)
        
        // 검색 결과를 캐시에 저장
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    @Transactional(readOnly = true)
    fun searchByWebtoonNameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키 생성
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:recommend:${page}:${size}"
        
        // 검색어를 웹툰 자동완성 목록에 추가
        autocompleteService.addWebtoonNameToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults != null) {
            log.info("캐시에서 웹툰이름 추천수 정렬 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 웹툰이름 추천수 정렬 검색 실행: $keyword")
        val searchResults = searchRepository.searchByWebtoonNameOrderByRecommendCount(keyword, pageable)
        
        // 검색 결과를 캐시에 저장
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
    
    @Transactional(readOnly = true)
    fun searchByNicknameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val pageable: Pageable = PageRequest.of(page, size)
        
        // 캐시 키 생성
        val cacheKey = "${SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:recommend:${page}:${size}"
        
        // 검색어를 닉네임 자동완성 목록에 추가
        autocompleteService.addNicknameToSuggestions(keyword)
        
        // 캐시에서 결과 확인
        val cachedResults = searchCacheService.getFromCache(cacheKey)
        
        if (cachedResults != null) {
            log.info("캐시에서 닉네임 추천수 정렬 검색 결과 반환: $keyword")
            val reviewIds = cachedResults.mapNotNull { review -> review.reviewId }
            return searchResponseMapper.buildSearchResponseFromReviews(keyword, cachedResults, reviewIds)
        }
        
        // 캐시에 없으면 DB에서 검색
        log.info("데이터베이스에서 닉네임 추천수 정렬 검색 실행: $keyword")
        val searchResults = searchRepository.searchByNicknameOrderByRecommendCount(keyword, pageable)
        
        // 검색 결과를 캐시에 저장
        val resultList = searchResults.content
        searchCacheService.cacheResults(cacheKey, resultList)
        
        val reviewIds = resultList.mapNotNull { review -> review.reviewId }
        return searchResponseMapper.buildSearchResponseFromReviews(keyword, resultList, reviewIds)
    }
}