package org.team14.webty.voting.mapper;

import org.team14.webty.voting.dto.SimilarResponse;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.webtoon.entity.Webtoon;

public class SimilarMapper {
	public static Similar toEntity(Long userId, String similarWebtoonName, Webtoon webtoon) {
		return Similar.builder()
			.userId(userId)
			.similarWebtoonName(similarWebtoonName)
			.similarResult(0L)
			.webtoon(webtoon)
			.build();
	}

	public static SimilarResponse toResponse(Similar similar) {
		return SimilarResponse.builder()
			.similarId(similar.getSimilarId())
			.thumbnailUrl(similar.getWebtoon().getThumbnailUrl())
			.similarResult(similar.getSimilarResult())
			.build();
	}
}
