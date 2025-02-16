package org.team14.webty.reviewComment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.team14.webty.reviewComment.entity.ReviewComment;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
	// 특정 리뷰의 모든 댓글을 depth와 생성일시 순으로 조회
	@Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
		"ORDER BY rc.depth ASC, rc.commentId DESC")
	Page<ReviewComment> findAllByReviewIdOrderByDepthAndCommentId(
		@Param("reviewId") Long reviewId,
		Pageable pageable
	);

	// 특정 댓글의 대댓글 목록 조회
	List<ReviewComment> findByParentIdOrderByCommentIdAsc(Long parentId);

	// 루트 댓글만 조회 (depth = 0)
	@Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId AND rc.depth = 0 " +
		"ORDER BY rc.commentId DESC")
	List<ReviewComment> findRootComments(@Param("reviewId") Long reviewId);

	// 특정 댓글의 모든 하위 댓글 조회 (재귀적으로)
	@Query(value = """
		WITH RECURSIVE CommentHierarchy AS (
			SELECT * FROM review_comment 
			WHERE comment_id = :commentId 
			UNION ALL 
			SELECT c.* FROM review_comment c 
			INNER JOIN CommentHierarchy ch ON c.parent_id = ch.comment_id
		) 
		SELECT * FROM CommentHierarchy
		""",
		nativeQuery = true)
	List<ReviewComment> findAllChildComments(@Param("commentId") Long commentId);

	// 특정 depth의 댓글만 조회
	@Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId AND rc.depth = :depth")
	List<ReviewComment> findByReviewIdAndDepth(@Param("reviewId") Long reviewId, @Param("depth") Integer depth);

	@Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId = :reviewId " +
		"ORDER BY CASE WHEN rc.depth = 0 THEN rc.commentId " +
		"ELSE (SELECT p.commentId FROM ReviewComment p WHERE p.commentId = rc.parentId) END DESC, " +
		"rc.depth ASC, rc.commentId DESC")
	List<ReviewComment> findAllByReviewIdOrderByParentCommentIdAndDepth(@Param("reviewId") Long reviewId);

	@Query("SELECT rc FROM ReviewComment rc WHERE rc.review.reviewId IN :reviewIds ORDER BY rc.commentId DESC")
	List<ReviewComment> findAllByReviewIds(@Param("reviewIds") List<Long> reviewIds);
}
