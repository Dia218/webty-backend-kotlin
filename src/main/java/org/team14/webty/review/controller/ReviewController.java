package org.team14.webty.review.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.common.dto.PageDto;
import org.team14.webty.common.mapper.PageMapper;
import org.team14.webty.review.dto.ReviewDetailResponse;
import org.team14.webty.review.dto.ReviewItemResponse;
import org.team14.webty.review.dto.ReviewRequest;
import org.team14.webty.review.service.ReviewService;
import org.team14.webty.security.authentication.WebtyUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	// id로 조회하기
	@GetMapping("/{reviewId}")
	public ResponseEntity<ReviewDetailResponse> getReviewDetail(@PathVariable(value = "reviewId") Long reviewId,
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size) {
		return ResponseEntity.ok(reviewService.getFeedReview(reviewId, page, size));
	}

	//전체 리뷰 조회
	@GetMapping
	public ResponseEntity<PageDto<ReviewItemResponse>> getAllFeedReviews(
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size) {
		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.getAllFeedReviews(page, size)));
	}

	// 리뷰 생성
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Long> createReview(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestPart(value = "reviewRequest") ReviewRequest reviewRequest,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		reviewRequest.setImages(images);
		return ResponseEntity.ok(reviewService.createFeedReview(webtyUserDetails, reviewRequest));
	}

	//리뷰 삭제
	@DeleteMapping("/delete/{reviewId}")
	public ResponseEntity<Void> deleteFeedReview(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "reviewId") Long reviewId) {
		reviewService.deleteFeedReview(webtyUserDetails, reviewId);
		return ResponseEntity.ok().build();
	}

	//리뷰 수정
	@PutMapping("/put/{reviewId}")
	public ResponseEntity<Long> updateFeedReview(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails, @PathVariable(value = "reviewId") Long reviewId,
		@RequestPart(value = "reviewRequest") ReviewRequest reviewRequest,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		reviewRequest.setImages(images);
		return ResponseEntity.ok().body(reviewService.updateFeedReview(webtyUserDetails, reviewId, reviewRequest));
	}

	// 특정 사용자의 리뷰 목록 조회
	@GetMapping("/me")
	public ResponseEntity<PageDto<ReviewItemResponse>> getReviewsByUser(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size) {
		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.getReviewsByUser(webtyUserDetails, page, size)));
	}

	// 조회수 내림차순으로 모든 리뷰 조회
	@GetMapping("/view-count-desc")
	public ResponseEntity<PageDto<ReviewItemResponse>> getAllReviewsOrderByViewCountDesc(
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size) {
		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.getAllReviewsOrderByViewCountDesc(page, size)));
	}

	// 특정 사용자의 리뷰 개수 조회
	@GetMapping("/me/count")
	public ResponseEntity<Long> getReviewCountByUser(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails) {
		return ResponseEntity.ok(reviewService.getReviewCountByUser(webtyUserDetails));
	}

	// 리뷰 검색 결과 반환
	@GetMapping("/search")
	public ResponseEntity<PageDto<ReviewItemResponse>> searchReview(
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size,
		@RequestParam(defaultValue = "", value = "title") String title
	) {
		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.searchFeedReviewByTitle(page, size, title)));
	}

	// 특정 웹툰 ID에 대한 리뷰 페이지 반환
	@GetMapping("/webtoon/{webtoonId}")
	public ResponseEntity<PageDto<ReviewItemResponse>> webtoonReviews(
		@PathVariable(value = "webtoonId") Long webtoonId,
		@RequestParam(defaultValue = "0", value = "page") int page,
		@RequestParam(defaultValue = "10", value = "size") int size
	){
		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.searchReviewByWebtoonId(webtoonId,page, size)));
	}

	// 특정 리뷰 ID 스포일러 처리
	@PatchMapping("/spoiler/{reviewId}")
	public ResponseEntity<Void> patchReviewIsSpoiler(
		@PathVariable(value = "reviewId") Long reviewId) {
		reviewService.patchReviewIsSpoiler(reviewId);
		return ResponseEntity.ok().build();
	}
}
