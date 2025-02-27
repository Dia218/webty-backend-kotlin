package org.team14.webty.search.dto

import org.team14.webty.review.dto.ReviewItemResponse

// 검색 결과 응답 DTO 클래스입니다.
data class SearchResponseDto(
    // 검색어를 저장합니다.
    val keyword: String,
    // 검색 결과(리뷰 목록)를 저장합니다.
    val results: List<ReviewItemResponse>
)