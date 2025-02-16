package org.team14.webty.reviewComment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.team14.webty.common.dto.PageDto;
import org.team14.webty.common.mapper.PageMapper;
import org.team14.webty.reviewComment.dto.CommentRequest;
import org.team14.webty.reviewComment.dto.CommentResponse;
import org.team14.webty.reviewComment.service.ReviewCommentService;
import org.team14.webty.security.authentication.WebtyUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews/{reviewId}/comments")
@RequiredArgsConstructor
public class ReviewCommentController {
	private final ReviewCommentService commentService;

	@PostMapping
	public ResponseEntity<CommentResponse> createComment(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable Long reviewId,
		@RequestBody @Valid CommentRequest request
	) {
		return ResponseEntity.ok(commentService.createComment(webtyUserDetails, reviewId, request));
	}

	@PutMapping("/{commentId}")
	public ResponseEntity<CommentResponse> updateComment(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentRequest request
	) {
		return ResponseEntity.ok(commentService.updateComment(commentId, webtyUserDetails, request));
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable Long commentId
	) {
		commentService.deleteComment(commentId, webtyUserDetails);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<PageDto<CommentResponse>> getComments(
		@PathVariable Long reviewId,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		return ResponseEntity.ok(PageMapper.toPageDto(commentService.getCommentsByReviewId(reviewId, page, size)));
	}
}
