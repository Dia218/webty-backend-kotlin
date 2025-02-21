// 리뷰 댓글 요청 DTO 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.dto

// 댓글 생성/수정 요청을 위한 데이터 클래스
// 클라이언트로부터 받는 댓글 관련 데이터를 담는 DTO (Data Transfer Object)
data class CommentRequest(
    // 댓글 내용 (필수)
    // 사용자가 작성한 실제 댓글 텍스트
    val content: String,
    
    // 멘션된 사용자 목록
    // @를 사용하여 언급된 사용자들의 닉네임 리스트
    // 기본값은 빈 리스트
    val mentions: List<String> = emptyList(),
    
    // 부모 댓글 ID
    // 대댓글인 경우 부모 댓글의 ID 값
    // 루트 댓글인 경우 null
    // 기본값은 null
    val parentCommentId: Long? = null
)