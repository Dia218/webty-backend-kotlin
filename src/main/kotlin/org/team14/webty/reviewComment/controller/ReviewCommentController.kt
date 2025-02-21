// 리뷰 댓글 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.controller

// 필요한 Spring 및 프로젝트 내부 클래스들을 임포트
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.service.ReviewCommentService
import org.team14.webty.security.authentication.WebtyUserDetails

// REST API 컨트롤러임을 나타내는 어노테이션
@RestController
@RequestMapping("/reviews/{reviewId}/comments")
class ReviewCommentController(
    private val reviewCommentService: ReviewCommentService
) {
    // 새로운 댓글 생성 API
    // POST /reviews/{reviewId}/comments
    @PostMapping
    fun createComment(
        @PathVariable reviewId: Long,
        @RequestBody request: CommentRequest,
        @AuthenticationPrincipal userDetails: WebtyUserDetails
    ): ResponseEntity<CommentResponse> = runCatching {
        reviewCommentService.createComment(reviewId, request, userDetails.webtyUser)
    }.getOrThrow().let { ResponseEntity.ok(it) }

    // 기존 댓글 수정 API
    // PUT /reviews/{reviewId}/comments/{commentId}
    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable reviewId: Long,
        @PathVariable commentId: Long,
        @RequestBody request: CommentRequest,
        @AuthenticationPrincipal userDetails: WebtyUserDetails
    ): ResponseEntity<CommentResponse> = runCatching {
        reviewCommentService.updateComment(commentId, request, userDetails.webtyUser)
    }.getOrThrow().let { ResponseEntity.ok(it) }

    // 댓글 삭제 API
    // DELETE /reviews/{reviewId}/comments/{commentId}
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable reviewId: Long,
        @PathVariable commentId: Long,
        @AuthenticationPrincipal userDetails: WebtyUserDetails
    ): ResponseEntity<Unit> = runCatching {
        reviewCommentService.deleteComment(commentId, userDetails.webtyUser)
    }.getOrThrow().let { ResponseEntity.ok().build() }

    // 리뷰에 달린 댓글 목록 조회 API
    // GET /reviews/{reviewId}/comments
    @GetMapping
    fun getComments(
        @PathVariable reviewId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<CommentResponse>> = runCatching {
        reviewCommentService.getComments(reviewId, pageable)
    }.getOrThrow().let { ResponseEntity.ok(it) }
}