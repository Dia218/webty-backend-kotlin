// 리뷰 댓글 서비스 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.service

// 필요한 Spring 및 프로젝트 내부 클래스들을 임포트
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.review.entity.Review
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.reviewComment.mapper.ReviewCommentMapper
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.user.entity.WebtyUser

// 리뷰 댓글 관련 비즈니스 로직을 처리하는 서비스 클래스
@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
class ReviewCommentService(
    private val reviewRepository: ReviewRepository, // 리뷰 레포지토리
    private val reviewCommentRepository: ReviewCommentRepository, // 리뷰 댓글 레포지토리
) {
    // 리뷰 ID로 리뷰가 존재하는지 확인하는 메서드
    fun existsReviewById(reviewId: Long): Boolean =
        reviewRepository.existsById(reviewId)

    // 리뷰 ID로 리뷰 엔티티 참조를 가져오는 메서드
    fun getReferenceById(reviewId: Long): Review =
        reviewRepository.getReferenceById(reviewId)

    // 새로운 댓글 생성
    @Transactional
    fun createComment(reviewId: Long, request: CommentRequest, user: WebtyUser): CommentResponse {
        // 리뷰 존재 여부 확인
        val review = reviewRepository.findById(reviewId).orElseThrow {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        // 부모 댓글이 있는 경우 (대댓글) 부모 댓글 존재 여부 확인
        request.parentCommentId?.let { parentId ->
            if (!reviewCommentRepository.existsById(parentId)) {
                throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
            }
        }

        // 댓글 엔티티 생성 및 저장
        val comment = ReviewCommentMapper.toEntity(request, user, review)
        return ReviewCommentMapper.toResponse(reviewCommentRepository.save(comment))
    }

    // 기존 댓글 수정
    @Transactional
    fun updateComment(commentId: Long, request: CommentRequest, user: WebtyUser): CommentResponse {
        // 댓글 존재 여부 확인 및 권한 검증
        val comment = getCommentWithAuthorization(commentId, user)

        // 댓글 내용 및 멘션 업데이트
        comment.updateComment(request.content, request.mentions)
        return ReviewCommentMapper.toResponse(comment)
    }

    // 댓글 삭제
    @Transactional
    fun deleteComment(commentId: Long, user: WebtyUser) {
        // 댓글 존재 여부 확인 및 권한 검증
        val comment = getCommentWithAuthorization(commentId, user)

        // 댓글 삭제 (대댓글이 있는 경우 함께 삭제)
        reviewCommentRepository.delete(comment)
    }

    // 리뷰에 달린 댓글 목록 조회
    @Cacheable(value = ["comments"], key = "#reviewId") // 캐시 적용
    fun getComments(reviewId: Long, pageable: Pageable): Page<CommentResponse> {
        // 리뷰 존재 여부 확인
        if (!reviewRepository.existsById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        // 댓글 목록 조회
        val commentPage = reviewCommentRepository.findAllByReviewIdOrderByDepthAndCommentId(reviewId, pageable)

        // 댓글 계층 구조 구성을 위한 맵
        val commentMap = mutableMapOf<Long, MutableList<ReviewComment>>()

        // 댓글들을 부모-자식 관계로 구성
        commentPage.content.forEach { comment ->
            comment.parentId?.let { parentId ->
                commentMap.getOrPut(parentId) { mutableListOf() }.add(comment)
            }
        }

        // 루트 댓글만 필터링하고 응답 DTO 생성
        val rootComments = commentPage.content.filter { it.parentId == null }
            .map { rootComment ->
                // 대댓글 목록을 찾아서 DTO로 변환
                val childComments = commentMap[rootComment.commentId]?.map { childComment ->
                    ReviewCommentMapper.toResponse(childComment)
                } ?: emptyList()

                // 루트 댓글을 DTO로 변환하면서 대댓글 목록 포함
                ReviewCommentMapper.toResponse(rootComment).let { rootResponse ->
                    CommentResponse(
                        user = rootResponse.user,
                        commentId = rootResponse.commentId,
                        content = rootResponse.content,
                        createdAt = rootResponse.createdAt,
                        modifiedAt = rootResponse.modifiedAt,
                        parentId = rootResponse.parentId,
                        mentions = rootResponse.mentions,
                        childComments = childComments
                    )
                }
            }

        // 페이지 정보와 함께 결과 반환
        return PageImpl(rootComments, pageable, commentPage.totalElements)
    }

    // 댓글 존재 여부 확인 및 작성자 권한 검증
    private fun getCommentWithAuthorization(commentId: Long, user: WebtyUser): ReviewComment {
        val comment = reviewCommentRepository.findById(commentId).orElseThrow {
            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        // 댓글 작성자와 요청자가 다른 경우 권한 없음 예외 발생
        if (comment.user?.userId != user.userId) {
            throw BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED)
        }

        return comment
    }
}