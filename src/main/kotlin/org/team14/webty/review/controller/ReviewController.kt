package org.team14.webty.review.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.common.dto.PageDto
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.review.dto.ReviewDetailResponse
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.review.dto.ReviewRequest
import org.team14.webty.review.service.ReviewService
import org.team14.webty.security.authentication.WebtyUserDetails

@RestController
@RequestMapping("/reviews")
class ReviewController(private val reviewService: ReviewService) {

    // id로 조회하기
    @GetMapping("/{reviewId}")
    fun getReviewDetail(
        @PathVariable(value = "reviewId") reviewId: Long,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<ReviewDetailResponse> {
        return ResponseEntity.ok(reviewService.getFeedReview(reviewId, page, size))
    }

    //전체 리뷰 조회
    @GetMapping
    fun getAllFeedReviews(
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(
            PageMapper.toPageDto(
                reviewService.getAllFeedReviews(page, size)
            )
        )
    }

    // 리뷰 생성
    @PostMapping(value = ["/create"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createReview(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @RequestPart(value = "reviewRequest") reviewRequest: ReviewRequest,
        @RequestPart(value = "images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<Long> {
        reviewRequest.images = images
        return ResponseEntity.ok(reviewService.createFeedReview(webtyUserDetails.webtyUser, reviewRequest))
    }

    //리뷰 삭제
    @DeleteMapping("/delete/{reviewId}")
    fun deleteFeedReview(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable(value = "reviewId") reviewId: Long
    ): ResponseEntity<Void> {
        reviewService.deleteFeedReview(webtyUserDetails.webtyUser, reviewId)
        return ResponseEntity.ok().build()
    }

    //리뷰 수정
    @PutMapping("/put/{reviewId}")
    fun updateFeedReview(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails, @PathVariable(value = "reviewId") reviewId: Long,
        @RequestPart(value = "reviewRequest") reviewRequest: ReviewRequest,
        @RequestPart(value = "images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<Long> {
        reviewRequest.images = images
        return ResponseEntity.ok()
            .body(reviewService.updateFeedReview(webtyUserDetails.webtyUser, reviewId, reviewRequest))
    }

    // 특정 사용자의 리뷰 목록 조회
    @GetMapping("/me")
    fun getReviewsByUser(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(
            PageMapper.toPageDto(
                reviewService.getReviewsByUser(webtyUserDetails.webtyUser, page, size)
            )
        )
    }

    // 조회수 내림차순으로 모든 리뷰 조회
    @GetMapping("/view-count-desc")
    fun getAllReviewsOrderByViewCountDesc(
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(
            PageMapper.toPageDto(
                reviewService.getAllReviewsOrderByViewCountDesc(page, size)
            )
        )
    }

    // 특정 사용자의 리뷰 개수 조회
    @GetMapping("/me/count")
    fun getReviewCountByUser(@AuthenticationPrincipal webtyUserDetails: WebtyUserDetails): ResponseEntity<Long> {
        return ResponseEntity.ok(reviewService.getReviewCountByUser(webtyUserDetails.webtyUser))
    }

    // 리뷰 검색 결과 반환
    @GetMapping("/search")
    fun searchReview(
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int,
        @RequestParam(defaultValue = "", value = "title") title: String
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(
            PageMapper.toPageDto(
                reviewService.searchFeedReviewByTitle(page, size, title)
            )
        )
    }

    // 특정 웹툰 ID에 대한 리뷰 페이지 반환
    @GetMapping("/webtoon/{webtoonId}")
    fun webtoonReviews(
        @PathVariable(value = "webtoonId") webtoonId: Long,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<PageDto<ReviewItemResponse>> {
        return ResponseEntity.ok(
            PageMapper.toPageDto(
                reviewService.searchReviewByWebtoonId(webtoonId, page, size)
            )
        )
    }

    // 특정 리뷰 ID 스포일러 처리
    @PatchMapping("/spoiler/{reviewId}")
    fun patchReviewIsSpoiler(
        @PathVariable(value = "reviewId") reviewId: Long
    ): ResponseEntity<Void> {
        reviewService.patchReviewIsSpoiler(reviewId)
        return ResponseEntity.ok().build()
    }
}
