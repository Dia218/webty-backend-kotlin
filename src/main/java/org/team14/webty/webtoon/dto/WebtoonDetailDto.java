package org.team14.webty.webtoon.dto;

import org.team14.webty.webtoon.enumerate.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonDetailDto {
	private Long webtoonId;
	private String webtoonName;
	private Platform platform;
	private String webtoonLink;
	private String thumbnailUrl;
	private String authors;
	private boolean finished;
}
