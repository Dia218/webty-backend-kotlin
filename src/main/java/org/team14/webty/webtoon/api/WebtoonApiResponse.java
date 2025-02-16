package org.team14.webty.webtoon.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WebtoonApiResponse {
	@JsonProperty("title")
	private String title;

	@JsonProperty("provider")
	private String provider;

	@JsonProperty("url")
	private String url;

	@JsonProperty("thumbnail")
	private List<String> thumbnails;

	@JsonProperty("isEnd")
	private boolean isEnd;

	@JsonProperty("authors")
	private List<String> authors;
}
