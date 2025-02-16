package org.team14.webty.review.dto;

import java.util.List;

import org.team14.webty.review.enumrate.SpoilerStatus;
import org.team14.webty.user.dto.UserDataResponse;
import org.team14.webty.webtoon.dto.WebtoonSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewItemResponse {
	private Long reviewId;
	private UserDataResponse userDataResponse; // 사용자 프로필, 닉네임
	private String content;
	private String title;
	private Integer viewCount;
	private SpoilerStatus spoilerStatus;
	private WebtoonSummaryDto webtoon;
	private List<String> imageUrls;
	private Integer commentCount;
	private Long recommendCount;
}
