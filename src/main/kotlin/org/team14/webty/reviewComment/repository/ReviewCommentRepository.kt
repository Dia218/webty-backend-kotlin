// 리뷰 댓글 레포지토리 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.repository

// 필요한 Spring Data JPA 관련 클래스들을 임포트
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.reviewComment.entity.ReviewComment

// 이 인터페이스가 레포지토리임을 나타내는 어노테이션
@Repository
// JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받음
// ReviewComment 엔티티에 대한 레포지토리이며, ID 타입은 Long
interface ReviewCommentRepository : JpaRepository<ReviewComment, Long> {

    // 특정 리뷰의 모든 댓글을 depth 오름차순, commentId 내림차순으로 조회
    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
        "ORDER BY rc.depth ASC, rc.commentId DESC")
    fun findAllByReviewIdOrderByDepthAndCommentId(
        // reviewId 파라미터 정의
        @Param("reviewId") reviewId: Long,
        // 페이지네이션 정보
        pageable: Pageable
    ): Page<ReviewComment>

    // 특정 부모 댓글의 모든 대댓글을 commentId 오름차순으로 조회
    fun findByParentIdOrderByCommentIdAsc(parentId: Long): List<ReviewComment>

    // 특정 리뷰의 모든 댓글을 계층 구조로 정렬하여 조회
    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
        "ORDER BY CASE WHEN rc.depth = 0 THEN rc.commentId " +
        "ELSE (SELECT p.commentId FROM ReviewComment p WHERE p.commentId = rc.parentId) END DESC, " +
        "rc.depth ASC, rc.commentId DESC")
    fun findAllByReviewIdOrderByParentCommentIdAndDepth(
        @Param("reviewId") reviewId: Long
    ): List<ReviewComment>

    // 여러 리뷰의 모든 댓글을 commentId 내림차순으로 조회
    @Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId IN :reviewIds ORDER BY rc.commentId DESC")
    fun findAllByReviewIds(
        @Param("reviewIds") reviewIds: List<Long>
    ): List<ReviewComment>
}