package org.team14.webty.search.mapper

import org.springframework.stereotype.Component
import org.team14.webty.review.entity.Review
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.webtoon.dto.WebtoonSummaryDto

@Component
class SearchReviewMapper {
    /**
     * 리뷰 엔티티를 ReviewItemResponse DTO로 변환합니다.
     * 
     * @param review 변환할 리뷰 엔티티
     * @param recommendCount 좋아요 수
     * @return 변환된 ReviewItemResponse 객체
     */
    fun convertToReviewItemResponse(review: Review, recommendCount: Long = 0): ReviewItemResponse {
        return ReviewItemResponse(
            reviewId = review.reviewId ?: 0L,
            userDataResponse = convertToUserDataResponse(review),
            content = review.content,
            title = review.title,
            viewCount = review.viewCount,
            spoilerStatus = review.isSpoiler,
            webtoon = convertToWebtoonSummaryDto(review),
            imageUrls = getImageUrls(review),
            commentCount = getCommentCount(review),
            recommendCount = recommendCount
        )
    }
    
    // 사용자 정보를 UserDataResponse로 변환
    fun convertToUserDataResponse(review: Review): UserDataResponse {
        return UserDataResponse(
            userid = review.user.userId ?: 0,
            nickname = review.user.nickname,
            profileImage = review.user.profileImage
        )
    }
    
    // 웹툰 정보를 WebtoonSummaryDto로 변환
    fun convertToWebtoonSummaryDto(review: Review): WebtoonSummaryDto {
        return WebtoonSummaryDto(
            webtoonId = review.webtoon.webtoonId,
            webtoonName = review.webtoon.webtoonName,
            thumbnailUrl = review.webtoon.thumbnailUrl
        )
    }
    
    fun getImageUrls(review: Review): List<String>? {
        return null // 임시로 null 반환
    }
    
    fun getCommentCount(review: Review): Int {
        return 0 // 임시로 0 반환
    }
} 