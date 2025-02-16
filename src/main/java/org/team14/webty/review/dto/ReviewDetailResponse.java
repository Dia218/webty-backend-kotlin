package org.team14.webty.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.team14.webty.common.dto.PageDto;
import org.team14.webty.review.enumrate.SpoilerStatus;
import org.team14.webty.reviewComment.dto.CommentResponse;
import org.team14.webty.user.dto.UserDataResponse;
import org.team14.webty.webtoon.dto.WebtoonSummaryDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewDetailResponse {
	private Long reviewId;
	private UserDataResponse userDataResponse; // 사용자 프로필, 닉네임
	private WebtoonSummaryDto webtoon;
	private String content;
	private String title;
	private Integer viewCount;
	private SpoilerStatus spoilerStatus;
	private List<String> imageUrls;
	private PageDto<CommentResponse> commentResponses;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Map<String, Long> recommendCount;
}
