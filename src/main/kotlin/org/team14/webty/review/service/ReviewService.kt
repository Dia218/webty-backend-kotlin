package org.team14.webty.review.service

import lombok.SneakyThrows
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.common.util.FileStorageUtil
import org.team14.webty.recommend.repository.RecommendRepository
import org.team14.webty.review.dto.ReviewDetailResponse
import org.team14.webty.review.dto.ReviewItemResponse
import org.team14.webty.review.dto.ReviewRequest
import org.team14.webty.review.entity.Review
import org.team14.webty.review.entity.ReviewImage
import org.team14.webty.review.mapper.ReviewMapper
import org.team14.webty.review.repository.ReviewImageRepository
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.reviewComment.mapper.ReviewCommentMapper
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.security.authentication.AuthWebtyUserProvider
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.repository.WebtoonRepository
import java.util.stream.Collectors
import java.util.stream.IntStream

@Service
@Slf4j
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val webtoonRepository: WebtoonRepository,
    private val reviewCommentRepository: ReviewCommentRepository,
    private val authWebtyUserProvider: AuthWebtyUserProvider,
    private val fileStorageUtil: FileStorageUtil,
    private val reviewImageRepository: ReviewImageRepository,
    private val recommendRepository: RecommendRepository
){

    // 리뷰 상세 조회
    @Transactional
    fun getFeedReview(id: Long, page: Int, size: Int): ReviewDetailResponse {
        val pageable: Pageable = PageRequest.of(page, size)
        val review = reviewRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.REVIEW_NOT_FOUND) }!!
        val comments = reviewCommentRepository.findAllByReviewIdOrderByDepthAndCommentId(id, pageable)
        val commentResponses = PageMapper.toPageDto(comments.map { comment: ReviewComment? ->
            ReviewCommentMapper.toResponse(
                comment
            )
        })
        val reviewImages = reviewImageRepository.findAllByReview(review)
        review.plusViewCount() // 조회수 증가
        val recommendCounts = recommendRepository.getRecommendCounts(id)
        return ReviewMapper.toDetail(review, commentResponses, reviewImages, recommendCounts)
    }

    // 전체 리뷰 조회
    @Transactional(readOnly = true)
    fun getAllFeedReviews(page: Int, size: Int): Page<ReviewItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)

        // 모든 리뷰 조회
        val reviews = reviewRepository.findAllByOrderByReviewIdDesc(pageable)

        // 모든 리뷰 ID 리스트 추출
        val reviewIds = reviews.mapNotNull { it.reviewId }

        // 리뷰 ID를 기반으로 한 번의 쿼리로 모든 댓글 조회
        val commentMap = getReviewMap(reviewIds)

        // 리뷰 ID 리스트를 기반으로 한 번의 쿼리로 모든 리뷰 이미지 조회
        val reviewImageMap = getReviewImageMap(reviewIds)

        // 리뷰 ID 리스트를 기반으로 한 번의 쿼리로 모든 추천수 조회
        val likeCounts = getLikesMap(reviewIds)

        return reviews.map { review ->
            ReviewMapper.toResponse(
                review,
                commentMap[review.reviewId] ?: emptyList(),
                reviewImageMap[review.reviewId] ?: emptyList(),
                likeCounts[review.reviewId]!!
            )
        }
    }


    // 리뷰 생성
    @Transactional
    fun createFeedReview(webtyUserDetails: WebtyUserDetails?, reviewRequest: ReviewRequest): Long {
        val webtyUser = getAuthenticatedUser(webtyUserDetails)

        val webtoon = webtoonRepository.findById(reviewRequest.webtoonId)
            .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }

        val review = ReviewMapper.toEntity(reviewRequest, webtyUser, webtoon)
        reviewRepository.save(review)

        if (!reviewRequest.images.isNullOrEmpty()) {
            uploadReviewImage(review, reviewRequest.images!!)
        }

        return review.reviewId!!
    }

    // 리뷰 삭제
    @Transactional
    fun deleteFeedReview(webtyUserDetails: WebtyUserDetails?, id: Long) {
        val webtyUser = getAuthenticatedUser(webtyUserDetails)

        val review = reviewRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.REVIEW_NOT_FOUND) }!!

        if (review.user.userId != webtyUser.userId) {
            throw BusinessException(ErrorCode.REVIEW_PERMISSION_DENIED)
        }

        // 해당 리뷰에 달린 댓글 삭제 처리
        reviewCommentRepository.deleteAll(reviewCommentRepository.findAllByReviewIdOrderByParentCommentIdAndDepth(id))
        // 해당 리뷰에 달린 이미지 삭제 처리
        deleteExistingReviewImages(review)
        reviewRepository.delete(review)
    }

    // 리뷰 수정
    @Transactional
    fun updateFeedReview(
        webtyUserDetails: WebtyUserDetails?, id: Long,
        reviewRequest: ReviewRequest
    ): Long {
        val webtyUser = getAuthenticatedUser(webtyUserDetails)

        val review = reviewRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.REVIEW_NOT_FOUND) }!!

        val webtoon = webtoonRepository.findById(reviewRequest.webtoonId)
            .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }

        if (review.user.userId != webtyUser.userId) {
            throw BusinessException(ErrorCode.REVIEW_PERMISSION_DENIED)
        }

        deleteExistingReviewImages(review)

        if (!reviewRequest.images.isNullOrEmpty()) {
            uploadReviewImage(review, reviewRequest.images!!)
        }

        review.updateReview(
            reviewRequest.title, reviewRequest.content, reviewRequest.spoilerStatus,
            webtoon
        )
        reviewRepository.save(review)

        return review.reviewId!!
    }

    // 특정 사용자의 리뷰 목록 조회
    @Transactional(readOnly = true)
    fun getReviewsByUser(webtyUserDetails: WebtyUserDetails?, page: Int, size: Int): Page<ReviewItemResponse> {
        val webtyUser = getAuthenticatedUser(webtyUserDetails)
        val pageable: Pageable = PageRequest.of(page, size)
        val reviews = reviewRepository.findReviewByWebtyUser(webtyUser, pageable)
        val reviewIds = reviews.mapNotNull { it.reviewId }
        val commentMap = getReviewMap(reviewIds)
        val reviewImageMap = getReviewImageMap(reviewIds)
        val likeCounts = getLikesMap(reviewIds)

        return reviews.map { review: Review ->
            ReviewMapper.toResponse(
                review,
                commentMap.getOrDefault(review.reviewId, emptyList()),
                reviewImageMap.getOrDefault(review.reviewId, emptyList()),
                likeCounts[review.reviewId]!!
            )
        }
    }

    // 조회수 내림차순으로 모든 리뷰 조회
    @Transactional(readOnly = true)
    fun getAllReviewsOrderByViewCountDesc(page: Int, size: Int): Page<ReviewItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)

        val reviews = reviewRepository.findAllByOrderByViewCountDesc(pageable)

        val reviewIds = reviews.mapNotNull { it.reviewId }

        val commentMap = getReviewMap(reviewIds)

        val reviewImageMap = getReviewImageMap(reviewIds)

        val likeCounts = getLikesMap(reviewIds)

        return reviews.map { review: Review ->
            ReviewMapper.toResponse(
                review,
                commentMap.getOrDefault(review.reviewId, emptyList()),
                reviewImageMap.getOrDefault(review.reviewId, emptyList()),
                likeCounts[review.reviewId]!!
            )
        }
    }

    // 특정 사용자의 리뷰 개수 조회
    @Transactional(readOnly = true)
    fun getReviewCountByUser(webtyUserDetails: WebtyUserDetails?): Long {
        val webtyUser = getAuthenticatedUser(webtyUserDetails)
        return reviewRepository.countReviewByWebtyUser(webtyUser)
    }

    @Transactional
    @SneakyThrows
    fun uploadReviewImage(review: Review, files: List<MultipartFile>) {
        val fileUrls = fileStorageUtil.storeImageFiles(files)
        fileUrls.stream()
            .map { fileUrl: String -> ReviewMapper.toImageEntity(fileUrl, review) }
            .forEach { entity: ReviewImage -> reviewImageRepository.save(entity) }
    }

    private fun getReviewMap(reviewIds: List<Long>): Map<Long, List<CommentResponse>> {
        val allComments = reviewCommentRepository.findAllByReviewIds(reviewIds)

        val parentComments = allComments.filter { it.parentId == null }

        // 리뷰 ID를 기준으로 부모 댓글을 매핑하는 Map 생성
        return parentComments.groupBy(
            { it.review.reviewId!! },
            { ReviewCommentMapper.toResponse(it) }
        )
    }


    private fun getReviewImageMap(reviewIds: List<Long>): Map<Long, List<String>> {
        // 특정 리뷰 ID 리스트에 해당하는 모든 ReviewImage 조회 (한 번의 쿼리)
        val reviewImages = reviewImageRepository.findByReviewIdIn(reviewIds)

        // Review ID를 Key로, 이미지 URL 리스트를 Value로 변환
        return reviewImages.filter { it.review.reviewId != null }
            .groupBy(
                { it.review.reviewId!! },
                { it.imageUrl }
            )
    }

    private fun deleteExistingReviewImages(review: Review) {
        val existingImages = reviewImageRepository.findAllByReview(review)

        for (image in existingImages) {
            fileStorageUtil.deleteFile(image.imageUrl) // 로컬 파일 삭제
        }
        reviewImageRepository.deleteAll(existingImages) // DB에서 삭제
    }

    fun getAuthenticatedUser(webtyUserDetails: WebtyUserDetails?): WebtyUser {
        return authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
    }

    fun searchFeedReviewByTitle(page: Int, size: Int, title: String?): Page<ReviewItemResponse?> {
        val pageable: Pageable = PageRequest.of(page, size)

        val reviews = reviewRepository.findByTitleContainingIgnoreCaseOrderByReviewIdDesc(title, pageable)

        val reviewIds = reviews.mapNotNull { it.reviewId }

        val commentMap = getReviewMap(reviewIds)
        val reviewImageMap = getReviewImageMap(reviewIds)
        val likeCounts = getLikesMap(reviewIds)

        return reviews.map { review: Review ->
            ReviewMapper.toResponse(
                review,
                commentMap.getOrDefault(review.reviewId, emptyList()),
                reviewImageMap.getOrDefault(review.reviewId, emptyList()),
                likeCounts[review.reviewId]!!
            )
        }
    }

    @Transactional
    fun getUserRecommendedReviews(userId: Long, page: Int, size: Int): Page<ReviewItemResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val reviews = recommendRepository.getUserRecommendReview(userId, pageable)
        val reviewIds = reviews.mapNotNull { it.reviewId }
        val commentMap = getReviewMap(reviewIds)
        val reviewImageMap = getReviewImageMap(reviewIds)
        val likeCounts = getLikesMap(reviewIds)
        return reviews.map { review: Review ->
            ReviewMapper.toResponse(
                review,
                commentMap.getOrDefault(review.reviewId, emptyList()),
                reviewImageMap.getOrDefault(review.reviewId, emptyList()),
                likeCounts[review.reviewId]!!
            )
        }
    }

    private fun getLikesMap(reviewIds: List<Long>): Map<Long, Long> {
        val counts = recommendRepository.getLikeCounts(reviewIds)
        return IntStream.range(0, reviewIds.size)
            .boxed()
            .collect(
                Collectors.toMap(
                    { index: Int? -> reviewIds[index!!] },  // key: reviewId
                    { index: Int? -> counts[index!!] } // value: count
                ))
    }

    fun searchReviewByWebtoonId(webtoonId: Long, page: Int, size: Int): Page<ReviewItemResponse?> {
        val pageable: Pageable = PageRequest.of(page, size)
        val reviews = reviewRepository.findReviewByWebtoonId(webtoonId, pageable)
        val reviewIds = reviews.mapNotNull { it.reviewId }
        val commentMap = getReviewMap(reviewIds)
        val reviewImageMap = getReviewImageMap(reviewIds)
        val likeCounts = getLikesMap(reviewIds)
        return reviews.map { review: Review ->
            ReviewMapper.toResponse(
                review,
                commentMap.getOrDefault(review.reviewId, emptyList()),
                reviewImageMap.getOrDefault(review.reviewId, emptyList()),
                likeCounts[review.reviewId]!!
            )
        }
    }

    @Transactional
    fun patchReviewIsSpoiler(id: Long) {
        val review = reviewRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.REVIEW_NOT_FOUND) }!!
        review.patchIsSpoiler()
        reviewRepository.save(review)
    }
}
