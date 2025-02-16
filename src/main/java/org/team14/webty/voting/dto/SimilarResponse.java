package org.team14.webty.voting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarResponse {
	private Long similarId;
	private String thumbnailUrl;
	private Long similarResult;
}
