// 리뷰 댓글 매퍼 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.mapper

// 필요한 의존성들을 임포트
import org.team14.webty.review.entity.Review
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.user.entity.WebtyUser

object ReviewCommentMapper {
    // CommentRequest DTO를 ReviewComment 엔티티로 변환하는 메서드
    fun toEntity(
        request: CommentRequest, // 클라이언트로부터 받은 댓글 요청 DTO
        user: WebtyUser, // 댓글 작성자 정보
        review: Review // 댓글이 달릴 리뷰 정보
    ): ReviewComment {
        // 새로운 ReviewComment 엔티티 생성
        return ReviewComment(
            user = user, // 댓글 작성자 설정
            review = review, // 연관된 리뷰 설정
            content = request.content, // 댓글 내용 설정
            parentId = request.parentCommentId, // 부모 댓글 ID 설정 (대댓글인 경우)
            depth = if (request.parentCommentId != null) 1 else 0, // 댓글 깊이 설정 (대댓글: 1, 루트 댓글: 0)
            mentions = request.mentions.joinToString(",") // 멘션 목록을 콤마로 구분된 문자열로 변환
        )
    }

    // ReviewComment 엔티티를 CommentResponse DTO로 변환하는 메서드
    fun toResponse(comment: ReviewComment): CommentResponse {
        val user = comment.user ?: throw IllegalStateException("Comment user cannot be null")
        return CommentResponse(
            commentId = comment.commentId!!,
            content = comment.content,
            user = UserDataResponse(
                userid = user.userId!!,
                nickname = user.nickname,
                profileImage = user.profileImage
            ),
            parentId = comment.parentId,
            mentions = comment.getMentionsList(),
            childComments = emptyList(),
            createdAt = comment.createdAt!!,
            modifiedAt = comment.modifiedAt
        )
    }
}