// 리뷰 댓글 응답 DTO 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.dto

// 필요한 클래스들을 임포트
import org.team14.webty.user.dto.UserDataResponse
import java.time.LocalDateTime

// 댓글 조회 응답을 위한 데이터 클래스
data class CommentResponse(
    // 댓글 작성자 정보 (UserDataResponse DTO 사용)
    val user: UserDataResponse,
    
    // 댓글 ID
    val commentId: Long,
    
    // 댓글 내용
    val content: String,
    
    // 댓글 생성 시간
    val createdAt: LocalDateTime,
    
    // 댓글 수정 시간 (수정되지 않은 경우 null 가능)
    val modifiedAt: LocalDateTime?,
    
    // 부모 댓글 ID (대댓글인 경우에만 값이 있음)
    val parentId: Long?,
    
    // 멘션된 사용자 목록
    val mentions: List<String>,
    
    // 대댓글 목록 (계층형 구조 표현)
    var childComments: List<CommentResponse> = emptyList()
)