package org.team14.webty.search.dto

import org.team14.webty.review.dto.ReviewItemResponse

// 검색 결과 응답 DTO 클래스입니다.
data class SearchResponseDto(
    // 검색어를 저장합니다.
    val keyword: String,
    // 검색 결과(리뷰 목록)를 저장합니다.
    val results: List<ReviewItemResponse>
)

// 검색어 자동완성 응답 DTO 클래스입니다.
data class SearchSuggestionDto(
    // 자동완성 목록을 저장합니다.
    val suggestions: List<String>,
    // 검색 URL을 저장합니다(정렬 방식 정보 포함).
    val searchUrl: String = "/search" // 기본 검색 URL
)

// 자동완성 항목 하나에 대한 상세 정보를 담는 DTO 클래스입니다.
data class SuggestionItemDto(
    // 자동완성 텍스트를 저장합니다.
    val text: String,
    // 자동완성 타입을 저장합니다("webtoonName", "nickname" 등).
    val type: String? = null,
    // 검색 URL을 저장합니다(정렬 방식 포함 가능).
    val searchUrl: String = "/search"
)