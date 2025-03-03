package org.team14.webty.review.dto

import org.team14.webty.review.enums.SpoilerStatus
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.webtoon.dto.WebtoonSummaryDto

data class ReviewItemResponse(
    val reviewId: Long,
    val userDataResponse: UserDataResponse, // 사용자 프로필, 닉네임
    val content: String,
    val title: String,
    val viewCount: Int,
    val spoilerStatus: SpoilerStatus,
    val webtoon: WebtoonSummaryDto,
    val imageUrls: List<String>? = null,
    val commentCount: Long,
    val recommendCount: Long
)