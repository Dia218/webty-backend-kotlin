package org.team14.webty.voting.mapper;

import org.team14.webty.voting.dto.SimilarResponse;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.webtoon.entity.Webtoon;

public class SimilarMapper {
	public static Similar toEntity(Long userId, Long choiceWebtoonId, Webtoon targetWebtoon) {
		return Similar.builder()
			.similarWebtoonId(choiceWebtoonId)
			.similarResult(0L)
			.userId(userId)
			.targetWebtoon(targetWebtoon)
			.build();
	}

	public static SimilarResponse toResponse(Similar similar, Webtoon similarWebtoon) {
		return SimilarResponse.builder()
			.similarId(similar.getSimilarId())
			.similarThumbnailUrl(similarWebtoon.getThumbnailUrl())
			.similarResult(similar.getSimilarResult())
			.similarWebtoonId(similarWebtoon.getWebtoonId())
			.build();
	}
}
