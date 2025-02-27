package org.team14.webty.search.dto

// 자동완성 항목 하나에 대한 상세 정보를 담는 DTO 클래스입니다.

data class SuggestionItemDto(
    // 자동완성 텍스트를 저장합니다.
    val text: String,
    // 자동완성 타입을 저장합니다("webtoonName", "nickname" 등).
    val type: String? = null,
    // 검색 URL을 저장합니다(정렬 방식 포함 가능).
    val searchUrl: String = "/search"
)