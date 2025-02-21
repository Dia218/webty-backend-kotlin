package org.team14.webty.review.mapper

import org.team14.webty.common.dto.PageDto
import org.team14.webty.review.dto.ReviewDetailResponse
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.review.dto.ReviewRequest
import org.team14.webty.review.entity.Review
import org.team14.webty.review.entity.ReviewImage
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.mapper.UserDataResponseMapper
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper
import java.time.LocalDateTime

object ReviewMapper {
    fun toEntity(request: ReviewRequest, webtyUser: WebtyUser, webtoon: Webtoon): Review {
        return Review(
            user = webtyUser,
            isSpoiler = request.spoilerStatus,
            content = request.content,
            title = request.title,
            viewCount = 0,
            webtoon = webtoon
        )
    }

    fun toResponse(
        review: Review, comments: List<CommentResponse>,
        imageUrls: List<String>?, likeCount: Long
    ): ReviewItemResponse {
        return ReviewItemResponse(
            reviewId = review.reviewId!!,
            userDataResponse = UserDataResponseMapper.toDto(review.user),
            content = review.content,
            title = review.title,
            viewCount = review.viewCount,
            spoilerStatus = review.isSpoiler,
            webtoon = WebtoonApiResponseMapper.toSummaryDto(review.webtoon),
            imageUrls = imageUrls,
            commentCount = comments.size,
            recommendCount = likeCount
        )
    }

    fun toDetail(
        review: Review, comments: PageDto<CommentResponse>?,
        reviewImages: List<ReviewImage?>, recommendCount: Map<String, Long>
    ): ReviewDetailResponse {
        return ReviewDetailResponse(
            reviewId = review.reviewId!!,
            userDataResponse = UserDataResponseMapper.toDto(review.user),
            webtoon = WebtoonApiResponseMapper.toSummaryDto(review.webtoon),
            content = review.content,
            title = review.title,
            viewCount = review.viewCount,
            spoilerStatus = review.isSpoiler,
            imageUrls = reviewImages.mapNotNull { it?.imageUrl },
            commentResponses = comments,
            createdAt = review.createdAt ?: LocalDateTime.now(),
            updatedAt = review.modifiedAt,
            recommendCount = recommendCount
        )
    }

    fun toImageEntity(imageUrl: String, review: Review): ReviewImage {
        return ReviewImage(
            imageUrl = imageUrl,
            review = review
        )
    }
}
