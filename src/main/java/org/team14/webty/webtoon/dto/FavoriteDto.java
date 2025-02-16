package org.team14.webty.webtoon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDto {
	@JsonProperty("webtoonId")
	private Long webtoonId;
}
