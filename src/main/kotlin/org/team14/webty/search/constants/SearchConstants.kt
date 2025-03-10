package org.team14.webty.search.constants

/**
 * 검색 관련 상수들을 정의합니다.
 */
object SearchConstants {
    // 캐시 관련 상수
    const val SEARCH_CACHE_KEY_PREFIX = "search:"
    const val SEARCH_CACHE_TTL_MINUTES = 0L  // 캐시 TTL을 0으로 설정하여 캐싱 비활성화
    const val POPULAR_CACHE_TTL_HOURS = 0L   // 캐시 TTL을 0으로 설정하여 캐싱 비활성화
    const val NORMAL_CACHE_TTL_HOURS = 0L    // 캐시 TTL을 0으로 설정하여 캐싱 비활성화
    const val COMPRESSION_THRESHOLD = 1024 // 1KB
    
    // 자동완성 관련 상수
    const val SEARCH_SUGGESTION_KEY = "search:suggestions"
    const val WEBTOON_SUGGESTION_KEY = "search:webtoon:suggestions"
    const val NICKNAME_SUGGESTION_KEY = "search:nickname:suggestions"
    const val REVIEW_CONTENT_SUGGESTION_KEY = "search:review:suggestions"
    const val MAX_SUGGESTIONS = 10
    const val MIN_PREFIX_LENGTH = 1  // 한 글자(초성)만으로도 검색 가능하도록 변경
    
    // 인기도 관련 상수
    const val POPULAR_THRESHOLD = 5
    const val POPULAR_SUGGESTION_THRESHOLD = 3
    
    // 유사도 관련 상수
    const val DEFAULT_MIN_MATCH_SCORE = 0.5  // 기본 최소 유사도 점수
    
    // 한국어 초성 관련 상수
    val KOREAN_CONSONANTS = arrayOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", 
        "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )
    
    // 초성 추출을 위한 상수
    const val HANGUL_BASE = 0xAC00     // 한글 유니코드 시작 값 (가)
    const val HANGUL_END = 0xD7A3      // 한글 유니코드 끝 값 (힣)
    const val CONSONANT_BASE = 0x1100  // 한글 자음 시작 값 (ㄱ)
    const val VOWEL_BASE = 0x1161      // 한글 모음 시작 값 (ㅏ)
    const val CONSONANTS_COUNT = 19    // 초성 개수
    const val VOWELS_COUNT = 21        // 중성 개수
    const val FINAL_COUNT = 28         // 종성 개수 (없음 포함)
    const val FINAL_BASE = 0x11A7      // 한글 종성 시작 값 (ㄱ)
} 