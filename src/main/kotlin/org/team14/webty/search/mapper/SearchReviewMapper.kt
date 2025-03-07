package org.team14.webty.search.mapper

import org.springframework.stereotype.Component
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.review.entity.Review
import org.team14.webty.review.mapper.ReviewMapper
import org.team14.webty.review.repository.ReviewImageRepository
import org.team14.webty.reviewComment.repository.ReviewCommentRepository

@Component
class SearchReviewMapper(
    private val reviewImageRepository: ReviewImageRepository,
    private val reviewCommentRepository: ReviewCommentRepository
) {
    /**
     * 리뷰 엔티티를 ReviewItemResponse DTO로 변환합니다.
     * ReviewMapper를 활용하여 중복 코드를 제거합니다.
     * 
     * @param review 변환할 리뷰 엔티티
     * @param recommendCount 좋아요 수
     * @param loadImages 이미지를 로드할지 여부 (기본값: false)
     * @return 변환된 ReviewItemResponse 객체
     */
    fun convertToReviewItemResponse(
        review: Review, 
        recommendCount: Long = 0,
        loadImages: Boolean = false
    ): ReviewItemResponse {
        // 이미지 URL 목록 (필요한 경우에만 로드)
        val imageUrls = if (loadImages) {
            reviewImageRepository.findAllByReview(review)
                .mapNotNull { it?.imageUrl }
        } else {
            null
        }
        
        // 댓글 개수 가져오기
        val commentCount = review.reviewId?.let { reviewId ->
            reviewCommentRepository.countByReviewIds(listOf(reviewId))
                .firstOrNull()
                ?.let { it[1] as Long }
                ?: 0L
        } ?: 0L
        
        // ReviewMapper의 toResponse 메서드를 활용
        return ReviewMapper.toResponse(
            review = review,
            commentCount = commentCount, // 정확한 댓글 개수 사용
            imageUrls = imageUrls,
            likeCount = recommendCount
        )
    }
}