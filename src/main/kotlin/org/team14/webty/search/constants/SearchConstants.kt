package org.team14.webty.search.constants

/**
 * 검색 관련 상수들을 정의합니다.
 */
object SearchConstants {
    // 캐시 관련 상수
    const val SEARCH_CACHE_KEY_PREFIX = "search:"
    const val SEARCH_CACHE_TTL_MINUTES = 10L
    const val POPULAR_CACHE_TTL_HOURS = 48L
    const val NORMAL_CACHE_TTL_HOURS = 24L
    const val COMPRESSION_THRESHOLD = 1024 // 1KB
    
    // 자동완성 관련 상수
    const val SEARCH_SUGGESTION_KEY = "search:suggestions"
    const val WEBTOON_SUGGESTION_KEY = "search:webtoon:suggestions"
    const val NICKNAME_SUGGESTION_KEY = "search:nickname:suggestions"
    const val REVIEW_CONTENT_SUGGESTION_KEY = "search:review:suggestions"
    const val MAX_SUGGESTIONS = 10
    const val MIN_PREFIX_LENGTH = 2
    
    // 인기도 관련 상수
    const val POPULAR_THRESHOLD = 5
    const val POPULAR_SUGGESTION_THRESHOLD = 3
} 