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

        recommendRepository.existsByReviewAndUserIdAndLikeType(
            review, webtyUser.userId, LikeType.fromString(type!!)
        ).takeIf { it }?.let { // true 이면 Exception 던짐
            throw BusinessException(ErrorCode.RECOMMEND_DUPLICATION_ERROR)
        }

        return Recommend(
            likeType = LikeType.fromString(type),
            userId = webtyUser.userId,
            review = review
        ).also { recommendRepository.save(it) }.voteId
    }

    @Transactional
    fun deleteRecommend(webtyUserDetails: WebtyUserDetails, reviewId: Long, type: String) {
        val webtyUser = webtyUserDetails.webtyUser
        val review = reviewIdToReview(reviewId)

        recommendRepository.findByReviewAndUserIdAndLikeType(
            review, webtyUser.userId, LikeType.fromString(type)
        )?.let { recommendRepository.delete(it) }
            ?: throw BusinessException(ErrorCode.RECOMMEND_NOT_FOUND)
    }

    fun getRecommendCounts(reviewId: Long): Map<String, Long> {
        return recommendRepository.getRecommendCounts(reviewId)
    }

    private fun reviewIdToReview(reviewId: Long): Review = runCatching {
        reviewRepository.findById(reviewId).get()
    }.getOrElse {
        throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
    }

    fun isRecommended(webtyUserDetails: WebtyUserDetails, reviewId: Long): Map<String, Boolean> {
        return recommendRepository.findRecommendStatusByUserAndReview(webtyUserDetails.webtyUser.userId, reviewId)
            .mapValues { it.value == 1 }
    }
}
