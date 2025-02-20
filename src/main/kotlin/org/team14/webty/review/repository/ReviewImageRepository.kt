package org.team14.webty.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.review.entity.Review
import org.team14.webty.review.entity.ReviewImage

@Repository
interface ReviewImageRepository : JpaRepository<ReviewImage?, Long?> {
    fun findAllByReview(review: Review?): List<ReviewImage>
    
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId IN :reviewIds")
    fun findByReviewIdIn(@Param("reviewIds") reviewIds: List<Long>?): List<ReviewImage>
}
