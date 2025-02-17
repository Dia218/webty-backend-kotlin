// package org.team14.webty.recommend.controller;
//
// import java.util.Map;
//
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.team14.webty.common.dto.PageDto;
// import org.team14.webty.common.mapper.PageMapper;
// import org.team14.webty.recommend.service.RecommendService;
// import org.team14.webty.review.dto.ReviewItemResponse;
// import org.team14.webty.review.service.ReviewService;
// import org.team14.webty.security.authentication.WebtyUserDetails;
//
// import lombok.RequiredArgsConstructor;
//
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/recommend")
// public class RecommendController {
// 	private final RecommendService recommendService;
// 	private final ReviewService reviewService;
//
// 	// 선택한 리뷰 추천하기
// 	@PostMapping("/{reviewId}")
// 	public ResponseEntity<Long> createRecommend(
// 		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
// 		@PathVariable(value = "reviewId") Long reviewId,
// 		@RequestParam(value = "type") String type
// 	){
// 		return ResponseEntity.ok(recommendService.createRecommend(webtyUserDetails,reviewId,type));
// 	}
//
// 	// 선택한 리뷰 추천 취소하기
// 	@DeleteMapping("/{reviewId}")
// 	public ResponseEntity<Void> deleteRecommend(
// 		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
// 		@PathVariable(value = "reviewId") Long reviewId,
// 		@RequestParam(value = "type") String type
// 	){
// 		recommendService.deleteRecommend(webtyUserDetails,reviewId,type);
// 		return ResponseEntity.ok().build();
// 	}
//
// 	// 선택한 리뷰의 추천수 조회 (사용하지 않을 것으로 보임)
// 	@GetMapping("/{reviewId}")
// 	public ResponseEntity<Map<String,Long>> getRecommendCounts(
// 		@PathVariable(value = "reviewId") Long reviewId
// 	){
// 		Map<String, Long> recommendCounts = recommendService.getRecommendCounts(reviewId);
// 		return ResponseEntity.ok(recommendCounts);
// 	}
//
// 	// 로그인한 사용자 기준) 추천한 리뷰 목록 조회
// 	@GetMapping("/user/{userId}")
// 	public ResponseEntity<PageDto<ReviewItemResponse>> getUserRecommendReviews(
// 		@PathVariable(value = "userId") Long userId,
// 		@RequestParam(defaultValue = "0", value = "page") int page,
// 		@RequestParam(defaultValue = "10", value = "size") int size
// 	){
// 		return ResponseEntity.ok(PageMapper.toPageDto(reviewService.getUserRecommendedReviews(userId, page, size)));
// 	}
//
// 	// 로그인한 사용자 기준) 선택한 리뷰의 추천 여부 반환
// 	@GetMapping("/{reviewId}/recommendation")
// 	public ResponseEntity<Map<String, Boolean>> getRecommended(
// 		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
// 		@PathVariable(value="reviewId") Long reviewId
// 	){
// 		return ResponseEntity.ok(recommendService.isRecommended(webtyUserDetails,reviewId));
// 	}
// }
