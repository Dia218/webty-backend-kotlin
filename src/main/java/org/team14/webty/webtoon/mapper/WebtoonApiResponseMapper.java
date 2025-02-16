package org.team14.webty.webtoon.mapper;

import java.util.List;

import org.team14.webty.webtoon.api.WebtoonApiResponse;
import org.team14.webty.webtoon.dto.WebtoonDetailDto;
import org.team14.webty.webtoon.dto.WebtoonSummaryDto;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.enumerate.Platform;

public class WebtoonApiResponseMapper {
	public static Webtoon toEntity(WebtoonApiResponse webtoonApiResponse) {
		return Webtoon.builder()
			.webtoonName(webtoonApiResponse.getTitle())
			.platform(Platform.fromString(webtoonApiResponse.getProvider()))
			.webtoonLink(webtoonApiResponse.getUrl())
			.thumbnailUrl(webtoonApiResponse.getThumbnails().isEmpty() ? null :
				webtoonApiResponse.getThumbnails().getFirst())
			.authors(formatAuthors(webtoonApiResponse.getAuthors()))
			.finished(webtoonApiResponse.isEnd())
			.build();
	}

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

	public static WebtoonSummaryDto toSummaryDto(Webtoon webtoon) {
		return WebtoonSummaryDto.builder()
			.webtoonId(webtoon.getWebtoonId())
			.webtoonName(webtoon.getWebtoonName())
			.thumbnailUrl(webtoon.getThumbnailUrl())
			.build();
	}

	public static String formatAuthors(List<String> authors) {
		return authors == null ? "" : String.join(", ", authors);
	}
}
