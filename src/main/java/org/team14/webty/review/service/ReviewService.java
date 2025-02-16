package org.team14.webty.review.service;

import static org.team14.webty.review.mapper.ReviewMapper.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.common.dto.PageDto;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.common.mapper.PageMapper;
import org.team14.webty.common.util.FileStorageUtil;
import org.team14.webty.recommend.repository.RecommendRepository;
import org.team14.webty.review.dto.ReviewDetailResponse;
import org.team14.webty.review.dto.ReviewItemResponse;
import org.team14.webty.review.dto.ReviewRequest;
import org.team14.webty.review.entity.Review;
import org.team14.webty.review.entity.ReviewImage;
import org.team14.webty.review.mapper.ReviewMapper;
import org.team14.webty.review.repository.ReviewImageRepository;
import org.team14.webty.review.repository.ReviewRepository;
import org.team14.webty.reviewComment.dto.CommentResponse;
import org.team14.webty.reviewComment.entity.ReviewComment;
import org.team14.webty.reviewComment.mapper.ReviewCommentMapper;
import org.team14.webty.reviewComment.repository.ReviewCommentRepository;
import org.team14.webty.security.authentication.AuthWebtyUserProvider;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.repository.WebtoonRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final WebtoonRepository webtoonRepository;
	private final ReviewCommentRepository reviewCommentRepository;
	private final AuthWebtyUserProvider authWebtyUserProvider;
	private final FileStorageUtil fileStorageUtil;
	private final ReviewImageRepository reviewImageRepository;
	private final RecommendRepository recommendRepository;

	// 리뷰 상세 조회
	@Transactional
	public ReviewDetailResponse getFeedReview(Long id, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
		Page<ReviewComment> comments = reviewCommentRepository.findAllByReviewIdOrderByDepthAndCommentId(id, pageable);
		PageDto<CommentResponse> commentResponses = PageMapper.toPageDto(comments.map(ReviewCommentMapper::toResponse));
		List<ReviewImage> reviewImages = reviewImageRepository.findAllByReview(review);
		review.plusViewCount(); // 조회수 증가
		Map<String, Long> recommendCounts = recommendRepository.getRecommendCounts(id);
		return ReviewMapper.toDetail(review, commentResponses, reviewImages, recommendCounts);
	}

	// 전체 리뷰 조회
	@Transactional(readOnly = true)
	public Page<ReviewItemResponse> getAllFeedReviews(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		// 모든 리뷰 조회
		Page<Review> reviews = reviewRepository.findAllByOrderByReviewIdDesc(pageable);

		// 모든 리뷰 ID 리스트 추출
		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();

		// 리뷰 ID를 기반으로 한 번의 쿼리로 모든 댓글 조회
		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);

		// 리뷰 ID 리스트를 기반으로 한 번의 쿼리로 모든 리뷰 이미지 조회
		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);

		// 리뷰 ID 리스트를 기반으로 한 번의 쿼리로 모든 추천수 조회
		Map<Long, Long> likeCounts = getLikesMap(reviewIds);

		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	// 리뷰 생성
	@Transactional
	public Long createFeedReview(WebtyUserDetails webtyUserDetails, ReviewRequest reviewRequest) {
		WebtyUser webtyUser = getAuthenticatedUser(webtyUserDetails);

		Webtoon webtoon = webtoonRepository.findById(reviewRequest.getWebtoonId())
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));

		Review review = ReviewMapper.toEntity(reviewRequest, webtyUser, webtoon);
		reviewRepository.save(review);
		if (reviewRequest.getImages() != null && !reviewRequest.getImages().isEmpty()) {
			uploadReviewImage(review, reviewRequest.getImages());
		}
		return review.getReviewId();
	}

	// 리뷰 삭제
	@Transactional
	public void deleteFeedReview(WebtyUserDetails webtyUserDetails, Long id) {
		WebtyUser webtyUser = getAuthenticatedUser(webtyUserDetails);

		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getUserId().equals(webtyUser.getUserId())) {
			throw new BusinessException(ErrorCode.REVIEW_PERMISSION_DENIED);
		}

		// 해당 리뷰에 달린 댓글 삭제 처리
		reviewCommentRepository.deleteAll(reviewCommentRepository.findAllByReviewIdOrderByParentCommentIdAndDepth(id));
		// 해당 리뷰에 달린 이미지 삭제 처리
		deleteExistingReviewImages(review);
		reviewRepository.delete(review);
	}

	// 리뷰 수정
	@Transactional
	public Long updateFeedReview(WebtyUserDetails webtyUserDetails, Long id,
		ReviewRequest reviewRequest) {
		WebtyUser webtyUser = getAuthenticatedUser(webtyUserDetails);

		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		Webtoon webtoon = webtoonRepository.findById(reviewRequest.getWebtoonId())
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));

		if (!review.getUser().getUserId().equals(webtyUser.getUserId())) {
			throw new BusinessException(ErrorCode.REVIEW_PERMISSION_DENIED);
		}

		deleteExistingReviewImages(review);

		if (reviewRequest.getImages() != null && !reviewRequest.getImages().isEmpty()) {
			uploadReviewImage(review, reviewRequest.getImages());
		}

		review.updateReview(reviewRequest.getTitle(), reviewRequest.getContent(), reviewRequest.getSpoilerStatus(),
			webtoon);
		reviewRepository.save(review);

		return review.getReviewId();
	}

	// 특정 사용자의 리뷰 목록 조회
	@Transactional(readOnly = true)
	public Page<ReviewItemResponse> getReviewsByUser(WebtyUserDetails webtyUserDetails, int page, int size) {
		WebtyUser webtyUser = getAuthenticatedUser(webtyUserDetails);
		Pageable pageable = PageRequest.of(page, size);
		Page<Review> reviews = reviewRepository.findReviewByWebtyUser(webtyUser, pageable);
		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();
		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);
		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);
		Map<Long, Long> likeCounts = getLikesMap(reviewIds);

		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	// 조회수 내림차순으로 모든 리뷰 조회
	@Transactional(readOnly = true)
	public Page<ReviewItemResponse> getAllReviewsOrderByViewCountDesc(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Review> reviews = reviewRepository.findAllByOrderByViewCountDesc(pageable);

		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();

		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);

		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);

		Map<Long, Long> likeCounts = getLikesMap(reviewIds);

		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	// 특정 사용자의 리뷰 개수 조회
	@Transactional(readOnly = true)
	public Long getReviewCountByUser(WebtyUserDetails webtyUserDetails) {
		WebtyUser webtyUser = getAuthenticatedUser(webtyUserDetails);
		return reviewRepository.countReviewByWebtyUser(webtyUser);
	}

	@Transactional
	@SneakyThrows
	public void uploadReviewImage(Review review, List<MultipartFile> files) {
		List<String> fileUrls = fileStorageUtil.storeImageFiles(files);
		fileUrls.stream()
			.map(fileUrl -> toImageEntity(fileUrl, review))
			.forEach(reviewImageRepository::save);
	}

	private Map<Long, List<CommentResponse>> getreviewMap(List<Long> reviewIds) {
		List<ReviewComment> allComments = reviewCommentRepository.findAllByReviewIds(reviewIds);

		List<ReviewComment> parentComments = allComments.stream() // 부모 댓글만
			.filter(comment -> comment.getParentId() == null)
			.toList();

		// 리뷰 ID를 기준으로 부모 댓글을 매핑하는 Map 생성
		return parentComments.stream()
			.collect(Collectors.groupingBy(
				comment -> comment.getReview().getReviewId(),
				Collectors.mapping(ReviewCommentMapper::toResponse, Collectors.toList())
			));
	}

	private Map<Long, List<String>> getReviewImageMap(List<Long> reviewIds) {
		// 특정 리뷰 ID 리스트에 해당하는 모든 ReviewImage 조회 (한 번의 쿼리)
		List<ReviewImage> reviewImages = reviewImageRepository.findByReviewIdIn(reviewIds);

		// Review ID를 Key로, 이미지 URL 리스트를 Value로 변환
		return reviewImages.stream()
			.collect(Collectors.groupingBy(
				reviewImage -> reviewImage.getReview().getReviewId(),
				Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
			));
	}

	private void deleteExistingReviewImages(Review review) {
		List<ReviewImage> existingImages = reviewImageRepository.findAllByReview(review);

		for (ReviewImage image : existingImages) {
			fileStorageUtil.deleteFile(image.getImageUrl()); // 로컬 파일 삭제
		}
		reviewImageRepository.deleteAll(existingImages); // DB에서 삭제
	}

	public WebtyUser getAuthenticatedUser(WebtyUserDetails webtyUserDetails) {
		return authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
	}

	public Page<ReviewItemResponse> searchFeedReviewByTitle(int page, int size, String title) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Review> reviews = reviewRepository.findByTitleContainingIgnoreCaseOrderByReviewIdDesc(title, pageable);

		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();

		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);
		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);
		Map<Long, Long> likeCounts = getLikesMap(reviewIds);

		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	@Transactional
	public Page<ReviewItemResponse> getUserRecommendedReviews(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Review> reviews = recommendRepository.getUserRecommendReview(userId, pageable);
		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();
		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);
		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);
		Map<Long, Long> likeCounts = getLikesMap(reviewIds);
		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	private Map<Long, Long> getLikesMap(List<Long> reviewIds){
		List<Long> counts = recommendRepository.getLikeCounts(reviewIds);
		return IntStream.range(0, reviewIds.size())
			.boxed()
			.collect(Collectors.toMap(
				reviewIds::get,  // key: reviewId
				counts::get      // value: count
			));
	}

	public Page<ReviewItemResponse> searchReviewByWebtoonId(Long webtoonId, int page, int size) {
		Pageable pageable = PageRequest.of(page,size);
		Page<Review> reviews = reviewRepository.findReviewByWebtoonId(webtoonId,pageable);
		List<Long> reviewIds = reviews.stream().map(Review::getReviewId).toList();
		Map<Long, List<CommentResponse>> commentMap = getreviewMap(reviewIds);
		Map<Long, List<String>> reviewImageMap = getReviewImageMap(reviewIds);
		Map<Long, Long> likeCounts = getLikesMap(reviewIds);
		return reviews.map(review ->
			ReviewMapper.toResponse(
				review,
				commentMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				reviewImageMap.getOrDefault(review.getReviewId(), Collections.emptyList()),
				likeCounts.get(review.getReviewId())
			)
		);
	}

	@Transactional
	public void patchReviewIsSpoiler(Long id) {
		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
		review.patchIsSpoiler();
		reviewRepository.save(review);
	}
}
