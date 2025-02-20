package org.team14.webty.reviewComment.controller

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.service.ReviewCommentService
import org.team14.webty.security.authentication.WebtyUserDetails

@RestController
@RequestMapping("/reviews/{reviewId}/comments")
class ReviewCommentController(
    private val commentService: ReviewCommentService
) {
    @PostMapping
    fun createComment(
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        @PathVariable reviewId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> =
        ResponseEntity.ok(commentService.createComment(userDetails, reviewId, request))

    @PutMapping("/{commentId}")
    fun updateComment(
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        @PathVariable commentId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<CommentResponse> =
        ResponseEntity.ok(commentService.updateComment(commentId, userDetails, request))

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal userDetails: WebtyUserDetails,
        @PathVariable commentId: Long
    ): ResponseEntity<Unit> =
        ResponseEntity.ok(commentService.deleteComment(commentId, userDetails))

    @GetMapping
    fun getComments(
        @PathVariable reviewId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<CommentResponse>> =
        ResponseEntity.ok(commentService.getCommentsByReviewId(reviewId, page, size))
}