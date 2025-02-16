package org.team14.webty.webtoon.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WebtoonPageApiResponse {
	@JsonProperty("webtoons")
	private List<WebtoonApiResponse> webtoonApiResponses;
	@JsonProperty("total")
	private int total;
	@JsonProperty("isLastPage")
	private boolean isLastPage;
}
