package org.team14.webty.recommend.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.recommend.entity.Recommend;
import org.team14.webty.recommend.enumerate.LikeType;
import org.team14.webty.recommend.repository.RecommendRepository;
import org.team14.webty.review.entity.Review;
import org.team14.webty.review.repository.ReviewRepository;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.WebtyUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendService {
	private final ReviewRepository reviewRepository;
	private final RecommendRepository recommendRepository;

	@Transactional
	public Long createRecommend(WebtyUserDetails webtyUserDetails, Long reviewId, String type) {
		WebtyUser webtyUser = webtyUserDetails.getWebtyUser();
		Review review = reviewIdToReview(reviewId);

		if (recommendRepository.existsByReviewAndUserIdAndLikeType(
			review, webtyUser.getUserId(), LikeType.fromString(type))) {
			throw new BusinessException(ErrorCode.RECOMMEND_DUPLICATION_ERROR);
		}

		Recommend recommend = Recommend.builder()
			.likeType(LikeType.fromString(type))
			.userId(webtyUser.getUserId())
			.review(review)
			.build();

		recommendRepository.save(recommend);
		return recommend.getVoteId();
	}

	@Transactional
	public void deleteRecommend(WebtyUserDetails webtyUserDetails, Long reviewId, String type) {
		WebtyUser webtyUser = webtyUserDetails.getWebtyUser();
		Review review = reviewIdToReview(reviewId);
		Recommend recommend = recommendRepository.
			findByReviewAndUserIdAndLikeType(review, webtyUser.getUserId(), LikeType.fromString(type))
			.orElseThrow(() -> new BusinessException(ErrorCode.RECOMMEND_NOT_FOUND));
		recommendRepository.delete(recommend);
	}

	public Map<String, Long> getRecommendCounts(Long reviewId) {
		Map<String, Long> counts = recommendRepository.getRecommendCounts(reviewId);
		// 아무것도 없으면 null 반환할 수 있기 때문에 처리
		return counts != null ? counts : Map.of("likes",0L,"hates",0L);
	}

	private Review reviewIdToReview(Long reviewId){
		return reviewRepository.findById(reviewId)
			.orElseThrow(()->new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
	}

	public Map<String, Boolean> isRecommended(WebtyUserDetails webtyUserDetails, Long reviewId) {
		WebtyUser webtyUser = webtyUserDetails.getWebtyUser();
		Map<String, Long> rawResult = recommendRepository.findRecommendStatusByUserAndReview(webtyUser.getUserId(), reviewId);
		return rawResult.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().equals(1L)
			));
	}
}
