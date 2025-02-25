package org.team14.webty.common.search.mapper

import org.springframework.stereotype.Component
import org.team14.webty.recommend.repository.RecommendRepository
import org.team14.webty.recommend.enums.LikeType
import org.team14.webty.review.entity.Review
import org.team14.webty.common.search.dto.SearchResponseDto
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.webtoon.dto.WebtoonSummaryDto
import org.team14.webty.review.entity.ReviewImage
import org.team14.webty.review.enums.SpoilerStatus

@Component
class SearchResponseMapper(
    private val recommendRepository: RecommendRepository
) {
    /**
     * 리뷰 리스트와 추가 데이터로부터 검색 응답 객체를 구성합니다.
     * 
     * @param keyword 검색 키워드
     * @param reviews 검색된 리뷰 리스트
     * @param reviewIds 리뷰 ID 리스트 (좋아요 카운트를 위해 사용)
     * @return 완성된 검색 응답 DTO
     */
    fun buildSearchResponseFromReviews(
        keyword: String, 
        reviews: List<Review>,
        reviewIds: List<Long>
    ): SearchResponseDto {
        val likesCountsMap = getLikesCounts(reviewIds)
        
        val reviewsWithLikes = reviews.map { review ->
            ReviewItemResponse(
                reviewId = review.reviewId ?: 0L,
                userDataResponse = convertToUserDataResponse(review),
                content = review.content,
                title = review.title,
                viewCount = review.viewCount,
                spoilerStatus = review.isSpoiler,
                webtoon = convertToWebtoonSummaryDto(review),
                imageUrls = getImageUrls(review),
                commentCount = getCommentCount(review),
                recommendCount = review.reviewId?.let { id -> likesCountsMap[id]?.toLong() } ?: 0L
            )
        }
        
        return SearchResponseDto(
            keyword = keyword,
            results = reviewsWithLikes
        )
    }
    
    /**
     * 리뷰 ID 목록에 대한 좋아요 수를 조회합니다.
     * 
     * @param reviewIds 좋아요 수를 조회할 리뷰 ID 리스트
     * @return 리뷰 ID를 키로, 좋아요 수를 값으로 하는 맵
     */
    private fun getLikesCounts(reviewIds: List<Long>): Map<Long, Int> {
        if (reviewIds.isEmpty()) {
            return emptyMap()
        }
        
        return reviewIds.associateWith { reviewId ->
            val counts = recommendRepository.getRecommendCounts(reviewId)
            counts["likes"]?.toInt() ?: 0
        }
    }
    
    // 사용자 정보를 UserDataResponse로 변환
    private fun convertToUserDataResponse(review: Review): UserDataResponse {
        return UserDataResponse(
            userid = review.user.userId ?: 0,
            nickname = review.user.nickname,
            profileImage = review.user.profileImage ?: ""
        )
    }
    
    // 웹툰 정보를 WebtoonSummaryDto로 변환
    private fun convertToWebtoonSummaryDto(review: Review): WebtoonSummaryDto {
        return WebtoonSummaryDto(
            webtoonId = review.webtoon.webtoonId,
            webtoonName = review.webtoon.webtoonName,
            thumbnailUrl = review.webtoon.thumbnailUrl ?: ""
        )
    }
    
    private fun getImageUrls(review: Review): List<String>? {
        return null // 임시로 null 반환
    }
    
    private fun getCommentCount(review: Review): Int {
        return 0 // 임시로 0 반환
    }
} 