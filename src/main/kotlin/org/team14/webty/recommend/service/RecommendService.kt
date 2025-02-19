package org.team14.webty.recommend.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.recommend.entity.Recommend
import org.team14.webty.recommend.enumerate.LikeType
import org.team14.webty.recommend.repository.RecommendRepository
import org.team14.webty.review.entity.Review
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.security.authentication.WebtyUserDetails

@Service
class RecommendService(
    private val reviewRepository: ReviewRepository,
    private val recommendRepository: RecommendRepository
) {

    @Transactional
    fun createRecommend(webtyUserDetails: WebtyUserDetails, reviewId: Long, type: String?): Long? {
        val webtyUser = webtyUserDetails.webtyUser
        val review = reviewIdToReview(reviewId)

        if (recommendRepository.existsByReviewAndUserIdAndLikeType(
                review, webtyUser.userId, LikeType.fromString(type!!)
            )
        ) {
            throw BusinessException(ErrorCode.RECOMMEND_DUPLICATION_ERROR)
        }
        val recommend = Recommend(
            voteId = null,
            likeType = LikeType.fromString(type),
            userId = webtyUser.userId,
            review = review,
        )
        recommendRepository.save(recommend)
        return recommend.voteId
    }

    @Transactional
    fun deleteRecommend(webtyUserDetails: WebtyUserDetails, reviewId: Long, type: String) {
        val webtyUser = webtyUserDetails.webtyUser
        val review = reviewIdToReview(reviewId)
        val recommend = recommendRepository
            .findByReviewAndUserIdAndLikeType(review, webtyUser.userId, LikeType.fromString(type))
            ?: throw BusinessException(ErrorCode.RECOMMEND_NOT_FOUND)
        recommendRepository.delete(recommend)
    }

    fun getRecommendCounts(reviewId: Long): Map<String, Long> {
        return recommendRepository.getRecommendCounts(reviewId)
    }

    private fun reviewIdToReview(reviewId: Long): Review {
        return reviewRepository.findById(reviewId)
            .orElseThrow { BusinessException(ErrorCode.REVIEW_NOT_FOUND) }
    }

    fun isRecommended(webtyUserDetails: WebtyUserDetails, reviewId: Long): Map<String, Boolean> {
        val webtyUser = webtyUserDetails.webtyUser
        val rawResult = recommendRepository.findRecommendStatusByUserAndReview(webtyUser.userId, reviewId)

        return rawResult.mapValues { it.value == 1 } // key값은 유지 value값만 변경
    }
}
