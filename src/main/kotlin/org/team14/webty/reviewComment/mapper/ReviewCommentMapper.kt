// 리뷰 댓글 매퍼 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.mapper

// 필요한 클래스들을 임포트
import org.springframework.stereotype.Component
import org.team14.webty.review.entity.Review
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.user.entity.WebtyUser
import java.time.LocalDateTime

// 스프링 컴포넌트임을 나타내는 어노테이션
@Component
// ReviewCommentMapper를 싱글톤 객체로 선언
object ReviewCommentMapper {
    // CommentRequest DTO를 ReviewComment 엔티티로 변환하는 메서드
    fun toEntity(request: CommentRequest, user: WebtyUser, review: Review) = ReviewComment(
        // 댓글 작성자 정보
        user = user,
        // 댓글이 속한 리뷰 정보
        review = review,
        // 댓글 내용
        content = request.content,
        // 부모 댓글 ID (대댓글인 경우)
        parentId = request.parentCommentId,
        // 댓글 깊이 (부모 댓글이 있으면 1, 없으면 0)
        depth = if (request.parentCommentId != null) 1 else 0,
        // 멘션된 사용자 목록
        mentions = request.mentions
    )

    // ReviewComment 엔티티를 CommentResponse DTO로 변환하는 메서드
    fun toResponse(comment: ReviewComment?) = comment?.let {
        // null이 아닌 경우 CommentResponse 객체 생성
        CommentResponse(
            // 댓글 작성자 정보를 DTO로 변환
            user = UserDataResponse(it.user),
            // 댓글 ID
            commentId = it.commentId,
            // 댓글 내용
            content = it.content,
            // 생성 시간
            createdAt = it.getCreatedAt(),
            // 수정 시간 (생성 시간과 같으면 null 반환)
            modifiedAt = if (it.getModifiedAt()?.equals(it.getCreatedAt()) == true) null else it.getModifiedAt(),
            // 부모 댓글 ID
            parentId = it.parentId,
            // 멘션된 사용자 목록
            mentions = it.mentions,
            // 대댓글 목록 (초기에는 빈 리스트)
            childComments = emptyList()
        )
    } ?: throw IllegalArgumentException("Comment cannot be null")
    // null인 경우 예외 발생
}