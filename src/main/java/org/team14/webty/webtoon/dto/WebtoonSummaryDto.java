package org.team14.webty.webtoon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebtoonSummaryDto {
	private String webtoonName;
	private Long webtoonId;
	private String thumbnailUrl;
}
