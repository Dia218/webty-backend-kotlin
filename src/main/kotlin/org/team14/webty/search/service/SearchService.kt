package org.team14.webty.search.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.search.constants.SearchConstants
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.enums.SearchType
import org.team14.webty.search.enums.SortType
import org.team14.webty.search.mapper.SearchResponseMapper
import org.team14.webty.search.repository.SearchRepository

@Service
class SearchService(
    private val searchRepository: SearchRepository,
    private val searchCacheService: SearchCacheService,
    private val autocompleteService: AutocompleteService,
    private val searchResponseMapper: SearchResponseMapper
) {
    private val log = LoggerFactory.getLogger(SearchService::class.java)
    private val searchExecutor = SearchExecutor(searchCacheService, searchResponseMapper)

    /**
     * 검색 유형과 정렬 방식에 따라 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun search(
        keyword: String,
        page: Int,
        size: Int,
        searchType: SearchType = SearchType.ALL,
        sortType: SortType = SortType.LATEST
    ): SearchResponseDto = withContext(Dispatchers.IO) {
        // 검색어를 자동완성 목록에 추가 (비동기)
        when (searchType) {
            SearchType.WEBTOON_NAME -> autocompleteService.addWebtoonNameToSuggestions(keyword)
            SearchType.NICKNAME -> autocompleteService.addNicknameToSuggestions(keyword)
            SearchType.REVIEW_CONTENT -> autocompleteService.addSearchKeywordToSuggestions(keyword)
            else -> autocompleteService.addSearchKeywordToSuggestions(keyword)
        }

        // 검색 유형과 정렬 방식에 따라 적절한 검색 메서드 호출
        when (searchType) {
            SearchType.ALL -> {
                when (sortType) {
                    SortType.LATEST -> searchAll(keyword, page, size)
                    SortType.RECOMMEND -> searchAllOrderByRecommendCount(keyword, page, size)
                    SortType.VIEW_COUNT -> searchAllOrderByViewCount(keyword, page, size)
                }
            }

            SearchType.WEBTOON_NAME -> {
                when (sortType) {
                    SortType.LATEST -> searchByWebtoonName(keyword, page, size)
                    SortType.RECOMMEND -> searchByWebtoonNameOrderByRecommendCount(keyword, page, size)
                    else -> searchByWebtoonName(keyword, page, size)
                }
            }

            SearchType.NICKNAME -> {
                when (sortType) {
                    SortType.LATEST -> searchByNickname(keyword, page, size)
                    SortType.RECOMMEND -> searchByNicknameOrderByRecommendCount(keyword, page, size)
                    else -> searchByNickname(keyword, page, size)
                }
            }

            SearchType.REVIEW_CONTENT -> {
                when (sortType) {
                    SortType.LATEST -> searchByReviewContent(keyword, page, size)
                    SortType.RECOMMEND -> searchByReviewContentOrderByRecommendCount(keyword, page, size)
                    else -> searchByReviewContent(keyword, page, size)
                }
            }
        }
    }

    /**
     * 일반 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchAll(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}${keyword}:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByKeyword(k, p)
        }
    }

    /**
     * 추천수 기준으로 정렬된 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchAllOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}${keyword}:recommend:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByKeywordOrderByRecommendCount(k, p)
        }
    }

    /**
     * 조회수 기준으로 정렬된 검색을 수행합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchAllOrderByViewCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}${keyword}:viewCount:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByKeywordOrderByViewCount(k, p)
        }
    }

    /**
     * 웹툰 이름으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByWebtoonName(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByWebtoonName(k, p)
        }
    }

    /**
     * 닉네임으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByNickname(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByNickname(k, p)
        }
    }

    /**
     * 웹툰 이름으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByWebtoonNameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}webtoon:${keyword}:recommend:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByWebtoonNameOrderByRecommendCount(k, p)
        }
    }

    /**
     * 닉네임으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByNicknameOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}nickname:${keyword}:recommend:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByNicknameOrderByRecommendCount(k, p)
        }
    }

    /**
     * 리뷰 내용 및 제목으로 검색합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByReviewContent(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}review:${keyword}:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByReviewContent(k, p)
        }
    }

    /**
     * 리뷰 내용 및 제목으로 검색하고 추천수로 정렬합니다.
     */
    @Transactional(readOnly = true)
    suspend fun searchByReviewContentOrderByRecommendCount(keyword: String, page: Int, size: Int): SearchResponseDto {
        val cacheKey = "${SearchConstants.SEARCH_CACHE_KEY_PREFIX}review:${keyword}:recommend:${page}:${size}"
        return searchExecutor.executeSearch(keyword, page, size, cacheKey) { k, p ->
            searchRepository.searchByReviewContentOrderByRecommendCount(k, p)
        }
    }
}