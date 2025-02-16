package org.team14.webty.review.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.team14.webty.common.dto.PageDto;
import org.team14.webty.review.dto.ReviewDetailResponse;
import org.team14.webty.review.dto.ReviewItemResponse;
import org.team14.webty.review.dto.ReviewRequest;
import org.team14.webty.review.entity.Review;
import org.team14.webty.review.entity.ReviewImage;
import org.team14.webty.reviewComment.dto.CommentResponse;
import org.team14.webty.user.dto.UserDataResponse;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper;

public class ReviewMapper {
	public static Review toEntity(ReviewRequest request, WebtyUser webtyUser, Webtoon webtoon) {
		return Review.builder()
			.user(webtyUser)
			.isSpoiler(request.getSpoilerStatus())
			.content(request.getContent())
			.title(request.getTitle())
			.viewCount(0)
			.webtoon(webtoon)
			.createdAt(LocalDateTime.now())
			.build();
	}

	public static ReviewItemResponse toResponse(Review review, List<CommentResponse> comments,
		List<String> imageUrls, Long likeCount) {
		return ReviewItemResponse.builder()
			.reviewId(review.getReviewId())
			.userDataResponse(new UserDataResponse(review.getUser()))
			.content(review.getContent())
			.title(review.getTitle())
			.viewCount(review.getViewCount())
			.spoilerStatus(review.getIsSpoiler())
			.webtoon(WebtoonApiResponseMapper.toSummaryDto(review.getWebtoon()))
			.imageUrls(imageUrls)
			.commentCount(comments.size()) // 댓글 개수만
			.recommendCount(likeCount)
			.build();
	}

	public static ReviewDetailResponse toDetail(Review review, PageDto<CommentResponse> comments,
		List<ReviewImage> reviewImages, Map<String, Long> recommendCount) {

		return ReviewDetailResponse.builder()
			.reviewId(review.getReviewId())
			.userDataResponse(new UserDataResponse(review.getUser()))
			.webtoon(WebtoonApiResponseMapper.toSummaryDto(review.getWebtoon()))
			.content(review.getContent())
			.title(review.getTitle())
			.viewCount(review.getViewCount())
			.spoilerStatus(review.getIsSpoiler())
			.imageUrls(reviewImages.stream().map(ReviewImage::getImageUrl).toList())
			.commentResponses(comments) // 댓글 정보까지
			.createdAt(review.getCreatedAt())
			.updatedAt(review.getUpdatedAt())
			.recommendCount(recommendCount)
			.build();
	}

	public static ReviewImage toImageEntity(String imageUrl, Review review) {
		return ReviewImage.builder()
			.imageUrl(imageUrl)
			.review(review)
			.build();
	}
}
