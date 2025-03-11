package org.team14.webty.search.mapper

import org.springframework.stereotype.Component
import org.team14.webty.review.cache.ViewCountCacheService
import org.team14.webty.review.entity.Review
import org.team14.webty.search.dto.SearchResponseDto
import org.team14.webty.search.mapper.SearchReviewMapper
import org.team14.webty.search.mapper.SearchRecommendMapper

@Component
class SearchResponseMapper(
    private val reviewMapper: SearchReviewMapper,
    private val recommendMapper: SearchRecommendMapper,
    private val viewCountCacheService: ViewCountCacheService
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
        val likesCountsMap = recommendMapper.getLikesCounts(reviewIds)
        
        val viewCountsMap = viewCountCacheService.getCurrentViewCounts(reviewIds)
        
        val reviewsWithLikes = reviews.map { review ->
            val recommendCount = review.reviewId?.let { id -> likesCountsMap[id]?.toLong() } ?: 0L
            
            val latestViewCount = review.reviewId?.let { id -> 
                viewCountsMap[id] ?: review.viewCount
            } ?: review.viewCount
            
            reviewMapper.convertToReviewItemResponse(
                review = review,
                recommendCount = recommendCount,
                overrideViewCount = latestViewCount
            )
        }
        
        return SearchResponseDto(
            keyword = keyword,
            results = reviewsWithLikes
        )
    }
} 