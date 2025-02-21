// 리뷰 댓글 서비스 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.service

// 필요한 Spring 및 프로젝트 내부 클래스들을 임포트
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.review.entity.Review
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.mapper.ReviewCommentMapper
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.security.authentication.WebtyUserDetails
import java.time.LocalDateTime

// 서비스 계층임을 나타내는 어노테이션
@Service
// 기본적으로 모든 메서드가 읽기 전용 트랜잭션으로 동작함을 명시
@Transactional(readOnly = true)
class ReviewCommentService(
    // 필요한 의존성 주입
    private val reviewRepository: ReviewRepository,
    private val commentRepository: ReviewCommentRepository,
    private val commentMapper: ReviewCommentMapper
) {
    // 리뷰 ID로 리뷰가 존재하는지 확인하는 메서드
    fun existsReviewById(reviewId: Long): Boolean = 
        reviewRepository.existsById(reviewId)

    // 리뷰 ID로 리뷰 엔티티 참조를 가져오는 메서드
    fun getReferenceById(reviewId: Long): Review = 
        reviewRepository.getReferenceById(reviewId)

    // 댓글 생성 메서드
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    @Cacheable(value = ["comments"], key = "#reviewId") // 캐시 적용
    fun createComment(
        userDetails: WebtyUserDetails,
        reviewId: Long,
        request: CommentRequest
    ): CommentResponse {
        // 리뷰가 존재하지 않으면 예외 발생
        if (!existsReviewById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        // 부모 댓글이 있는 경우 (대댓글인 경우)
        request.parentCommentId?.let { parentId ->
            // 부모 댓글을 찾고 depth 검사
            commentRepository.findById(parentId).orElseThrow {
                BusinessException(ErrorCode.COMMENT_NOT_FOUND)
            }.takeIf { it.depth >= 2 }?.let {
                // depth가 2 이상이면 더 이상의 대댓글을 달 수 없음
                throw BusinessException(ErrorCode.COMMENT_WRITING_RESTRICTED)
            }
        }

        // 리뷰 엔티티 참조 가져오기
        val review = getReferenceById(reviewId)
        // 댓글 엔티티 생성 및 저장
        val comment = commentMapper.toEntity(request, userDetails.webtyUser, review)
        return commentMapper.toResponse(commentRepository.save(comment))
    }

    // 댓글 수정 메서드
    @Transactional
    fun updateComment(
        commentId: Long,
        userDetails: WebtyUserDetails,
        request: CommentRequest
    ): CommentResponse {
        // 댓글 찾기
        val comment = commentRepository.findById(commentId).orElseThrow {
            BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        // 댓글 작성자와 현재 사용자가 다르면 예외 발생
        if (comment.user.userId != userDetails.webtyUser.userId) {
            throw BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED)
        }

        // 댓글 내용 업데이트
        comment.updateComment(request.content, request.mentions)
        return commentMapper.toResponse(comment)
    }

    // 댓글 삭제 메서드
    @Transactional
    @CacheEvict(value = ["comments"], allEntries = true) // 관련된 모든 캐시 삭제
    fun deleteComment(commentId: Long, userDetails: WebtyUserDetails) {
        // 댓글 찾기
        val comment = commentRepository.findById(commentId).orElseThrow {
            BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        // 댓글 작성자와 현재 사용자가 다르면 예외 발생
        if (comment.user.userId != userDetails.webtyUser.userId) {
            throw BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED)
        }

        // 대댓글이 있으면 모두 삭제
        commentRepository.findByParentIdOrderByCommentIdAsc(commentId).let { childComments ->
            if (childComments.isNotEmpty()) {
                commentRepository.deleteAll(childComments)
            }
        }
        // 댓글 삭제
        commentRepository.delete(comment)
    }

    // 리뷰에 달린 댓글 목록 조회 메서드
    @Cacheable(value = ["comments"], key = "#reviewId + '_' + #page + '_' + #size") // 캐시 적용
    fun getCommentsByReviewId(reviewId: Long, page: Int, size: Int): Page<CommentResponse> {
        // 리뷰가 존재하지 않으면 예외 발생
        if (!existsReviewById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        // 페이지네이션 정보 생성
        val pageable = PageRequest.of(page, size)
        // 댓글 목록 조회
        val commentPage = commentRepository.findAllByReviewIdOrderByDepthAndCommentId(reviewId, pageable)

        // 댓글 계층 구조 구성을 위한 맵
        val commentMap = mutableMapOf<Long, MutableList<CommentResponse>>()
        // 루트 댓글만 필터링하고 대댓글은 맵에 저장
        val rootComments = commentPage.content.filter { comment ->
            if (comment.parentId != null) {
                // 대댓글인 경우 맵에 추가
                commentMap.getOrPut(comment.parentId) { mutableListOf() }
                    .add(commentMapper.toResponse(comment))
                false
            } else {
                true // 루트 댓글인 경우
            }
        }.map { commentMapper.toResponse(it) }
            .onEach { root ->
                // 각 루트 댓글에 대댓글 목록 설정
                root.childComments = commentMap[root.commentId] ?: emptyList()
            }

        // 페이지 정보와 함께 결과 반환
        return PageImpl(rootComments, pageable, commentPage.totalElements)
    }
}