package org.team14.webty.recommend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.common.dto.PageDto
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.recommend.service.RecommendService
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.review.service.ReviewService
import org.team14.webty.security.authentication.WebtyUserDetails

@RestController
@RequestMapping("/recommend")
class RecommendController(
        private val recommendService: RecommendService,
        private val reviewService: ReviewService
) {
    // 선택한 리뷰 추천하기
    @PostMapping("/{reviewId}")
    fun createRecommend(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @PathVariable(value = "reviewId") reviewId: Long,
            @RequestParam(value = "type") type: String
    ): ResponseEntity<Long> {
        return ResponseEntity.ok(recommendService.createRecommend(webtyUserDetails, reviewId, type))
    }

    // 선택한 리뷰 추천 취소하기
    @DeleteMapping("/{reviewId}")
    fun deleteRecommend(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @PathVariable(value = "reviewId") reviewId: Long,
            @RequestParam(value = "type") type: String
    ): ResponseEntity<Void> {
        recommendService.deleteRecommend(webtyUserDetails, reviewId, type)
        return ResponseEntity.ok().build()
    }

    // 선택한 리뷰의 추천수 조회 (사용하지 않을 것으로 보임)
    @GetMapping("/{reviewId}")
    fun getRecommendCounts(
            @PathVariable(value = "reviewId") reviewId: Long
    ): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(
                recommendService.getRecommendCounts(
                        reviewId
                )
        )
    }

    // 로그인한 사용자 기준) 추천한 리뷰 목록 조회
    @GetMapping("/user/{userId}")
    fun getUserRecommendReviews(
            @PathVariable(value = "userId") userId: Long,
            @RequestParam(defaultValue = "0", value = "page") page: Int,
            @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(PageMapper.toPageDto(reviewService.getUserRecommendedReviews(userId, page, size)))
    }

    // 로그인한 사용자 기준) 선택한 리뷰의 추천 여부 반환
    @GetMapping("/{reviewId}/recommendation")
    fun getRecommended(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @PathVariable(value = "reviewId") reviewId: Long
    ): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(
                recommendService.isRecommended(
                        webtyUserDetails,
                        reviewId
                )
        )
    }
}