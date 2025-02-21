// 리뷰 댓글 응답 DTO 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.dto

// 필요한 클래스들을 임포트
import org.team14.webty.user.dto.UserDataResponse
import java.time.LocalDateTime

// 댓글 조회 응답을 위한 데이터 클래스
data class CommentResponse(
    // 댓글 작성자 정보
    val user: UserDataResponse,
    
    // 댓글의 고유 식별자
    val commentId: Long,
    
    // 댓글 내용
    val content: String,
    
    // 댓글 생성 시간
    val createdAt: LocalDateTime,
    
    // 댓글 수정 시간 (수정되지 않은 경우 null)
    val modifiedAt: LocalDateTime?,
    
    // 부모 댓글의 ID (대댓글인 경우에만 값이 있음, 루트 댓글인 경우 null)
    val parentId: Long?,
    
    // 멘션된 사용자들의 닉네임 목록
    val mentions: List<String>,
    
    // 이 댓글에 달린 대댓글 목록
    val childComments: List<CommentResponse> = emptyList()
)