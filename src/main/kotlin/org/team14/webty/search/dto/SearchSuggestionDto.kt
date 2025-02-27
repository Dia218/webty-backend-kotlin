package org.team14.webty.search.dto

// 검색어 자동완성 응답 DTO 클래스입니다.
data class SearchSuggestionDto(
    // 자동완성 목록을 저장합니다.
    val suggestions: List<String>,
    // 검색 URL을 저장합니다(정렬 방식 정보 포함).
    val searchUrl: String = "/search" // 기본 검색 URL
)
