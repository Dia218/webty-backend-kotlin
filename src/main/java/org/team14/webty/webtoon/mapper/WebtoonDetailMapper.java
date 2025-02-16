package org.team14.webty.webtoon.mapper;

import org.team14.webty.webtoon.dto.WebtoonDetailDto;
import org.team14.webty.webtoon.entity.Webtoon;

public class WebtoonDetailMapper {
	public static WebtoonDetailDto toDto(Webtoon webtoon) {
		return WebtoonDetailDto.builder()
			.webtoonId(webtoon.getWebtoonId())
			.webtoonName(webtoon.getWebtoonName())
			.platform(webtoon.getPlatform())
			.webtoonLink(webtoon.getWebtoonLink())
			.thumbnailUrl(webtoon.getThumbnailUrl())
			.authors(webtoon.getAuthors())
			.finished(webtoon.isFinished())
			.build();
	}
}