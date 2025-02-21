// 리뷰 댓글 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.controller

// 필요한 Spring 및 프로젝트 내부 클래스들을 임포트
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.service.ReviewCommentService
import org.team14.webty.security.authentication.WebtyUserDetails

// REST API 컨트롤러임을 나타내는 어노테이션
@RestController
// 모든 엔드포인트의 기본 URL 경로를 지정 (/reviews/{reviewId}/comments)
@RequestMapping("/reviews/{reviewId}/comments")
// ReviewCommentController 클래스 선언과 생성자 주입
class ReviewCommentController(
    private val commentService: ReviewCommentService
) {
    // 댓글 생성 엔드포인트
    @PostMapping
    fun createComment(
        // 현재 인증된 사용자 정보를 주입
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        // URL 경로에서 reviewId를 추출
        @PathVariable reviewId: Long,
        // 요청 본문에서 댓글 정보를 추출
        @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> =
        // 서비스 계층에 댓글 생성을 위임하고 결과를 200 OK와 함께 반환
        ResponseEntity.ok(commentService.createComment(userDetails, reviewId, request))

    // 댓글 수정 엔드포인트
    @PutMapping("/{commentId}")
    fun updateComment(
        // 현재 인증된 사용자 정보를 주입
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        // URL 경로에서 commentId를 추출
        @PathVariable commentId: Long,
        // 요청 본문에서 수정할 댓글 정보를 추출
        @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> =
        // 서비스 계층에 댓글 수정을 위임하고 결과를 200 OK와 함께 반환
        ResponseEntity.ok(commentService.updateComment(commentId, userDetails, request))

    // 댓글 삭제 엔드포인트
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        // 현재 인증된 사용자 정보를 주입
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        // URL 경로에서 commentId를 추출
        @PathVariable commentId: Long
    ): ResponseEntity<Unit> =
        // 서비스 계층에 댓글 삭제를 위임하고 결과를 200 OK와 함께 반환
        ResponseEntity.ok(commentService.deleteComment(commentId, userDetails))

    // 댓글 목록 조회 엔드포인트
    @GetMapping
    fun getComments(
        // URL 경로에서 reviewId를 추출
        @PathVariable reviewId: Long,
        // 페이지 번호 (기본값: 0)
        @RequestParam(defaultValue = "0") page: Int,
        // 페이지 크기 (기본값: 10)
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<CommentResponse>> =
        // 서비스 계층에 댓글 목록 조회를 위임하고 결과를 200 OK와 함께 반환
        ResponseEntity.ok(commentService.getCommentsByReviewId(reviewId, page, size))
}