package org.team14.webty.reviewComment.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.reviewComment.entity.ReviewComment

@Repository
interface ReviewCommentRepository : JpaRepository<ReviewComment, Long> {
    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
        "ORDER BY rc.depth ASC, rc.commentId DESC")
    fun findAllByReviewIdOrderByDepthAndCommentId(
        @Param("reviewId") reviewId: Long,
        pageable: Pageable
    ): Page<ReviewComment>

    fun findByParentIdOrderByCommentIdAsc(parentId: Long): List<ReviewComment>

    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
        "ORDER BY CASE WHEN rc.depth = 0 THEN rc.commentId " +
        "ELSE (SELECT p.commentId FROM ReviewComment p WHERE p.commentId = rc.parentId) END DESC, " +
        "rc.depth ASC, rc.commentId DESC")
    fun findAllByReviewIdOrderByParentCommentIdAndDepth(@Param("reviewId") reviewId: Long): List<ReviewComment>

    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId IN :reviewIds ORDER BY rc.commentId DESC")
    fun findAllByReviewIds(@Param("reviewIds") reviewIds: List<Long>): List<ReviewComment>
}