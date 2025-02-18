package org.team14.webty.voting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarResponse {
	private Long similarId;
	private String similarThumbnailUrl;
	private Long similarResult;
	private Long similarWebtoonId; // webtoon-detail 페이지 이동 시 필요
}
