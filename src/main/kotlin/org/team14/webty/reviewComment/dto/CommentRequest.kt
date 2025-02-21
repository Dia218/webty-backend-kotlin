// 리뷰 댓글 요청 DTO 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.dto

// 댓글 생성/수정 요청을 위한 데이터 클래스
data class CommentRequest(
    // 댓글 내용
    val content: String,
    
    // 멘션된 사용자 목록 (기본값: 빈 리스트)
    val mentions: List<String> = emptyList(),
    
    // 부모 댓글 ID (대댓글인 경우에만 사용, 기본값: null)
    val parentCommentId: Long? = null
)