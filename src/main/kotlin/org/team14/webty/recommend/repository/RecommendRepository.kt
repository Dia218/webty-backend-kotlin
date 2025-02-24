package org.team14.webty.recommend.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.team14.webty.recommend.entity.Recommend
import org.team14.webty.recommend.enums.LikeType
import org.team14.webty.review.entity.Review

interface RecommendRepository : JpaRepository<Recommend, Long> {
    fun existsByReviewAndUserIdAndLikeType(review: Review, userId: Long, likeType: LikeType): Boolean
    fun findByReviewAndUserIdAndLikeType(review: Review, userId: Long, likeType: LikeType): Recommend?

    @Query(
        ("SELECT new map( " +
                "COALESCE(SUM(CASE WHEN r.likeType = 'LIKE' THEN 1 ELSE 0 END), 0) AS likes, " +
                "COALESCE(SUM(CASE WHEN r.likeType = 'HATE' THEN 1 ELSE 0 END), 0) AS hates) " +
                "FROM Recommend r WHERE r.review.reviewId = :reviewId")
    )
    fun getRecommendCounts(@Param("reviewId") reviewId: Long?): Map<String, Long>

    @Query(
        """
        SELECT r FROM Review r
        JOIN FETCH r.user
        JOIN FETCH r.webtoon
        WHERE r.reviewId IN (SELECT rec.review.reviewId FROM Recommend rec WHERE rec.userId = :userId)
        ORDER BY (SELECT MAX(rec.recommendId) FROM Recommend rec WHERE rec.review.reviewId = r.reviewId) DESC
        """
    )
    fun getUserRecommendReview(@Param("userId") userId: Long, pageable: Pageable): Page<Review>

    @Query(
        ("SELECT COALESCE(COUNT(r.recommendId), 0) " +
                "FROM Review rv LEFT JOIN Recommend r ON rv.reviewId = r.review.reviewId AND r.likeType = 'LIKE' " +
                "WHERE rv.reviewId IN :reviewIds " +
                "GROUP BY rv.reviewId ORDER BY rv.reviewId DESC")
    )
    fun getLikeCounts(@Param("reviewIds") reviewIds: List<Long>): List<Long>

    @Query(
        value = """
        SELECT
            COALESCE(MAX(CASE WHEN r.like_type = 'LIKE' THEN 1 ELSE 0 END), 0) AS likes,
            COALESCE(MAX(CASE WHEN r.like_type = 'HATE' THEN 1 ELSE 0 END), 0) AS hates
        FROM recommend r
        WHERE r.user_id = :userId AND r.review_id = :reviewId
        """,
        nativeQuery = true
    )
    fun findRecommendStatusByUserAndReview(
        @Param("userId") userId: Long,
        @Param("reviewId") reviewId: Long
    ): Map<String, Int>

}