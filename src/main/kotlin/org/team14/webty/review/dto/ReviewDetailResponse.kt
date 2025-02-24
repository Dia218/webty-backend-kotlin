package org.team14.webty.review.dto

import org.team14.webty.common.dto.PageDto
import org.team14.webty.review.enums.SpoilerStatus
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.webtoon.dto.WebtoonSummaryDto
import java.time.LocalDateTime

data class ReviewDetailResponse(
    val reviewId: Long,
    val userDataResponse: UserDataResponse, // 사용자 프로필, 닉네임
    val webtoon: WebtoonSummaryDto,
    val content: String,
    val title: String,
    val viewCount: Int,
    val spoilerStatus: SpoilerStatus,
    val imageUrls: List<String>? = null,
    val commentResponses: PageDto<CommentResponse>? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
    val recommendCount: Map<String, Long>
)
